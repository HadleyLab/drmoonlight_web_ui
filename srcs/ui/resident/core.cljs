(ns ui.resident.core
  (:require
   [reagent.core :as reagent]
   [ui.db :refer [>event]]
   [ui.pages :as pages]
   [ui.routes :refer [href]]
   [ui.widgets :refer [BuildForm fields->schema]]
   [ui.resident.layout :refer [ResidentLayout ResidentProfileLayout]]
   [re-frame.core :as rf]
   [clojure.string :as str]
   [soda-ash.core :as sa]
   [ui.resident.schedule.core]))

(def root-path :resident)

(def resident-profile-form-fields
  {"Personal Information" {:first-name "First Name"
                           :last-name "Last Name"}
   "Reidency" {:residency-program "Residency Program"
               :specialities "Specialities"
               :residency-year "Residency Year"
               :state-licence {:type :radio :label "State Licence" :items {"Yes" true "No" false}}
               :board-score {:type :radio :label "Board Score" :items {"Yes" true "No" false}}}})

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
        resident-profile-form-cursor (reagent/cursor resident-page-cursor [:profile-form])]
    (fn []
      [sa/Form {:class-name "moonlight-form-inner"}
       [BuildForm resident-profile-form-cursor resident-profile-form-fields]
       [:div [:label "Account settings"] [:div.field [:label "Email"] [:p "test@me.com"]]]
       [:div.moonlight-form-group
        [sa/FormButton {:color :blue
                        :on-click (>event [:udpate-resident-profile])} "save changes"]]])))

(defn with-init [render]
  (fn []
    (rf/dispatch-sync [::init-resident-page])
    render))

(rf/reg-event-db
 ::init-resident-page
 (fn [db [_]]
   (if (= (root-path db) nil)
     (assoc-in db [root-path] schema)
     db)))

(pages/reg-page :core/resident (with-init (fn [] [ResidentLayout [sa/Header {} "index"]])))
(pages/reg-page :core/resident-statistics (with-init (fn [] [ResidentLayout [sa/Header {} "statistics"]])))
(pages/reg-page :core/resident-messages (with-init (fn [] [ResidentLayout [sa/Header {} "messages"]])))
(pages/reg-page :core/resident-profile (with-init (fn [] [ResidentProfileLayout [ResidentProfileForm]])))
(pages/reg-page :core/resident-profile-notification (with-init (fn [] [ResidentProfileLayout [ResidentNotificationForm]])))
