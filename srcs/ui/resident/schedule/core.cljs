(ns ui.resident.schedule.core
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

(dt/month (dt/now))

(def root-path :resident-schedule-page)

(defn schema []
  {:selected (dt/date-time (dt/year (dt/now)) (dt/month (dt/now)) 1)
   :shifts {:status :not-asked}})

(defn shift-label [{title :title start :start finish :finish total :total pk :pk}]
  [:p [sa/Popup
       {:trigger (reagent/as-element [sa/Label {:color :blue} title])
        :flowing true
        :position "left center"
        :hoverable true}
       [:div
        [:p [:strong "start"] start]
        [:p [:strong "finish"] finish]
        [:p [:strong "total"] total]
        [na/button {:content "Apply" :on-click (na/>event [:goto :resident :messages pk :apply])}]]]])

(defn as-key [date]
  (format/unparse (format/formatter "yyyy-MM-dd") date))

(defn get-shifts-for-day [shifts day]
  (mapv shift-label (get-in shifts [:data (as-key day)] [])))

(defn draw-cell [row column selected shifts]
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
    (concatv [:div [:p (dt/day cell-date)]] (get-shifts-for-day shifts cell-date))))

(defn calendar [selected shifts]
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
                                     [sa/TableCell [draw-cell row column selected shifts]])
                                   (range 7)))) (range 5)))])

(defn index [params]
  (rf/dispatch-sync [::init-resident-shedule-page])
  (let [selected-cursor @(rf/subscribe [:cursor [root-path :selected]])
        shifts-cursor @(rf/subscribe [:cursor [root-path :shifts]])]
    (fn [params]
      [resident-layout
       [na/grid {}
        [na/grid-row {}
         [na/grid-column {:width 6}]
         [na/grid-column {:width 4}
          [na/button {:icon "angle left"
                      :on-click #(reset! selected-cursor (dt/minus @selected-cursor (dt/months 1)))}]
          [:span (format/unparse (format/formatter "MMMM YYYY") @selected-cursor)]
          [na/button {:icon "angle right"
                      :on-click #(reset! selected-cursor (dt/plus @selected-cursor (dt/months 1)))}]
          [na/grid-column {:width 6}]]]
        [na/grid-row {}
         [na/grid-column {:width 1}]
         [calendar @selected-cursor @shifts-cursor]
         [na/grid-column {:width 1}]]]])))

(rf/reg-event-fx
 ::init-resident-shedule-page
 (fn [{db :db} [_]]
   {:db (if (= (root-path db) nil)
          (assoc-in db [root-path] (schema))
          db)
    :dispatch [:load-shifts]}))

(rf/reg-event-db
 :load-shifts
 (fn [db [_]]
   (assoc-in db [root-path :shifts]
             {:status :succeed
              :data {"2017-11-30" [{:pk 1 :date (dt/date-time 2017 11 30) :title "doctor" :start "8pm" :finish "11pm" :total "6 hours"}
                                   {:pk 2 :date (dt/date-time 2017 11 30) :title "doctor" :start "8pm" :finish "11pm" :total "6 hours"}
                                   {:pk 3 :date (dt/date-time 2017 11 30) :title "doctor" :start "8pm" :finish "11pm" :total "6 hours"}]
                     "2017-12-01" [{:pk 4 :date (dt/date-time 2017 12 1) :title "doctor" :start "8pm" :finish "11pm" :total "6 hours"}]
                     "2017-12-02" [{:pk 5 :date (dt/date-time 2017 12 1) :title "doctor" :start "8pm" :finish "11pm" :total "6 hours"}]
                     "2017-12-03" [{:pk 6 :date (dt/date-time 2017 12 1) :title "doctor" :start "8pm" :finish "11pm" :total "6 hours"}]
                     "2017-12-04" [{:pk 7 :date (dt/date-time 2017 12 1) :title "doctor" :start "8pm" :finish "11pm" :total "6 hours"}]
                     "2017-12-05" [{:pk 8 :date (dt/date-time 2017 12 1) :title "doctor" :start "8pm" :finish "11pm" :total "6 hours"}]
                     "2017-12-06" [{:pk 9 :date (dt/date-time 2017 12 6) :title "doctor" :start "8pm" :finish "11pm" :total "6 hours"}
                                   {:pk 10 :date (dt/date-time 2017 12 6) :title "doctor" :start "8pm" :finish "11pm" :total "6 hours"}
                                   {:pk 11 :date (dt/date-time 2017 12 6) :title "doctor" :start "8pm" :finish "11pm" :total "6 hours"}]}})))

(pages/reg-page :core/resident-schedule index)
