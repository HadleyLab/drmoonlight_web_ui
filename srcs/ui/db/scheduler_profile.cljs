(ns ui.db.scheduler-profile
  (:require
   [re-frame.core :as rf]
   [ui.db.misc :refer [get-url <sub fields->schema
                       setup-form-initial-values get-timezone-str]]))

(def scheduler-profile-form-fields
  {"Personal Information" {:first-name {:type :input
                                        :label "First Name"}
                           :last-name {:type :input
                                       :label "Last Name"}
                           :avatar {:type :avatar
                                    :label "Avatar"}}
   "" (array-map
                :facility-name {:type :input
                                :label "Hospital / Facility name"}
                :department-name {:type :input
                                  :label "Department name"})})

(def schema
  {::scheduler-profile
   {:profile-form
    {:fields
     (fields->schema scheduler-profile-form-fields)
     :response {:status :not-asked}}}})

(rf/reg-sub
 ::scheduler-profile
 (fn [db path]
   (get-in db path)))

(rf/reg-event-db
 :init-scheduler-profile-form
 (fn [db _]
   (-> db
       (assoc-in [::scheduler-profile :profile-form]
                 (get-in schema [::scheduler-profile :profile-form]))
       (update-in [::scheduler-profile :profile-form :fields]
                  (setup-form-initial-values (<sub [:user-info]))))))

(rf/reg-sub
 :scheduler-profile-form-cursor
 #(<sub [:cursor [::scheduler-profile :profile-form]]))

(rf/reg-sub
 :scheduler-profile-form-response
 #(<sub [::scheduler-profile :profile-form :response]))

 (defn generate-form-data [params]
   (let [form-data (js/FormData.)]
     (doseq [[k v] params]
       (.append form-data (name k) v))
     form-data))

(rf/reg-event-fx
 :update-scheduler-profile
 (fn [{db :db} [_ type]]
   (let [pk (<sub [:user-id])
         state (<sub [:user-state])
         body (<sub [::scheduler-profile :profile-form :fields])
         url (get-url db "/api/accounts/scheduler/" pk "/")]
     {:json/fetch->path {:path [::scheduler-profile :profile-form :response]
                         :uri url
                         :method (if (= state 1) "POST" "PATCH")
                         :token (<sub [:token])
                         :body generate-form-data (merge body {:timezone (get-timezone-str)})
                         :succeed-fx (fn [data]
                                       [:update-account-info
                                        (if (= state 1)
                                          (merge body {:state 2})
                                          data)])}})))
