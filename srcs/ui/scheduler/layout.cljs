(ns ui.scheduler.layout
  (:require
   [ui.db.misc :refer [>evt <sub >event]]
   [soda-ash.core :as sa]
   [ui.widgets.header-logo :refer [HeaderLogo]]))

(defn Header []
  (let [route (<sub [:route-map/current-route])]
    [sa/GridRow {:class-name "header__wrapper"}
     [sa/Container {:class-name "header__content"}
      [HeaderLogo]
      [sa/ButtonGroup {}
       [sa/Button {:active (= (:match route) :core/scheduler-schedule)
                   :on-click (>event [:goto :scheduler :schedule])
                   :class-name "header__menu-item"}
        [sa/Icon {:name :calendar :size :large}]
        "Schedule"]
       [sa/Button {:active (= (:. (nth (:parents route) 2 nil))
                              :core/scheduler-messages)
                   :on-click (>event [:goto :scheduler :messages])
                   :class-name "header__menu-item"}
        [sa/Icon {:name :mail :size :large}]
        "Messages"]
       [sa/Button {:active (= (:match route) :core/scheduler-statistics)
                   :on-click (>event [:goto :scheduler :statistics])
                   :class-name "header__menu-item"}
        [sa/Icon {:name "line graph" :size :large}]
        "Statistics"]
       [sa/Button {:active (= (:. (last (:parents route))) :core/scheduler-profile)
                   :on-click (>event [:goto :scheduler :profile])
                   :class-name "header__menu-item"}
        [sa/Icon {:name :user :size :large}]
        "Profile"]]]]))

(defn SchedulerLayout [content]
  (let [route (<sub [:route-map/current-route])]
    [sa/Grid {}
     [Header]
     [sa/GridRow {}
      [sa/Container content]]]))

(defn SchedulerProfileLayout [content]
  (let [route (<sub [:route-map/current-route])]
    [SchedulerLayout
     [sa/Grid {}
      [sa/GridRow {}
       [sa/GridColumn {:width 4}
        [:div {:class-name "profile__menu-wrapper"}
         [sa/Menu {:fluid true :vertical true :tabular true}
          [sa/MenuItem {:name "Edit Profile"
                        :active (= (:match route) :core/scheduler-profile)
                        :on-click #(>evt [:goto :scheduler :profile])}]
          [sa/MenuItem {:name "Log out"
                        :on-click #(>evt [:logout])}]]]]
       [sa/GridColumn {:width 12}
        [:div {:class-name "profile__container moonlight-white"} content]]]
      [sa/GridRow {}]]]))
