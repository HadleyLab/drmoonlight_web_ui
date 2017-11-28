(ns ui.scheduler.layout
  (:require
   [ui.db.misc :refer [<sub]]
   [re-frame.core :as rf]
   [soda-ash.core :as sa]))

(defn SchedulerLayout [content]
  (let [route (<sub [:route-map/current-route])]
    [sa/Grid {}
     [sa/GridRow {:class-name "moonlight-header"}
      [sa/GridColumn {:width 1}]
      [sa/GridColumn {:width 7} [sa/Header {} "Dr.Moonlight"]]
      [sa/GridColumn {:width 7}
       [sa/ButtonGroup {}
        [sa/Button {:active (= (:match route) :core/scheduler-schedule)
                    :on-click #(rf/dispatch [:goto :scheduler :schedule])}
         [sa/Icon {:name :calendar}]
         "Schedule"]

        [sa/Button {:active (= (:. (nth (:parents route) 2 nil))
                               :core/scheduler-messages)
                    :on-click #(rf/dispatch [:goto :scheduler :messages])}
         [sa/Icon {:name :mail}]
         "Messages"]
        [sa/Button {:active (= (:match route) :core/scheduler-statistics)
                    :on-click #(rf/dispatch [:goto :scheduler :statistics])}
         [sa/Icon {:name :table}]
         "Statistics"]
        [sa/Button {:active (= (:. (last (:parents route))) :core/scheduler-profile)
                    :on-click #(rf/dispatch [:goto :scheduler :profile])}
         [sa/Icon {:name :user}]
         "Profile"]]]]
     [sa/GridRow {}
      [sa/GridColumn {:width 1}]
      [sa/GridColumn {:width 14} content]
      [sa/GridColumn {:width 1}]]]))

