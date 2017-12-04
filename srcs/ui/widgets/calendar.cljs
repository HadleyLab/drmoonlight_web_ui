(ns ui.widgets.calendar
  (:require
   [reagent.core :as reagent]
   [ui.db.misc :refer (>event <sub)]
   [ui.pages :as pages]
   [ui.routes :refer [href]]
   [ui.widgets :refer [concatv]]
   [re-frame.core :as rf]
   [clojure.string :as str]
   [soda-ash.core :as sa]))

(defn- as-key [date]
  (.format date "YYYY-MM-DD"))

(defn- get-shifts-for-day [shifts day]
  (get-in shifts [:data (as-key day)] []))

(defn- draw-cell [render-shift-label row column selected shifts]
  (let [month-start (.weekday selected)
        index (+ (* row 7) column)
        day (+ (- index month-start) 1)
        prev-month (.subtract (js/moment selected) 1 "month")
        next-month (.add (js/moment selected) 1 "month")
        selected-month-max-day (-> selected
                                   (js/moment)
                                   (.endOf "month")
                                   (.date))
        prev-month-max-day        (-> prev-month
                                      (js/moment)
                                      (.endOf "month")
                                      (.date))
        cell-date (cond
                    (<= day 0) (js/moment (str (.format prev-month "YYYY-MM-")
                                               (+ prev-month-max-day day)))
                    (> day selected-month-max-day) (js/moment (str (.format next-month "YYYY-MM-")
                                                                   (- day selected-month-max-day)))
                    :else (js/moment (str (.format selected "YYYY-MM-") day)))]
    (concatv [:div [:p (.date cell-date)]]
             (mapv render-shift-label (get-shifts-for-day shifts cell-date)))))

(defn Calendar [selected shifts render-shift-label]
  (fn [selected shifts]
    [sa/Table {:celled true}
     [sa/TableHeader
      [sa/TableRow
       [sa/TableHeaderCell "Sunday"]
       [sa/TableHeaderCell "Monday"]
       [sa/TableHeaderCell "Tuesday"]
       [sa/TableHeaderCell "Wednesday"]
       [sa/TableHeaderCell "Thursday"]
       [sa/TableHeaderCell "Friday"]
       [sa/TableHeaderCell "Saturday"]]]
     (concatv [sa/TableBody]
              (mapv (fn [row]
                      (concatv [sa/TableRow]
                               (mapv (fn [column]
                                       [sa/TableCell [draw-cell render-shift-label row column selected shifts]])
                                     (range 7)))) (range 5)))]))

(defn CalendarMonthControl [parent]
  (into parent
        [[:div.calendar__controls
          [:div.calendar__button {:on-click (>event [:dec-calendar-month])}
           [sa/Icon {:name "angle left" :size "big"}]]
          [:span.calendar__month-name (<sub [:calendar-month-formated])]
          [:div.calendar__button {:on-click (>event [:inc-calendar-month])}
           [sa/Icon {:name "angle right" :size "big"}]]]
         [sa/GridColumn {:width 6}]]))
