(ns ui.resident.core
  (:require
   [reagent.core :as reagent]
   [ui.db.misc :refer [>event <sub get-url]]
   [ui.db.resident-profile :refer [resident-profile-form-fields
                                   resident-notification-form-fields]]
   [ui.pages :as pages]
   [ui.routes :refer [href]]
   [ui.change-password.core :refer [ChangePasswordForm]]
   [ui.widgets :refer [BuildForm]]
   [ui.widgets.error-message :refer [ErrorMessage]]
   [ui.resident.layout :refer [ResidentLayout ResidentProfileLayout]]
   [re-frame.core :as rf]
   [clojure.string :as str]
   [soda-ash.core :as sa]
   [ui.resident.schedule.core]
   [ui.resident.message]))

(defn ResidentNotificationForm []
  (rf/dispatch [:init-resident-notification-form])
  (let [resident-notification-form-cursor (<sub [:resident-notification-form-cursor])]
    (fn []
      (let [{status :status errors :errors} (<sub [:resident-notification-form-response])]
        [:div.profile__content._notifications
         [sa/Form {:class-name "moonlight-form-inner"}
          [BuildForm resident-notification-form-cursor resident-notification-form-fields]
          (when (= status :failure)
            [ErrorMessage {:errors errors}])
          [:div.form__group
           (if (= status :succeed)
             [sa/Message {:color "green"
                          :compact true
                          :header "Your notification settings has been successfully saved!"}])
           [sa/FormButton {:color :blue
                           :loading (= status :loading)
                           :on-click (>event [:update-resident-notifications])} "Save changes"]]]]))))

(defn ResidentProfileForm []
  (rf/dispatch [:init-resident-profile-form])
  (let [resident-profile-form-cursor (<sub [:resident-profile-form-cursor])]
    (fn []
      (let [{status :status errors :errors} (<sub [:resident-profile-form-response])
            email (<sub [:user-email])]
        [:div.profile__content._edit
         [sa/Form {:class-name "moonlight-form-inner"}
          [BuildForm resident-profile-form-cursor resident-profile-form-fields]
          [:div.form__group._account-settings
           [:label.form__group-title "Account settings"]
           [:div.field [:label "Email"] [:p email]]
           [sa/FormButton {:color :blue
                           :loading (= status :loading)
                           :class-name "profile__button"
                           :on-click (>event [:update-resident-profile])} "Save changes"]]]]))))

(pages/reg-page :core/resident (fn [] [ResidentLayout [sa/Header {} "index"]]))
(pages/reg-page :core/resident-profile (fn [] [ResidentProfileLayout [ResidentProfileForm]]))
(pages/reg-page :core/resident-profile-notification (fn [] [ResidentProfileLayout [ResidentNotificationForm]]))
(pages/reg-page :core/resident-profile-change-password (fn [] [ResidentProfileLayout [ChangePasswordForm]]))
