(ns ui.db.resident-profile
  (:require
   [reagent.core :as reagent]
   [ui.db.misc :refer [get-url <sub fields->schema
                       cljstime->drf-date-time setup-form-initial-values]]
   [ui.db.shift :refer [parse-date-time]]
   [re-frame.core :as rf]
   [cljs-time.core :as dt]
   [cljs-time.format :as format]))

(def resident-profile-form-fields
  {"Personal Information" {:first-name "First Name"
                           :last-name "Last Name"}
   "Residency" {:residency-program {:type :select
                                    :label "Residency Program"
                                    :items #(<sub [:residency-program-as-options])}
                :specialities {:type :multy-select
                               :label "Specialities"
                               :items #(<sub [:speciality-as-options])}
                :residency-years {:type :input
                                  :label "Residency Years"
                                  :width 3}
                :state-license {:type :radio
                                :label "State Licence"
                                :items {true "Yes" false "No"}}
                :board-score {:type :radio
                              :label "Board Score"
                              :items {true "Yes" false "No"}}}})

(def resident-notification-form-fields
  {"I would like to recieve emails when:"
   {:notification-new-shifts {:type :toggle :label "New shift is created"}
    :notification-application-status-changing  {:type :toggle :label "My application is approved/rejected"}
    :notification-new-messages {:type :toggle :label "New message from a scheduler is received"}}})

(def schema
  {::resident-profile
   {:profile-form
    {:fields (fields->schema resident-profile-form-fields)
     :response {:status :not-asked}}
    :notification-form
    {:fields (fields->schema resident-notification-form-fields)
     :response {:status :not-asked}}}})

(rf/reg-sub
 ::resident-profile
 (fn [db path]
   (get-in db path)))

(rf/reg-event-db
 :init-resident-profile-form
 (fn [db _]
   (-> db
       (assoc-in [::resident-profile :profile-form]
                 (get-in schema [::resident-profile :profile-form]))
       (update-in [::resident-profile :profile-form :fields]
                  (setup-form-initial-values (<sub [:user-info]))))))

(rf/reg-sub
 :resident-profile-form-cursor
 #(<sub [:cursor [::resident-profile :profile-form]]))

(rf/reg-sub
 :resident-profile-form-response
 #(<sub [::resident-profile :profile-form :response]))

(rf/reg-event-fx
 :update-resident-profile
 (fn [{db :db} [_ type]]
   (let [pk (<sub [:user-id])
         state (<sub [:user-state])
         body (<sub [::resident-profile :profile-form :fields])
         url (if (= state 1)
               (get-url db "/api/accounts/resident/" pk "/fill_profile/")
               (get-url db "/api/accounts/resident/" pk "/"))]
     {:json/fetch->path {:path [::resident-profile :profile-form :response]
                         :uri url
                         :method (if (= state 1) "POST" "PATCH")
                         :token (<sub [:token])
                         :body body
                         :succeed-fx (fn [data]
                                       [:update-account-info
                                        (if (= state 1)
                                          (merge body {:state 2})
                                          data)])}})))

(rf/reg-event-db
 :init-resident-notification-form
 (fn [db _]
   (-> db
       (assoc-in  [::resident-profile :notification-form]
                  (get-in schema [::resident-profile :notification-form]))
       (update-in [::resident-profile :notification-form :fields]
                  (setup-form-initial-values (<sub [:user-info]))))))

(rf/reg-sub
 :resident-notification-form-cursor
 #(<sub [:cursor [::resident-profile :notification-form]]))

(rf/reg-sub
 :resident-notification-form-response
 #(<sub [::resident-profile :notification-form :response]))

(rf/reg-event-fx
 :update-resident-notifications
 (fn [{db :db} [_ type]]
   (let [pk (<sub [:user-id])]
     {:json/fetch->path {:path [::resident-profile :notification-form :response]
                         :uri (get-url db "/api/accounts/resident/" pk "/")
                         :method "PATCH"
                         :token (<sub [:token])
                         :body (<sub [::resident-profile :notification-form :fields])
                         :succeed-fx (fn [data]
                                       [:update-account-info data])}})))