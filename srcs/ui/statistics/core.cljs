(ns ui.statistics.core
  (:require
   [ui.db.misc :refer [<sub]]
   [soda-ash.core :as sa]
   [re-frame.core :as rf]
   [ui.pages :as pages]
   [ui.db.statistics :refer [statistic-field-names-map]]
   [ui.resident.layout :refer [ResidentHeader]]
   [ui.scheduler.layout :refer [SchedulerHeader]]
   [ui.account-manager.core :refer [AccountManagerHeader]]
   [ui.dashboard.core :refer [CommonHeader]]))

(defn StatisticsLayout [content]
  (let [user-type (<sub [:user-type])]
    [sa/Grid
     (if-not (nil? user-type)
       (case user-type
         :resident [ResidentHeader]
         :scheduler [SchedulerHeader]
         :account-manager [AccountManagerHeader])
       [sa/GridRow [sa/Grid {:container true :padded "vertically"} [CommonHeader]]])
     [sa/GridRow {}
      [sa/Container content]]]))

(defn Statistics [params]
  (rf/dispatch [:load-statistics])
  (fn [params]
    (let [statistics (<sub [:statistics-data])]
      [sa/Grid {:class-name "statistics__container"}
       [sa/GridRow {}
        [sa/GridColumn {:width 4}
         [sa/Header "Statistics"]]
        [sa/GridColumn {:width 12}
         [sa/Segment {:class-name "statistics__data-wrapper"}
          [sa/Grid
           (map (fn [[key value]]
                  [sa/GridRow
                   [sa/GridColumn {:width 6} (key statistic-field-names-map)]
                   [sa/GridColumn {:width 10 :class-name "statistics__col-value"}
                    (if (vector? value) (map
                                         (fn [item]
                                           [:span.statistics__col-select (:name item)])
                                         value) value)]])
                statistics)]]]]])))

(pages/reg-page :core/statistics (fn [] [StatisticsLayout [Statistics]]))
