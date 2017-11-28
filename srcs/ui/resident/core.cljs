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
   [ui.resident.schedule.core]
   [ui.resident.message]))

(def root-path :resident)

(def resident-profile-form-fields
  {"Personal Information" {:first-name "First Name"
                           :last-name "Last Name"}
   "Residency" {:residency-program {:type :select
                                    :label "Residency Program"
                                    :items #(<sub [:residency-program-as-options])}
                :specialities {:type :multy-select
                               :label "Specialities"
                               :items #(<sub [:speciality-as-options])}
                :residency-years "Residency Years"
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
      [:div {:class-name "profile__content-inner _notifications"}
       [sa/Form {:class-name "moonlight-form-inner"}
        [BuildForm resident-notification-form-cursor resident-notification-form-fields]
        [:div.moonlight-form-group
         [sa/FormButton {:color :blue
                         :on-click (>event [:udpate-resident-profile])} "Save changes"]]]])))

(defn ResidentProfileForm []
  (let [resident-page-cursor @(rf/subscribe [:cursor [root-path]])
        resident-profile-form-cursor (reagent/cursor resident-page-cursor [:profile-form])
        status-cursor (reagent/cursor resident-profile-form-cursor [:response :status])
        email (<sub [:user-email])]
    (fn []
      [:div {:class-name "profile__content-inner"}
       [sa/Form {:class-name "moonlight-form-inner"}
        [BuildForm resident-profile-form-cursor resident-profile-form-fields]
        [sa/Divider]
        [:div [:label {:class-name "form__group-title"} "Account settings"]
         [:div.field [:label "Email"] [:p email]]]
        [:div.moonlight-form-group
         [sa/FormButton {:color :blue
                         :loading (= @status-cursor :loading)
                         :on-click (>event [:update-resident-profile])} "Save changes"]]]])))

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
   (let [pk (<sub [:user-id])
         token (<sub [:token])
         body (get-in db [root-path :profile-form :fields])
         state (<sub [:user-state])
         url (if (= state 1)
               (get-url db "/api/accounts/resident/" pk "/fill_profile/")
               (get-url db "/api/accounts/resident/" pk "/"))]
     {:json/fetch->path {:path [root-path :profile-form :response]
                         :uri url
                         :method (if (= state 1) "POST" "PATCH")
                         :token token
                         :body body
                         :succeed-fx
                         (fn [data]
                           [:update-resident-profile-succeed
                            (if (= state 1)
                              (merge body {:state 2})
                              data)])}})))
(rf/reg-event-db
 :update-resident-profile-succeed
 (fn [db [_ data]]
   (update-in db [:account :user-info] #(merge % data))))

(pages/reg-page :core/resident (with-init (fn [] [ResidentLayout [sa/Header {} "index"]])))
(pages/reg-page :core/resident-statistics (with-init (fn [] [ResidentLayout [sa/Header {} "statistics"]])))
(pages/reg-page :core/resident-profile (with-init (fn [] [ResidentProfileLayout [ResidentProfileForm]])))
(pages/reg-page :core/resident-profile-notification (with-init (fn [] [ResidentProfileLayout [ResidentNotificationForm]])))
