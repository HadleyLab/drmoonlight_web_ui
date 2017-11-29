(ns ui.db.account
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [cljs.core.async :as a :refer [<! >!]]
   [haslett.client :as ws]
   [haslett.format :as fmt]
   [ui.db.misc :refer [get-url <sub reduce-statuses reduce-errors]]
   [re-frame.core :as rf]
   [clojure.string :refer [replace]]))

(def schema
  {::account
   {:token nil
    :login-form
    {:fields
     {:email ""
      :password ""}
     :response {:status :not-asked}}
    :type-response {:status :not-asked}
    :info-response {:status :not-asked}}})

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
 (fn [_ _]
   {:account-websocket (str "ws://localhost:8000/accounts/user/" (<sub [:token]) "/")}))

(rf/reg-event-fx
 ::setup-login-form-and-redirect
 (fn [{db :db} _]
   (let [user-type (<sub [:user-type])]
     {:db (assoc-in db [::account :login-form] (get-in schema [::account :login-form]))
      :dispatch [:goto user-type]})))

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
                       :succeed-fx [:load-account-info final-succeed-fx]}}))

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
  (= (<sub [:user-state])  1))

(defn is-profile-filled []
  (= (<sub [:user-state])  2))

(defn is-approved []
  (= (<sub [:user-state])  3))

(defn is-rejected []
  (= (<sub [:user-state])  4))
