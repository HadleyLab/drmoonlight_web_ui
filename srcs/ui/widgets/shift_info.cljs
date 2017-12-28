(ns ui.widgets.shift-info
  (:require
   [ui.db.misc :refer [<sub >event format-date-time]]
   [ui.db.shift :refer [as-hours-interval]]
   [ui.db.application :refer [format-application-shift-date]]
   [soda-ash.core :as sa]))

(def shift-types [{:type "completed" :label "Completed shifts"}
                  {:type "active" :label "Active shifts"}
                  {:type "without_applies" :label "Shifts without applies"}
                  {:type "coverage_completed" :label "Coverage completed"}
                  {:type "require_approval" :label "Require approval"}
                  {:type "failed" :label "Failed shifts"}])

(defn ShiftsFilter []
  (let [shifts-count-by-state (<sub [:shifts-count-by-state])
        filter-state (<sub [:shifts-filter-state])]
    [:div
     (for [state shift-types]
       (let [type (:type state)
             label (:label state)]
         [:div {:key type :class-name "schedule__shifts-menu-item"}
          [:label {:class-name "schedule__shifts-menu-item-label"
                   :on-click (>event [:set-shifts-filter-state type])}
           [:span {:class-name (str "schedule__shifts-menu-item-color _" type)}]
           label " (" (get shifts-count-by-state type 0) ")"]
          [sa/Checkbox {:checked (= type filter-state)
                        :class-name "schedule__shifts-menu-item-checkbox"
                        :on-click (>event [:set-shifts-filter-state type])}]]))]))

(defn ShiftInfo [{speciality :speciality
                  start :date-start
                  finish :date-end
                  payment-amount :payment-amount
                  payment-per-hour :payment-per-hour
                  description :description
                  owner :owner}]
  (let [speciality-name (:name (<sub [:speciality speciality]))
        user-type (<sub [:user-type])]
    [:div.shift__popup-content
     (if (= user-type :resident)
       [:p.shift__row [:i "Hospital / Facility name: "] (:facility-name owner)])
     [:p.shift__row [:i "Starts: "] (format-date-time start)]
     [:p.shift__row [:i "Ends: "] (format-date-time finish)]
     [:p.shift__row [:i "Total: "] (as-hours-interval start finish) " hours"]
     [:p.shift__row [:i "Required staff: "] speciality-name]
     [:p.shift__row [:i "Payment amount: "] [:b "$" payment-amount] (str " per " (if payment-per-hour "hour" "shift"))]
     [:p.shift__row description]]))

(defn get-short-shift-info [{speciality :speciality
                             start :date-start
                             finish :date-end}]
  (str (:name (<sub [:speciality speciality])) (format-application-shift-date start finish)))
