(ns ui.resident.schedule.core
  (:require
   [reagent.core :as reagent]
   [ui.db.account :refer [is-approved is-profile-filled is-rejected]]
   [ui.db.misc :refer [>event <sub]]
   [ui.pages :as pages]
   [ui.widgets.calendar :refer [Calendar CalendarMonthControl]]
   [ui.widgets.shift-info :refer [ShiftInfo ShiftsFilter]]
   [ui.resident.layout :refer [ResidentLayout]]
   [re-frame.core :as rf]
   [soda-ash.core :as sa]))

(defn ActionButton [pk state has-already-applied]
  [:div.shift__popup-footer._resident
   (cond
     (and (true? has-already-applied)(= state "coverage_completed")) [:p "You have been approved for the shift"]
     (= state "coverage_completed") [:p "The coverage for this shift is already completed"]
     (= state "completed") [:p "The shift is completed"]
     (= state "active") [:p "The shift is active"]
     (= state "failed") [:p "The shift is failed"]
     (true? has-already-applied) [:p "You have already applied for this shift"]
     (is-approved) [sa/Button {:on-click (>event [:goto :resident :messages pk]) :fluid true :color "blue"} "Apply for the shift"]
     (is-profile-filled) [:p "Please wait until account manager approve your account"]
     (is-rejected) [:p "Your account was rejected, you can't apply for shifts"]
     :else [sa/Button {:on-click (>event [:goto :resident :profile]) :fluid true :color "blue"} "Fill profile to apply"])])

(defn ShiftLabel [params]
  (let [pk (:pk params)
        state (:state params)
        has-already-applied (:has-already-applied params)
        speciality (:speciality params)
        speciality-name (:name (<sub [:speciality speciality]))]
    [:div [sa/Popup
           {:trigger (reagent/as-element [:div {:class-name (str "shift__label _" state)}
                                          speciality-name
                                          (if has-already-applied [sa/Icon {:name "checkmark"}])])
            :position "left center"
            :offset 2
            :hoverable true
            :class-name "shift__popup"}
           [ShiftInfo params]
           [ActionButton pk state has-already-applied]]]))

(defn Index [params]
  (rf/dispatch [:load-shifts])
  (fn [params]
    (let [calendar-month (<sub [:calendar-month])
          filter-state (<sub [:shifts-filter-state])
          filtered-shifts (<sub [:shifts-filtered-by-state filter-state])]
      [ResidentLayout
       [sa/Grid {}
        [sa/GridRow {}
         [sa/GridColumn {:width 3}]
         [CalendarMonthControl [sa/GridColumn {:width 13}]]]
        [sa/GridRow {}
         [sa/GridColumn {:width 3}
          [ShiftsFilter]]
         [sa/GridColumn {:width 13}
          [Calendar calendar-month filtered-shifts ShiftLabel]]]]])))

(pages/reg-page :core/resident-schedule Index)
