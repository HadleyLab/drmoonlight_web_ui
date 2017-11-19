(ns ui.widgets.calendar
  (:require
   [reagent.core :as reagent]
   [ui.pages :as pages]
   [ui.routes :refer [href]]
   [ui.widgets :refer [concatv]]
   [ui.resident.layout :refer [resident-layout]]
   [re-frame.core :as rf]
   [clojure.string :as str]
   [soda-ash.core :as sa]
   [sodium.core :as na]
   [cljs-time.core :as dt]
   [cljs-time.format :as format]))

(defn- as-key [date]
  (format/unparse (format/formatter "yyyy-MM-dd") date))

(defn- get-shifts-for-day [shifts day]
  (get-in shifts [:data (as-key day)] []))

(defn- draw-cell [render-shift-label row column selected shifts]
  (let [month-start (dt/day-of-week selected)
        index (+ (* row 7) column)
        day (+ (- index month-start) 1)
        prev-month (dt/minus selected (dt/months 1))
        next-month (dt/plus selected (dt/months 1))
        selected-month-max-day (dt/day (dt/last-day-of-the-month selected))
        prev-month-max-day (dt/day (dt/last-day-of-the-month prev-month))
        cell-date (cond
                    (<= day 0) (dt/date-time
                                (dt/year prev-month)
                                (dt/month prev-month)
                                (+ prev-month-max-day day))
                    (> day selected-month-max-day) (dt/date-time
                                                    (dt/year next-month)
                                                    (dt/month next-month)
                                                    (- day selected-month-max-day))
                    :else (dt/date-time (dt/year selected)
                                        (dt/month selected)
                                        day))]
    (concatv [:div [:p (dt/day cell-date)]] (mapv render-shift-label (get-shifts-for-day shifts cell-date)))))

(defn calendar [selected shifts render-shift-label]
  (fn [selected shifts]
    [sa/Table {:celled true}
     [sa/TableHeader
      [sa/TableRow
       [sa/TableHeaderCell "Sunday"]
       [sa/TableHeaderCell "Monday"]
       [sa/TableHeaderCell "Tuesday"]
       [sa/TableHeaderCell "Wednesday"]
       [sa/TableHeaderCell "Trusday"]
       [sa/TableHeaderCell "Frayday"]
       [sa/TableHeaderCell "Saturday"]]]
     (concatv [sa/TableBody]
              (mapv (fn [row]
                      (concatv [sa/TableRow]
                               (mapv (fn [column]
                                       [sa/TableCell [draw-cell render-shift-label row column selected shifts]])
                                     (range 7)))) (range 5)))]))
