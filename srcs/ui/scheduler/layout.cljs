(ns ui.scheduler.layout
  (:require
   [re-frame.core :as rf]
   [soda-ash.core :as sa]
   [sodium.core :as na]))

(defn scheduler-layout [content]
  (let [route (rf/subscribe [:route-map/current-route])]
    [na/grid {}
     [na/grid-row {:class-name "moonlight-header"}
      [na/grid-column {:width 1}]
      [na/grid-column {:width 7} [na/header {} "Dr.Moonlight"]]
      [na/grid-column {:width 7}
       [sa/ButtonGroup {}
        [na/button {:content "Schedule"
                    :icon :calendar
                    :active? (= (:match @route) :core/scheduler-schedule)
                    :on-click #(rf/dispatch [:goto :scheduler :schedule])}]
        [na/button {:content "Messages"
                    :icon :mail
                    :active? (= (:match @route) :core/scheduler-messages)
                    :on-click #(rf/dispatch [:goto :scheduler :messages])}]
        [na/button {:content "Statistics"
                    :icon :table
                    :active? (= (:match @route) :core/scheduler-statistics)
                    :on-click #(rf/dispatch [:goto :scheduler :statistics])}]
        [na/button {:content "Profile"
                    :icon :user
                    :active? (= (:. (last (:parents @route))) :core/scheduler-profile)
                    :on-click #(rf/dispatch [:goto :scheduler :profile])}]]]]
     [na/grid-row {}
      [na/grid-column {:width 1}]
      [na/grid-column {:width 14} content]
      [na/grid-column {:width 1}]]]))

