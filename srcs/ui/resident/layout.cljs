(ns ui.resident.layout
  (:require
   [ui.db.misc :refer [>evt <sub]]
   [re-frame.core :as rf]
   [soda-ash.core :as sa]))

(defn ResidentLayout [content]
  (let [route (<sub [:route-map/current-route])]
    [sa/Grid {}
     [sa/GridRow {:class-name "moonlight-header"}
      [sa/GridColumn {:width 1}]
      [sa/GridColumn {:width 7} [sa/Header {} "Dr.Moonlight"]]
      [sa/GridColumn {:width 7}
       [sa/ButtonGroup {}
        [sa/Button {:active (= (:match route) :core/resident-schedule)
                    :on-click #(>evt [:goto :resident :schedule])}
         [sa/Icon {:name :calendar}]
         "Schedule"]
        [sa/Button {:active (= (:. (nth (:parents route) 2 nil))
                               :core/resident-messages)
                    :on-click #(>evt [:goto :resident :messages])}
         [sa/Icon {:name :mail}]
         "Messages"]
        [sa/Button {:active (= (:match route) :core/resident-statistics)
                    :on-click #(>evt [:goto :resident :statistics])}
         [sa/Icon {:name :table}]
         "Statistics"]
        [sa/Button {:active (= (:. (last (:parents route))) :core/resident-profile)
                    :on-click #(>evt [:goto :resident :profile])}
         [sa/Icon {:name :user}]
         "Profile"]]]]
     [sa/GridRow {}
      [sa/GridColumn {:width 1}]
      [sa/GridColumn {:width 14} content]
      [sa/GridColumn {:width 1}]]]))

(defn ResidentProfileLayout [content]
  (let [route (rf/subscribe [:route-map/current-route])]
    [ResidentLayout
     [sa/Grid {}
      [sa/GridRow {}
       [sa/GridColumn {:width 4}
        [:div {:class-name "profile__menu-wrapper"}
         [sa/Menu {:fluid true :vertical true :tabular true}
          [sa/MenuItem {:name "Edit Profile"
                        :active (= (:match @route) :core/resident-profile)
                        :on-click #(>evt [:goto :resident :profile])}]
          [sa/MenuItem {:name "Notification Settings"
                        :active (= (:match @route) :core/resident-profile-notification)
                        :on-click #(>evt [:goto :resident :profile :notification])}]
          [sa/MenuItem {:name "Log out"
                        :on-click #(>evt [:logout])}]]]]

       [sa/GridColumn {:width 12}
        [:div {:class-name "profile__content moonlight-white"} content]]]]]))
