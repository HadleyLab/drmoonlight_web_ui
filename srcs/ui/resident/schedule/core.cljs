(ns ui.resident.schedule.core
  (:require
   [reagent.core :as reagent]
   [ui.db.account :refer [is-approved is-profile-filled is-rejected]]
   [ui.db.misc :refer [>event dispatch-set! get-url <sub]]
   [ui.db.shift :refer [as-apply-date-time as-hours-interval]]
   [ui.pages :as pages]
   [ui.routes :refer [href]]
   [ui.widgets :refer [concatv]]
   [ui.widgets.calendar :refer [Calendar]]
   [ui.resident.layout :refer [ResidentLayout]]
   [re-frame.core :as rf]
   [clojure.string :as str]
   [soda-ash.core :as sa]
   [cljs-time.core :as dt]
   [cljs-time.format :as format]))

(defn ActionButton [pk]
  (cond
    (is-approved) [sa/Button {:on-click (>event [:goto :resident :messages pk])} "Apply"]
    (is-profile-filled) [:p "Please wait until account manager approve your account"]
    (is-rejected) [:p "Your account was rejected, you can't apply for shifts"]
    :else [sa/Button {:on-click (>event [:goto :resident :profile])} "Fill profile to apply"]))

(defn ShiftLabel [{speciality :speciality
                   start :date-start
                   finish :date-end
                   pk :pk
                   payment-amount :payment-amount
                   payment-per-hour :payment-per-hour
                   description :description}]
  (let [speciality-name (:name (<sub [:speciality speciality]))]
    [:div [sa/Popup
           {:trigger (reagent/as-element [sa/Label {:color :blue} speciality-name])
            :flowing true
            :position "left center"
            :hoverable true}
           [:div
            [:p [:strong "Starts: "] (as-apply-date-time start)]
            [:p [:strong "Ends: "] (as-apply-date-time finish)]
            [:p [:strong "Total: "] (as-hours-interval start finish) " hours"]
            [:p [:strong "Required staff: "] speciality-name]
            [:p [:strong "Payment amount: "] (str "$" payment-amount " per " (if payment-per-hour "hour" "shift"))]
            ;; [:p description] TODO fix max width
            [ActionButton pk]]] [:br] [:br]]))

(defn Index [params]
  (rf/dispatch [:load-shifts])
  (let [calendar-month (<sub [:calendar-month])]
    (fn [params]
      [ResidentLayout
       [sa/Grid {}
        [sa/GridRow {}
         [sa/GridColumn {:width 6}]
         [sa/GridColumn {:width 4}
          [sa/Button {:icon "angle left"
                      :on-click (>event [:set-calendar-month (dt/minus calendar-month (dt/months 1))])}]
          [:span (format/unparse (format/formatter "MMMM YYYY") calendar-month)]
          [sa/Button {:icon "angle right"
                      :on-click (>event [:set-calendar-month (dt/plus calendar-month (dt/months 1))])}]
          [sa/GridColumn {:width 6}]]]
        [sa/GridRow {}
         [sa/GridColumn {:width 16}
          [Calendar calendar-month (<sub [:shifts]) ShiftLabel]]]]])))

(pages/reg-page :core/resident-schedule Index)
