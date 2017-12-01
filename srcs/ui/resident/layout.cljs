(ns ui.resident.layout
  (:require
   [ui.db.account :refer [is-new]]
   [ui.db.misc :refer [>evt <sub]]
   [soda-ash.core :as sa]
   [ui.widgets.header :refer [ResidentHeader]]))

(defn ResidentLayout [content]
  (let [route (<sub [:route-map/current-route])]
    [sa/Grid {}
     [ResidentHeader]
     [sa/GridRow {}
      [sa/Container content]]]))

(defn ResidentProfileLayout [content]
  (let [route (<sub [:route-map/current-route])]
    [ResidentLayout
     [sa/Grid {}
      [sa/GridRow {}
       [sa/GridColumn {:width 4}
        [:div {:class-name "profile__menu-wrapper"}
         [sa/Menu {:fluid true :vertical true :tabular true}
          [sa/MenuItem {:name "Edit Profile"
                        :active (= (:match route) :core/resident-profile)
                        :on-click #(>evt [:goto :resident :profile])}]
          (when-not (is-new) [sa/MenuItem {:name "Notification Settings"
                                           :active (= (:match route) :core/resident-profile-notification)
                                           :on-click #(>evt [:goto :resident :profile :notification])}])
          [sa/MenuItem {:name "Log out"
                        :on-click #(>evt [:logout])}]]]]

       [sa/GridColumn {:width 12}
        [:div {:class-name "profile__container moonlight-white"} content]]]
      [sa/GridRow {}]]]))
