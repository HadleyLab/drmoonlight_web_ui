(ns ui.resident.layout
  (:require
   [re-frame.core :as rf]
   [ui.layout :refer [user-layout]]
   [sodium.core :as na]))

(defn resident-layout [content]
  [user-layout content])

(defn resident-profile-layout [content]
  (let [route (rf/subscribe [:route-map/current-route])]
    [user-layout
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

