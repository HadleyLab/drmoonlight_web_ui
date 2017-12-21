(ns ui.widgets.shift-info
  (:require
   [ui.db.misc :refer [<sub format-date-time]]
   [ui.db.shift :refer [as-hours-interval]]))

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
