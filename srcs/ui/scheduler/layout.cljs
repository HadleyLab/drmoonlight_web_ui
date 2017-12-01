(ns ui.scheduler.layout
  (:require
   [ui.db.misc :refer [<sub]]
   [soda-ash.core :as sa]
   [ui.widgets.header :refer [SchedulerHeader]]))

(defn SchedulerLayout [content]
  (let [route (<sub [:route-map/current-route])]
    [sa/Grid {}
     [SchedulerHeader]
     [sa/GridRow {}
      [sa/Container content]]]))
