(ns ui.resident.core
  (:require
   [reagent.core :as reagent]
   [ui.db :refer [>event <sub get-url]]
   [ui.pages :as pages]
   [ui.routes :refer [href]]
   [ui.widgets :refer [BuildForm fields->schema setup-form-initial-values]]
   [ui.resident.layout :refer [ResidentLayout ResidentProfileLayout]]
   [re-frame.core :as rf]
   [clojure.string :as str]
   [soda-ash.core :as sa]
   [ui.resident.schedule.core]))

(def root-path :resident)

(def resident-profile-form-fields
  {"Personal Information" {:first-name "First Name"
                           :last-name "Last Name"}
   "Reidency" {:residency-program {:type :select
                                   :label "Residency Program"
                                   :items #(<sub [:residency-program-as-options])}
               :specialities {:type :multy-select
                              :label "Specialities"
                              :items #(<sub [:speciality-as-options])}
               :residency-years "Residency Years"
               :state-license {:type :radio
                               :label "State Licence"
                               :items {"Yes" true "No" false}}
               :board-score {:type :radio
                             :label "Board Score"
                             :items {"Yes" true "No" false}}}})

(def resident-notification-form-fields
  {"I would like to recieve emails when:"
   {:notification-new-shifts {:type :toggle :label "New shift is created"}
    :notification-application-status-changing  {:type :toggle :label "My application is approved/rejected"}
    :notification-new-messages {:type :toggle :label "New message from a scheduler is received"}}})

(def schema
  {:profile-form
   {:fields (fields->schema resident-profile-form-fields)
    :response {:status :not-asked}}
   :notification-form
   {:fields (fields->schema resident-notification-form-fields)
    :response {:status :not-asked}}})

(defn ResidentNotificationForm []
  (let [resident-page-cursor @(rf/subscribe [:cursor [root-path]])
        resident-notification-form-cursor (reagent/cursor resident-page-cursor [:notification-form])]
    (fn []
      [sa/Form {:class-name "moonlight-form-inner"}
       [BuildForm resident-notification-form-cursor resident-notification-form-fields]
       [:div.moonlight-form-group
        [sa/FormButton {:color :blue
                        :on-click (>event [:udpate-resident-profile])} "Save changes"]]])))

(defn ResidentProfileForm []
  (let [resident-page-cursor @(rf/subscribe [:cursor [root-path]])
        resident-profile-form-cursor (reagent/cursor resident-page-cursor [:profile-form])
        status-cursor (reagent/cursor resident-profile-form-cursor [:response :status])
        email (get-in (<sub [:account]) [:user-info :email])]
    (fn []
      [sa/Form {:class-name "moonlight-form-inner"}
       [BuildForm resident-profile-form-cursor resident-profile-form-fields]
       [:div [:label "Account settings"] [:div.field [:label "Email"] [:p email]]]
       [:div.moonlight-form-group
        [sa/FormButton {:color :blue
                        :loading (= @status-cursor :loading)
                        :on-click (>event [:update-resident-profile])} "Save changes"]]])))

(defn with-init [render]
  (fn []
    (rf/dispatch-sync [::init-resident-page])
    render))

(rf/reg-event-db
 ::init-resident-page
 (fn [db _]
   (-> db
       (assoc-in [root-path] schema)
       (update-in [root-path :profile-form :fields] (setup-form-initial-values (:user-info (<sub [:account]))))
       (update-in [root-path :notification-form :fields] (setup-form-initial-values (:user-info (<sub [:account])))))))

(rf/reg-event-fx
 :update-resident-profile
 (fn [{db :db} _]
   (let [pk (get-in db [:account :user-info :id])
         token (get-in db [:account :token])
         body (get-in db [root-path :profile-form :fields])
         state (get-in db [:account :user-info :state])
         url (if (= state 1)
               (get-url db "/api/accounts/resident/" pk "/fill_profile/")
               (get-url db "/api/accounts/resident/" pk "/"))]
     {:db (assoc-in db [root-path :profile-form :response :status] :loading)
      :json/fetch {:uri url
                   :method (if (= state 1) "POST" "PATCH")
                   :token token
                   :body body
                   :success (merge
                             {:event :update-resident-profile-succeed}
                             (when (= state 1)
                               {:data (merge body {:state 2})}))
                   :error {:event :update-resident-profile-failure}}})))

(rf/reg-event-db
 :update-resident-profile-succeed
 (fn [db [_ {data :data}]]
   (-> db
       (assoc-in [root-path :profile-form :response] {:status :succeed :data data})
       (update-in [:account :user-info] #(merge % data)))))

(rf/reg-event-db
 :update-resident-profile-failure
 (fn [db [_ {data :data}]]
   (assoc-in db [root-path :profile-form :response] {:status :failure :errors data})))

(pages/reg-page :core/resident (with-init (fn [] [ResidentLayout [sa/Header {} "index"]])))
(pages/reg-page :core/resident-statistics (with-init (fn [] [ResidentLayout [sa/Header {} "statistics"]])))
(pages/reg-page :core/resident-messages (with-init (fn [] [ResidentLayout [sa/Header {} "messages"]])))
(pages/reg-page :core/resident-profile (with-init (fn [] [ResidentProfileLayout [ResidentProfileForm]])))
(pages/reg-page :core/resident-profile-notification (with-init (fn [] [ResidentProfileLayout [ResidentNotificationForm]])))
