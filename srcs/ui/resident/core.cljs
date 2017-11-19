(ns ui.resident.core
  (:require
   [reagent.core :as reagent]
   [ui.pages :as pages]
   [ui.routes :refer [href]]
   [ui.widgets :refer [build-form fields->schema]]
   [ui.resident.layout :refer [resident-layout resident-profile-layout]]
   [re-frame.core :as rf]
   [clojure.string :as str]
   [sodium.core :as na]
   [ui.resident.schedule.core]))

(def root-path :resident)

(def resident-profile-form-fields
  {"Personal Information" {:firstName "First Name"
                           :lastName "Last Name"}
   "Reidency" {:residencyProgram "Residency Program"
               :specialities "Specialities"
               :residencyYear "Residency Year"
               :stateLicence {:type :radio :label "State Licence" :items {"Yes" true "No" false}}
               :boardScore {:type :radio :label "Board Score" :items {"Yes" true "No" false}}}})

(def resident-notification-form-fields
  {"I would like to recieve emails when:"
   {:notificationNewShifts {:type :toggle :label "New shift is created"}
    :notificationApplicationStatusChanging  {:type :toggle :label "My application is approved/rejected"}
    :notificationNewMessages {:type :toggle :label "New message from a scheduler is received"}}})

(def schema
  {:profile-form
   {:fields (fields->schema resident-profile-form-fields)
    :response {:status :not-asked}}
   :notification-form
   {:fields (fields->schema resident-notification-form-fields)
    :response {:status :not-asked}}})

(defn resident-notification-form []
  (let [resident-page-cursor @(rf/subscribe [:cursor [root-path]])
        resident-notification-form-cursor (reagent/cursor resident-page-cursor [:notification-form])]
    (fn []
      [na/form {:class-name "moonlight-inner-form"}
       [build-form resident-notification-form-cursor resident-notification-form-fields]
       [:div.moonlight-form-group
        [na/form-button {:content "Save changes"
                         :color :blue
                         :on-click (na/>event [:udpate-resident-profile])}]]])))

(defn resident-profile-form []
  (let [resident-page-cursor @(rf/subscribe [:cursor [root-path]])
        resident-profile-form-cursor (reagent/cursor resident-page-cursor [:profile-form])]
    (fn []
      [na/form {:class-name "moonlight-inner-form"}
       [build-form resident-profile-form-cursor resident-profile-form-fields]
       [:div [:label "Account settings"] [:div.field [:label "Email"] [:p "test@me.com"]]]
       [:div.moonlight-form-group
        [na/form-button {:content "Save changes"
                         :color :blue
                         :on-click (na/>event [:udpate-resident-profile])}]]])))


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

(pages/reg-page :core/resident (with-init (fn [] [resident-layout [na/header {} "index"]])))
(pages/reg-page :core/resident-statistics (with-init (fn [] [resident-layout [na/header {} "statistics"]])))
(pages/reg-page :core/resident-messages (with-init (fn [] [resident-layout [na/header {} "messages"]])))
(pages/reg-page :core/resident-profile (with-init (fn [] [resident-profile-layout [resident-profile-form]])))
(pages/reg-page :core/resident-profile-notification (with-init (fn [] [resident-profile-layout [resident-notification-form]])))
