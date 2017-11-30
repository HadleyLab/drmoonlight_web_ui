(ns ui.db.resident
    (:require-macros [cljs.core.async.macros :refer [go]])
    (:require
     [ui.db.misc :refer [get-url <sub reduce-statuses reduce-errors]]
     [re-frame.core :as rf]
     [clojure.string :refer [replace]]))

(def schema
  {::resident
   {:waiting-for-approve {:status :not-asked}
    :profile-detail {}
    :response {:status :not-asked}}})

(rf/reg-event-db
 :init-resident-approve-rejects-form
 (fn [db]
   (assoc-in db [::resident :response] {:status :not-asked})))

(rf/reg-event-fx
 :load-residents-which-waiting-for-approve
 (fn [{db :db} _]
   {:json/fetch->path {:path [::resident :waiting-for-approve]
                       :uri (get-url db "/api/accounts/resident/waiting_for_approval/")
                       :token (<sub [:token])}}))
(rf/reg-event-fx
 :load-resident-profile-detail
 (fn [{db :db} [_ pk]]
   {:json/fetch->path {:path [::resident :profile-detail pk]
                       :uri (get-url db "/api/accounts/resident/" pk "/")
                       :token (<sub [:token])}}))
(rf/reg-sub
 ::resident
 (fn [db path]
   (get-in db path)))

(rf/reg-sub
 :get-residents-which-waiting-for-approve
 #(<sub [::resident :waiting-for-approve]))

(rf/reg-sub
 :get-resident-approve-reject-result
 #(<sub [::resident :response]))

(rf/reg-sub
 :get-resident-profile-detail
 (fn [db [_ pk]]
   (get-in db [::resident :profile-detail pk] {:status :not-asked})))

(rf/reg-event-fx
 :approve-resident
 (fn [{db :db} [_ pk fx]]
   {:json/fetch->path {:path [::resident :response]
                       :uri (get-url db "/api/accounts/resident/" pk "/approve/")
                       :token (<sub [:token])
                       :method "POST"
                       :succeed-fx fx}}))
(rf/reg-event-fx
 :reject-resident
 (fn [{db :db} [_ pk fx]]
   {:json/fetch->path {:path [::resident :response]
                       :uri (get-url db "/api/accounts/resident/" pk "/reject/")
                       :token (<sub [:token])
                       :method "POST"
                       :succeed-fx fx}}))
