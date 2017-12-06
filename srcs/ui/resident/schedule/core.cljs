(ns ui.resident.schedule.core
  (:require
   [reagent.core :as reagent]
   [ui.db.account :refer [is-approved is-profile-filled is-rejected]]
   [ui.db.misc :refer [>event <sub]]
   [ui.pages :as pages]
   [ui.widgets.calendar :refer [Calendar CalendarMonthControl]]
   [ui.widgets.shift-info :refer [ShiftInfo]]
   [ui.resident.layout :refer [ResidentLayout]]
   [re-frame.core :as rf]
   [soda-ash.core :as sa]))

(defn ActionButton [pk state]
  [:div.shift__popup-footer._resident
   (cond
     (= state "coverage_completed") [:p "The coverage for this shift is already completed"]
     (= state "completed") [:p "The shift is completed"]
     (is-approved) [sa/Button {:on-click (>event [:goto :resident :messages pk]) :fluid true :color "blue"} "Apply for the shift"]
     (is-profile-filled) [:p "Please wait until account manager approve your account"]
     (is-rejected) [:p "Your account was rejected, you can't apply for shifts"]
    ;; TODO hide button if resident has already apply for the shifts
    ;; https://gitlab.bro.engineering/drmoonlight/drmoonlight_api/issues/25
     :else [sa/Button {:on-click (>event [:goto :resident :profile]) :fluid true :color "blue"} "Fill profile to apply"])])

(defn ShiftLabel [params]
  (let [pk (:pk params)
        state (:state params)
        speciality (:speciality params)
        speciality-name (:name (<sub [:speciality speciality]))]
    [:div [sa/Popup
           {:trigger (reagent/as-element [:div {:class-name (str "shift__label _" state)}
                                          speciality-name])
            :position "left center"
            :offset 2
            :hoverable true
            :class-name "shift__popup"}
           [ShiftInfo params]
           [ActionButton pk state]]]))

(defn Index [params]
  (rf/dispatch [:load-shifts])
  (fn [params]
    (let [calendar-month (<sub [:calendar-month])]
      [ResidentLayout
       [sa/Grid {}
        [sa/GridRow {}
         [sa/GridColumn {:width 6}]
         [CalendarMonthControl [sa/GridColumn {:width 4}]]
         [sa/GridColumn {:width 6}]]
        [sa/GridRow {}
         [sa/GridColumn {:width 16}
          [Calendar calendar-month (<sub [:shifts]) ShiftLabel]]]]])))

(pages/reg-page :core/resident-schedule Index)
