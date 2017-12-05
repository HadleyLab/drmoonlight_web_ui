(ns ui.resident.schedule.core
  (:require
   [reagent.core :as reagent]
   [ui.db.account :refer [is-approved is-profile-filled is-rejected]]
   [ui.db.misc :refer [>event dispatch-set! get-url <sub]]
   [ui.db.shift :refer [as-apply-date-time as-hours-interval]]
   [ui.pages :as pages]
   [ui.routes :refer [href]]
   [ui.widgets :refer [concatv]]
   [ui.widgets.calendar :refer [Calendar CalendarMonthControl]]
   [ui.resident.layout :refer [ResidentLayout]]
   [re-frame.core :as rf]
   [clojure.string :as str]
   [soda-ash.core :as sa]))

(defn ActionButton [pk state]
  [:div.shift__popup-footer
   (cond
     (= state "coverage_completed") [:p "The coverage for this shift is already completed"]
     (= state "completed") [:p "The shift is completed"]
     (is-approved) [sa/Button {:on-click (>event [:goto :resident :messages pk]) :fluid true :color "blue"} "Apply for the shift"]
     (is-profile-filled) [:p "Please wait until account manager approve your account"]
     (is-rejected) [:p "Your account was rejected, you can't apply for shifts"]
    ;; TODO hide button if resident has already apply for the shifts
    ;; https://gitlab.bro.engineering/drmoonlight/drmoonlight_api/issues/25
     :else [sa/Button {:on-click (>event [:goto :resident :profile]) :fluid true :color "blue"} "Fill profile to apply"])])

(defn get-date [date]
  (let [formatted-date (.format date "DD/MM/YYYY")
        hour (.format date "h")
        minute (.format date "mm")
        am-pm (.format date "a")]
    (str formatted-date " at " hour ":" minute " " am-pm)))

(defn ShiftLabel [{speciality :speciality
                   start :date-start
                   finish :date-end
                   state :state
                   pk :pk
                   payment-amount :payment-amount
                   payment-per-hour :payment-per-hour
                   description :description}]
  (let [speciality-name (:name (<sub [:speciality speciality]))]
    [:div [sa/Popup
           {:trigger (reagent/as-element [sa/Label {:color :blue} speciality-name])
            :position "left center"
            :offset 2
            :hoverable true
            :class-name "shift__popup"}
           [:div.shift__popup-content
            [:p.shift__row [:i "Starts: "] (get-date start)]
            [:p.shift__row [:i "Ends: "] (get-date finish)]
            [:p.shift__row [:i "Total: "] (as-hours-interval start finish) " hours"]
            [:p.shift__row [:i "Required staff: "] speciality-name]
            [:p.shift__row [:i "Payment amount: "] [:b "$" payment-amount] (str " per " (if payment-per-hour "hour" "shift"))]
            [:p.shift__row description]]
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
