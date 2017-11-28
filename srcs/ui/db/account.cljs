(ns ui.db.account
  (:require
   [ui.db.misc :refer [get-url <sub reduce-statuses reduce-errors]]
   [re-frame.core :as rf]))

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
    :dispatch [:load-account-type final-succeed-fx]}))

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
     {:json/fetch->path {:path [::account :info-response]
                         :uri (get-url db (str "/api/accounts/" (name user-type) "/" user-id "/"))
                         :token token
                         :succeed-fx [:dispatch-fx final-succeed-fx]}})))

(rf/reg-event-fx
 :logout
 [(rf/inject-cofx :store)]
 (fn [{db :db store :store} _]
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

