(ns ui.resident.layout
  (:require
   [re-frame.core :as rf]
   [soda-ash.core :as sa]
   [sodium.core :as na]))

(defn resident-layout [content]
  (let [route (rf/subscribe [:route-map/current-route])]
    [na/grid {}
     [na/grid-row {:class-name "moonlight-header"}
      [na/grid-column {:width 1}]
      [na/grid-column {:width 7} [na/header {} "Dr.Moonlight"]]
      [na/grid-column {:width 7}
       [sa/ButtonGroup {}
        [na/button {:content "Schedule"
                    :icon :calendar
                    :active? (= (:match @route) :core/resident-schedule)
                    :on-click #(rf/dispatch [:goto :resident :schedule])}]
        [na/button {:content "Messages"
                    :icon :mail
                    :active? (= (:match @route) :core/resident-messages)
                    :on-click #(rf/dispatch [:goto :resident :messages])}]
        [na/button {:content "Statistics"
                    :icon :table
                    :active? (= (:match @route) :core/resident-statistics)
                    :on-click #(rf/dispatch [:goto :resident :statistics])}]
        [na/button {:content "Profile"
                    :icon :user
                    :active? (= (:. (last (:parents @route))) :core/resident-profile)
                    :on-click #(rf/dispatch [:goto :resident :profile])}]]]]
     [na/grid-row {}
      [na/grid-column {:width 1}]
      [na/grid-column {:width 14} content]
      [na/grid-column {:width 1}]]]))

(defn resident-profile-layout [content]
  (let [route (rf/subscribe [:route-map/current-route])]
    [resident-layout
     [na/grid {}
      [na/grid-row {}
       [na/grid-column {:width 4}
        [na/menu {:fluid? true :vertical? true :tabular? true}
         [na/menu-item {:name "Edit Profile"
                        :active? (= (:match @route) :core/resident-profile)
                        :on-click #(rf/dispatch [:goto :resident :profile])}]
         [na/menu-item {:name "Notification Settings"
                        :active? (= (:match @route) :core/resident-profile-notification)
                        :on-click #(rf/dispatch [:goto :resident :profile :notification])}]
         [na/menu-item {:name "Log out"}]]]

       [na/grid-column {:width 12 :class-name :moonlight-white}
        content]]]]))
