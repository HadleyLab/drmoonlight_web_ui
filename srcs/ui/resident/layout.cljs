(ns ui.resident.layout
  (:require
   [ui.db.account :refer [is-new is-approved]]
   [ui.db.misc :refer [>evt <sub >event]]
   [soda-ash.core :as sa]
   [ui.widgets.header-logo :refer [HeaderLogo]]))

(defn Header []
  (let [route (<sub [:route-map/current-route])]
    [sa/GridRow {:class-name "header__wrapper"}
     [sa/Container {:class-name "header__content"}
      [HeaderLogo]
      [sa/ButtonGroup {}
       [sa/Button {:active (= (:match route) :core/resident-schedule)
                   :on-click (>event [:goto :resident :schedule])
                   :class-name "header__menu-item"}
        [sa/Icon {:name :calendar :size :large}]
        "Schedule"]
       [sa/Button {:active (= (:. (nth (:parents route) 2 nil))
                              :core/resident-messages)
                   :disabled (not (is-approved))
                   :on-click (>event [:goto :resident :messages])
                   :class-name "header__menu-item"}
        [sa/Icon {:name :mail :size :large}]
        "Messages"]
       [sa/Button {:active (= (:match route) :core/resident-statistics)
                   :on-click (>event [:goto :resident :statistics])
                   :class-name "header__menu-item"}
        [sa/Icon {:name "line graph" :size :large}]
        "Statistics"]
       [sa/Button {:active (= (:. (last (:parents route))) :core/resident-profile)
                   :on-click (>event [:goto :resident :profile])
                   :class-name "header__menu-item"}
        [sa/Icon {:name :user :size :large}]
        "Profile"]]]]))

(defn ResidentLayout [content]
  (let [route (<sub [:route-map/current-route])]
    [sa/Grid {}
     [Header]
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
          [sa/MenuItem {:name "Change Password"
                        :active (= (:match route) :core/resident-profile-change-password)
                        :on-click #(>evt [:goto :resident :profile :change-password])}]
          [sa/MenuItem {:name "Log out"
                        :on-click #(>evt [:logout])}]]]]

       [sa/GridColumn {:width 12}
        [:div {:class-name "profile__container moonlight-white"} content]]]
      [sa/GridRow {}]]]))
