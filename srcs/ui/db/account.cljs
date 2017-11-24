(ns ui.db.account
  (:require
   [ui.db.misc :refer [get-url]]
   [re-frame.core :as rf]))

(rf/reg-sub
 :account
 (fn [db path]
   (get-in db path)))

(rf/reg-sub
 :user-type
 #(deref (rf/subscribe [:account :user-type])))

(rf/reg-sub
 :user-email
 #(deref (rf/subscribe [:account :user-info :email])))

(rf/reg-sub
 :user-state
 #(deref (rf/subscribe [:account :user-info :state])))

(rf/reg-sub
 :user-id
 #(deref (rf/subscribe [:account :user-info :id])))

(rf/reg-sub
 :token
 #(deref (rf/subscribe [:account :token])))

(rf/reg-event-fx
 :logout
 [(rf/inject-cofx :store)]
 (fn [{db :db store :store} _]
   {:db (dissoc db :account)
    :store (dissoc store :token)
    :dispatch [:goto "/"]}))

(rf/reg-event-fx
 :login-succeed
 [(rf/inject-cofx :store)]
 (fn [{db :db store :store} [_ {data :data fxs :fxs}]]
   {:db (assoc-in db [:account] {:token (:auth-token data)})
    :store (assoc store :token (:auth-token data))
    :dispatch [:load-account-info (:auth-token data) fxs]}))

(rf/reg-event-fx
 :load-account-info
 (fn [{db :db} [_ token fxs]]
   {:json/fetch {:uri (get-url db "/api/accounts/me/")
                 :token token
                 :success {:event :loaded-login-data :fxs fxs}
                 :error {:event :login-failure :fxs fxs}}}))

(rf/reg-event-fx
 :loaded-login-data
 (fn [{db :db} [_ {data :data fxs :fxs}]]
   (let [user-type (cond
                     (:is-resident data) :resident
                     (:is-scheduler data) :scheduler
                     (:is-account-manager data) :account-manager)
         token (get-in db [:account :token])]
     {:db (assoc-in db [:account :user-type] user-type)
      :json/fetch {:uri (get-url db (str "/api/accounts/" (name user-type) "/" (:pk data) "/"))
                   :token token
                   :success {:event :loaded-user-data :fxs fxs}
                   :error {:event :login-failure :fxs fxs}}})))

(rf/reg-event-fx
 :loaded-user-data
 (fn [{db :db} [_ {data :data fxs :fxs}]]
   (let [succeed-fx (:succeed-fx fxs {})
         fxs (if (fn? succeed-fx) (succeed-fx db) succeed-fx)]
     (merge
      {:db (assoc-in db [:account :user-info] data)} fxs))))
