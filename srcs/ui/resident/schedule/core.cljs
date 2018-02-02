(ns ui.resident.schedule.core
  (:require
   [reagent.core :as reagent]
   [ui.db.account :refer [is-approved is-profile-filled is-rejected]]
   [ui.db.resident :refer [shift-types]]
   [ui.db.misc :refer [>event <sub]]
   [ui.db.shift :refer [get-shift-type]]
   [ui.pages :as pages]
   [ui.widgets.calendar :refer [Calendar CalendarMonthControl]]
   [ui.widgets.shift-info :refer [ShiftInfo ShiftsFilter]]
   [ui.resident.layout :refer [ResidentLayout]]
   [re-frame.core :as rf]
   [soda-ash.core :as sa]))

(defn ActionButton [pk state has-already-applied was-rejected]
  [:div.shift__popup-footer._resident
   (cond
     was-rejected [:p "Your request has been rejected for the shift"]
     (and (true? has-already-applied) (= state "coverage_completed")) [:p "You have been approved for the shift"]
     (= state "coverage_completed") [:p "The coverage for this shift is already completed"]
     (= state "completed") [:p "The shift is completed"]
     (= state "active") [:p "The shift is active"]
     (= state "failed") [:p "The shift is failed"]
     (true? has-already-applied) [:p "You have already applied for this shift"]
     (is-approved) [sa/Button {:on-click (>event [:goto :resident :messages pk]) :fluid true :color "blue"} "Apply for the shift"]
     (is-profile-filled) [:p "Please wait until account manager approve your account"]
     (is-rejected) [:p "Your account was rejected, you can't apply for shifts"]
     :else [sa/Button {:on-click (>event [:goto :resident :profile]) :fluid true :color "blue"} "Fill profile to apply"])])

(defn ShiftLabel [params draw-data]
  (let [hovered (reagent/atom false)]
    (fn [params draw-data]
      (let [pk (:pk params)
            state (:state params)
            was-rejected (:was-rejected params)
            type (get-shift-type params shift-types)
            has-already-applied (:has-already-applied params)
            speciality (:speciality params)
            speciality-name (:name (<sub [:speciality speciality]))
            {:keys [starts-on-prev-week ends-on-next-week]} draw-data
            opened (<sub [:edit-shift-popup pk])]
        [:div [sa/Popup
               {:trigger (reagent/as-element [:div {:class-name (str
                                                                 "shift__label _" type
                                                                 (when starts-on-prev-week " _starts-on-prev-week")
                                                                 (when ends-on-next-week " _ends-on-next-week")
                                                                 (when opened " _hovered"))}
                                              speciality-name
                                              (if has-already-applied [sa/Icon {:name "checkmark"}])])
                :open @hovered
                :on-open #(reset! hovered true)
                :on-close #(reset! hovered false)
                :position "left center"
                :offset 2
                :hoverable true
                :class-name "shift__popup"}
               [ShiftInfo params]
               [ActionButton pk state has-already-applied was-rejected]]]))))

(defn Index [params]
  (rf/dispatch [:load-shifts])
  (fn [params]
    (let [calendar-month (<sub [:calendar-month])]
      [ResidentLayout
       [sa/Grid {}
        [sa/GridRow {}
         [sa/GridColumn {:width 3}]
         [CalendarMonthControl [sa/GridColumn {:width 13}]]]
        [sa/GridRow {}
         [sa/GridColumn {:width 3}
          [ShiftsFilter shift-types]]
         [sa/GridColumn {:width 13}
          [Calendar calendar-month ShiftLabel]]]]])))

(pages/reg-page :core/resident-schedule Index)
