(ns ui.resident.core
  (:require
   [reagent.core :as reagent]
   [ui.db.misc :refer [>event <sub get-url]]
   [ui.db.resident-profile :refer [resident-profile-form-fields
                                   resident-notification-form-fields
                                   resident-change-password-form-fields]]
   [ui.pages :as pages]
   [ui.routes :refer [href]]
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

(defn ResidentChangePasswordForm []
  (rf/dispatch [:init-resident-change-password-form])
  (let [resident-change-password-form-cursor (<sub [:resident-change-password-form-cursor])]
    (fn []
      (let [{status :status errors :errors} (<sub [:resident-change-password-form-response])]
        [:div.profile__content
         [sa/Form {:class-name "moonlight-form-inner"}
          [BuildForm resident-change-password-form-cursor resident-change-password-form-fields]
          [:div.form__group
           [sa/FormButton {:color :blue
                           :loading (= status :loading)
                           :class-name "profile__button"
                           :on-click (.log js/console "Change password")} "Save changes"]]]]))))

(pages/reg-page :core/resident (fn [] [ResidentLayout [sa/Header {} "index"]]))
(pages/reg-page :core/resident-statistics (fn [] [ResidentLayout [sa/Header {} "statistics"]]))
(pages/reg-page :core/resident-profile (fn [] [ResidentProfileLayout [ResidentProfileForm]]))
(pages/reg-page :core/resident-profile-notification (fn [] [ResidentProfileLayout [ResidentNotificationForm]]))
(pages/reg-page :core/resident-profile-change-password (fn [] [ResidentProfileLayout [ResidentChangePasswordForm]]))
