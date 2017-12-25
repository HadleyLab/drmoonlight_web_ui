(ns ui.db.account
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [cljs.core.async :as a :refer [<! >!]]
   [haslett.client :as ws]
   [haslett.format :as fmt]
   [ui.db.misc :refer [get-url <sub reduce-statuses
                       reduce-errors fields->schema get-timezone-str]]
   [re-frame.core :as rf]
   [clojure.string :refer [replace]]))

(def scheduler-form-fields
  {"Personal Information"
   {:first-name {:type :input
                 :label "First Name"}
    :last-name {:type :input
                :label "Last Name"}
    :facility-name {:type :input
                    :label "Hospital / Facility name"}
    :department-name {:type :input
                      :label "Department name"}}
   "Account settings"
   {:email {:type :input
            :label "Email"}
    :password {:type :input
               :label "Password"}}})

(def resident-form-fields
  {"Personal Information"
   {:first-name {:type :input
                 :label "First Name"}
    :last-name {:type :input
                :label "Last Name"}}
   "Account settings"
   {:email {:type :input
            :label "Email"}
    :password {:type :input
               :label "Password"}}})

(def change-password-form-fields
  {""
   {:current-password {:type :input :label "Current password" :width 8}
    :new-password {:type :input :label "New password" :width 8}
    :new-password-confirm {:type :input :label "Confirm new password" :width 8}}})

(def schema
  {::account
   {:token nil
    :sign-up-form
    {:fields (fields->schema scheduler-form-fields)
     :mode :resident
     :response {:status :not-asked}}
    :activation-response {:status :not-asked}
    :password-reset-form
    {:fields {:password ""}
     :response {:status :not-asked}}
    :forgot-password-form
    {:fields {:email ""}
     :response {:status :not-asked}}
    :login-form
    {:fields
     {:email ""
      :password ""}
     :response {:status :not-asked}}
    :type-response {:status :not-asked}
    :info-response {:status :not-asked}
    :change-password-form
    {:fields (fields->schema change-password-form-fields)
     :response {:status :not-asked}}}})

(defn event->action [data]
  (update-in data [:event] #(keyword (replace % #"_" "-"))))

(def json-websocker-formater
  "Read and write data encoded in JSON."
  (reify fmt/Format
    (read  [_ s] (event->action (js->clj (js/JSON.parse s) :keywordize-keys true)))
    (write [_ v] (js/JSON.stringify (clj->js v)))))

(defonce websocket (atom nil))

(rf/reg-fx
 :account-websocket
 (fn [url]
   (go
     (let [stream (<! (ws/connect url  {:format json-websocker-formater}))]
       (reset! websocket stream)
       (while (not (nil? @websocket))
         (let [message (<! (:source stream))]
           (if (nil? message)
             (let [{code :code} (<! (:close-status stream))]
               (when-not (= code 1000)
                 ;; Reconect doesn't work now
                 ;; See https://github.com/weavejester/haslett/issues/5 for more details
                 (js/setTimeout #(rf/dispatch [:websocket-connect]) 1000))
               (reset! websocket nil))
             (rf/dispatch [(:event message) (:payload message)]))))))))

(rf/reg-event-fx
 :websocket-connect
 (fn [{db :db} _]
   (let [base-url (replace (:base-url db) #"^http" "ws")]
     {:account-websocket (str base-url "/accounts/user/" (<sub [:token]) "/")})))

(rf/reg-event-fx
 ::setup-login-form-and-redirect
 (fn [{db :db} _]
   (let [user-type (<sub [:user-type])]
     {:db (assoc-in db [::account :login-form] (get-in schema [::account :login-form]))
      :dispatch (cond
                  (= user-type :account-manager) [:goto user-type]
                  :else [:goto user-type :schedule])})))

(rf/reg-event-fx
 :submit-login-form
 (fn [{db :db} _]
   (let [body (get-in db [::account :login-form :fields])
         final-succeed-fx [::setup-login-form-and-redirect]]
     {:json/fetch->path {:path [::account :login-form :response]
                         :uri (get-url db "/api/accounts/token/create/")
                         :method "post"
                         :body body
                         :succeed-fx (fn [data] [:save-token (:auth-token data) final-succeed-fx])}})))
(rf/reg-event-fx
 :save-token
 [(rf/inject-cofx :store)]
 (fn [{db :db store :store} [_ token final-succeed-fx]]
   {:db (assoc-in db [::account :token] token)
    :store (assoc store :token token)
    :dispatch-n [[:websocket-connect]
                 [:load-account-type final-succeed-fx]]}))

(rf/reg-event-fx
 :load-account-type
 (fn [{db :db} [_ final-succeed-fx]]
   {:json/fetch->path {:path [::account :type-response]
                       :uri (get-url db "/api/accounts/me/")
                       :token (<sub [:token])
                       :succeed-fx [:load-account-info final-succeed-fx]
                       :failure-fx [:process-invalid-token]}}))

(rf/reg-event-fx
 :force-reload
 (fn [world [_]]
   (.reload js/location)
   {}))

(rf/reg-event-fx
 :process-invalid-token
 [(rf/inject-cofx :store)]
 (fn [{store :store} [_]]
   {:store (dissoc store :token)
    :dispatch [:force-reload]}))

(def states-map
  {1 :new
   2 :profile-filled
   3 :approved
   4 :rejected})

(defn- convert-state [data]
  (if (:state data)
    (merge data {:state (get states-map (:state data))})
    data))

(rf/reg-event-fx
 :load-account-info
 (fn [{db :db} [_ final-succeed-fx]]
   (let [user-type (<sub [:user-type])
         user-id (<sub [:user-id])
         token (<sub [:token])]
     (if (= user-type :account-manager)
       {:dispatch [:dispatch-fx final-succeed-fx]}
       {:json/fetch->path {:path [::account :info-response]
                           :uri (get-url db (str "/api/accounts/" (name user-type) "/" user-id "/"))
                           :token token
                           :map-result convert-state
                           :succeed-fx [:dispatch-fx final-succeed-fx]}}))))

(rf/reg-event-db
 :update-account-info
 (fn [db [_ data]]
   (update-in db [::account :info-response :data] #(merge % data))))

(rf/reg-event-fx
 :logout
 [(rf/inject-cofx :store)]
 (fn [{db :db store :store} _]
   (if-let [stream @websocket]
     (ws/close stream))
   {:db (assoc db ::account schema)
    :store (dissoc store :token)
    :dispatch [:goto "/"]}))

(rf/reg-sub
 ::account
 (fn [db path]
   (get-in db path)))

(rf/reg-sub
 ::info-response
 (fn [db [_ & path]]
   (<sub (into [] (concat [::account :info-response :data] path)))))

(rf/reg-sub
 :user-info
 #(<sub [::info-response]))

(rf/reg-sub
 :user-type
 (fn [db _]
   (let
    [type-data (<sub [::account :type-response :data])
     user-type (cond
                 (:is-resident type-data) :resident
                 (:is-scheduler type-data) :scheduler
                 (:is-account-manager type-data) :account-manager)]
     user-type)))

(rf/reg-sub
 :user-email
 #(<sub [::info-response :email]))

(rf/reg-sub
 :user-state
 #(<sub [::info-response :state]))

(rf/reg-sub
 :user-id
 #(<sub [::account :type-response :data :pk]))

(rf/reg-sub
 :token
 #(<sub [::account :token]))

(rf/reg-sub
 :login-form-cursor
 #(<sub [:cursor [::account :login-form :fields]]))

(rf/reg-sub
 :login-form-response
 (fn [_ _]
   (let [responses [(<sub [::account :login-form :response])
                    (<sub [::account :type-response])
                    (<sub [::account :info-response])]
         status (apply reduce-statuses (map :status responses))
         errors (apply reduce-errors (map :errors responses))]
     {:status status :errors errors})))

(defn is-resident []
  (= (<sub [:user-type]) :resident))

(defn is-scheduler []
  (= (<sub [:user-type]) :scheduler))

(defn is-account-manager []
  (= (<sub [:user-type]) :account-manager))

(defn is-new []
  (= (<sub [:user-state]) :new))

(defn is-profile-filled []
  (= (<sub [:user-state]) :profile-filled))

(defn is-approved []
  (= (<sub [:user-state]) :approved))

(defn is-rejected []
  (= (<sub [:user-state]) :rejected))

(rf/reg-event-db
 :init-forgot-password-form
 (fn [db [_]]
   (let [login-form-email (<sub [::account :login-form :fields :email])]
     (-> db
         (assoc-in [::account :forgot-password-form] (get-in schema [::account :forgot-password-form]))
         (assoc-in [::account :forgot-password-form :fields :email] login-form-email)))))

(rf/reg-sub
 :forgot-password-form-email-cursor
 #(<sub [:cursor [::account :forgot-password-form :fields :email]]))

(rf/reg-event-fx
 :rest-password
 (fn [{db :db} [_ fx]]
   (let [email @(<sub [:forgot-password-form-email-cursor])]
     {:json/fetch->path {:path [::account :forgot-password-form :response]
                         :uri (get-url db "/api/accounts/password/reset/")
                         :method "post"
                         :body {:email email}
                         :succeed-fx fx}})))

(rf/reg-sub
 :forgot-password-form-response
 #(<sub [::account :forgot-password-form :response]))

(rf/reg-event-db
 :init-sign-up-form
 (fn [db [_]]
   (assoc-in db [::account :sign-up-form] (get-in schema [::account :sign-up-form]))))

(rf/reg-sub
 :sign-up-form-cursor
 #(<sub [:cursor [::account :sign-up-form]]))

(rf/reg-sub
 :sign-up-form-status
 #(<sub [::account :sign-up-form :response :status]))

(rf/reg-event-fx
 :do-sigh-up
 (fn [{db :db} [_ fx]]
   (let [mode (<sub [::account :sign-up-form :mode])
         data (<sub [::account :sign-up-form :fields])]
     {:json/fetch->path {:path [::account :sign-up-form :response]
                         :uri (get-url db (str "/api/accounts/" (name mode) "/"))
                         :method "post"
                         :body (merge data {:timezone (get-timezone-str)})
                         :succeed-fx fx}})))

(rf/reg-event-fx
 :activate
 (fn [{db :db} [_ data]]
   {:json/fetch->path {:path [::account :activation-response]
                       :uri (get-url db "/api/accounts/activate/")
                       :method "post"
                       :body data}}))

(rf/reg-sub
 :activation-response
 #(<sub [::account :activation-response]))

(rf/reg-event-db
 :init-password-reset-form
 (fn [db [_]]
   (assoc-in db [::account :password-reset-form] (get-in schema [::account :password-reset-form]))))

(rf/reg-sub
 :password-reset-form-password-cursor
 #(<sub [:cursor [::account :password-reset-form :fields :password]]))

(rf/reg-event-fx
 :confirm-rest-password
 (fn [{db :db} [_ data]]
   {:json/fetch->path {:path [::account :password-reset-form :response]
                       :uri (get-url db "/api/accounts/password/reset/confirm/")
                       :method "post"
                       :body (merge data {:new-password @(<sub [:password-reset-form-password-cursor])})
                       :succeed-fx [:goto :login]}}))

(rf/reg-sub
 :password-reset-form-response
 #(<sub [::account :password-reset-form :response]))

 ; Change password form

(rf/reg-event-db
 :init-change-password-form
 (fn [db [_]]
   (assoc-in db [::account :change-password-form] (get-in schema [::account :change-password-form]))))

(rf/reg-event-db
 :change-password-succeed
 (fn [db _]
   (-> db
       (assoc-in [::account :change-password-form :fields]
                 (get-in schema [::account :change-password-form :fields]))
       (update-in [::account :change-password-form :response] #(merge % {:errors {}})))))

(rf/reg-sub
 :change-password-form-cursor
 #(<sub [:cursor [::account :change-password-form]]))

(rf/reg-sub
 :change-password-form-response
 #(<sub [::account :change-password-form :response]))

(rf/reg-event-fx
 :update-user-password
 (fn [{db :db} [_ data]]
   (let [body (<sub [::account :change-password-form :fields])]
     {:json/fetch->path {:path [::account :change-password-form :response]
                         :uri (get-url db "/api/accounts/password/")
                         :token (<sub [:token])
                         :method "post"
                         :body (dissoc body :new-password-confirm)
                         :succeed-fx [:change-password-succeed]}})))
