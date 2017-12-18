(ns ui.statistics.core
  (:require
   [soda-ash.core :as sa]
   [re-frame.core :as rf]))

(defn Statistics [params]
  (rf/dispatch [:load-statistics])
  (let []
    (fn [params]
      [sa/Grid {:class-name "statistics__container"}
       [sa/GridRow {}
        [sa/GridColumn {:width 4}
         [sa/Header "Statistics"]]
        [sa/GridColumn {:width 12}
         [sa/Segment {:class-name "statistics__data-wrapper"}
          "Statistics data"]]]])))
