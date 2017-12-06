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

(defn- format-day [day]
  (if (> (count (str day)) 1) day (str 0 day)))

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
                                               (format-day (+ prev-month-max-day day))))
                    (> day selected-month-max-day) (js/moment
                                                    (str (.format next-month "YYYY-MM-")
                                                         (format-day (- day selected-month-max-day))))
                    :else (js/moment (str (.format selected "YYYY-MM-") (format-day day))))
        is-today (= (.format (js/moment) "YYYY-MM-DD") (.format cell-date "YYYY-MM-DD"))
        is-current-month (= (.month (js/moment selected)) (.month cell-date))]
    (concatv [:div {:class-name (str "calendar__cell-inner"
                                     " "
                                     (if is-current-month "_current-month")
                                     " "
                                     (if is-today "_today"))}
              [:div.calendar__cell-bg]
              [:div.calendar__date (.date cell-date)]]
             (mapv render-shift-label (get-shifts-for-day shifts cell-date)))))

(defn Calendar [selected shifts render-shift-label]
  (fn [selected shifts]
    [sa/Table {:class-name "calendar__table" :celled true}
     [sa/TableHeader {:class-name "calendar__table-header"}
      [sa/TableRow
       [sa/TableHeaderCell "Sun"]
       [sa/TableHeaderCell "Mon"]
       [sa/TableHeaderCell "Tue"]
       [sa/TableHeaderCell "Wed"]
       [sa/TableHeaderCell "Thu"]
       [sa/TableHeaderCell "Fri"]
       [sa/TableHeaderCell "Sat"]]]
     (concatv [sa/TableBody]
              (mapv (fn [row]
                      (concatv [sa/TableRow]
                               (mapv (fn [column]
                                       [sa/TableCell {:class-name "calendar__cell"}
                                        [draw-cell render-shift-label row column selected shifts]])
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
