(ns ui.scheduler.core
  (:require
   [reagent.core :as reagent]
   [ui.pages :as pages]
   [ui.routes :refer [href]]
   [ui.db.misc :refer [<sub >event]]
   [ui.change-password.core :refer [ChangePasswordForm]]
   [ui.scheduler.layout :refer [SchedulerLayout SchedulerProfileLayout]]
   [ui.db.scheduler-profile :refer [scheduler-profile-form-fields]]
   [re-frame.core :as rf]
   [clojure.string :as str]
   [soda-ash.core :as sa]
   [ui.widgets :refer [BuildForm]]
   [ui.scheduler.schedule.core]
   [ui.widgets.resident-profile-detail :refer [ResidentProfileDetail]]
   [ui.scheduler.message]))


  ;; var formData = new FormData(document.forms.person);
  ;; // добавить к пересылке ещё пару ключ - значение
  ;; formData.append("patronym", "Робертович");


  ;; (defn generate-form-data [params]
  ;;   (let [form-data (js/FormData.)]
  ;;     (doseq [[k v] params]
  ;;       (.append form-data (name k) v))
  ;;     form-data))

(defn SchedulerProfileForm []
  (rf/dispatch [:init-scheduler-profile-form])
  (let [scheduler-profile-form-cursor (<sub [:scheduler-profile-form-cursor])]
    (fn []
      (let [{status :status errors :errors} (<sub [:scheduler-profile-form-response])
            email (<sub [:user-email])]
        [:div.profile__content._edit
         [sa/Form {:class-name "moonlight-form-inner"}
          [BuildForm scheduler-profile-form-cursor scheduler-profile-form-fields]
          [:div.form__group._account-settings
           [:label.form__group-title "Account settings"]
           [:div.field [:label "Email"] [:p email]]
           [sa/FormButton {:color :blue
                           :loading (= status :loading)
                           :class-name "profile__button"
                           :on-click (>event [:update-scheduler-profile])} "Save changes"]]]]))))

(defn WrappedResidentProfileDetail [params]
  [SchedulerLayout [ResidentProfileDetail params]])

(pages/reg-page :core/scheduler (fn [] [SchedulerLayout [sa/Header {} "index"]]))
(pages/reg-page :core/scheduler-profile (fn [] [SchedulerProfileLayout [SchedulerProfileForm]]))
(pages/reg-page :core/scheduler-profile-change-password (fn [] [SchedulerProfileLayout [ChangePasswordForm]]))
(pages/reg-page :scheduler/resident-profile-detail WrappedResidentProfileDetail)
