(ns ui.db.resident-profile
  (:require
   [reagent.core :as reagent]
   [ui.db.misc :refer [get-url <sub fields->schema
                       setup-form-initial-values get-timezone-str]]
   [re-frame.core :as rf]))

(def resident-profile-form-fields
  {"Personal Information" {:first-name {:type :input
                                        :label "First Name"}
                           :last-name {:type :input
                                       :label "Last Name"}
                           :avatar {:type :avatar
                                    :label "Avatar"}}
   "Residency" (array-map
                :residency-program {:type :textarea
                                    :label "Residency/Fellowship Program"}
                :specialities {:type :multy-select
                               :label "Specialities"
                               :items #(<sub [:speciality-as-options])}
                :residency-years {:type :input
                                  :label "Residency/Fellowship Years"
                                  :width 4}
                :state-license {:type :radio
                                :label "State Licence"
                                :items {true "yes" false "no"}}
                :state-license-states {:type :multy-select
                                       :label "US States Licence"
                                       :items #(<sub [:us-states-as-options])
                                       :visibility-depends-on :state-license}
                :federal-dea-active {:type :radio
                                     :label "Federal DEA active"
                                     :items {true "yes" false "no"}}
                :bls-acls-pals {:type :radio
                                :label "BLS/ACLS/PALS"
                                :items {true "yes" false "no" nil "N/A"}}
                :active-permanent-residence-card-or-visa {:type :radio
                                                          :label "Active Permanent Residence Card or visa"
                                                          :desc "if non-US citizen"
                                                          :items {true "yes" false "no" nil "N/A"}}
                :active-current-driver-license-or-passport {:type :radio
                                                            :label "Active current driver license or passport"
                                                            :items {true "yes" false "no"}}
                :active-npi-number {:type :radio
                                    :label "Active NPI number"
                                    :items {true "yes" false "no"}}
                :ecfmg {:type :radio
                        :label "ECFMG"
                        :desc "if applicable"
                        :items {true "yes" false "no" nil "N/A"}}
                :active-board-certificates {:type :radio
                                            :label "Active Board Certificates"
                                            :desc "if applicable"
                                            :items {true "yes" false "no" nil "N/A"}}
                :earliest-availability-for-shift {:type :input
                                                  :label "Earliest availability for a shift"}
                :preferences-for-work-location {:type :input
                                                :label "Any preference for work location"}
                :cv-link {:type :input
                          :label "Link to CV on any external resource"})})

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
         body (if (string? (:avatar body))
                (dissoc body :avatar)
                body)
         is-new-or-rejected (or (= state :new) (= state :rejected))
         url (if is-new-or-rejected
               (get-url db "/api/accounts/resident/" pk "/fill_profile/")
               (get-url db "/api/accounts/resident/" pk "/"))]
     {:json/fetch->path {:path [::resident-profile :profile-form :response]
                         :uri url
                         :headers {"Content-Type" "multipart/form-data"}
                         :method (if is-new-or-rejected "POST" "PATCH")
                         :token (<sub [:token])
                         :body (merge body {:timezone (get-timezone-str)})
                         :succeed-fx (fn [data]
                                       [:update-account-info
                                        (if is-new-or-rejected
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
