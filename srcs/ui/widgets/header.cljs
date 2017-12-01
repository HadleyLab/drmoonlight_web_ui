(ns ui.widgets.header
  (:require
   [ui.db.account :refer [is-approved]]
   [ui.db.misc :refer [>evt <sub]]
   [re-frame.core :as rf]
   [soda-ash.core :as sa]))

(defn HeaderLogo []
  [sa/Header {:class-name "header__logo"} "Dr. Moonlight"])

(defn ResidentHeader []
  (let [route (<sub [:route-map/current-route])]
    [sa/GridRow {:class-name "header__wrapper"}
     [sa/Container {:class-name "header__content"}
      [HeaderLogo]
      [sa/ButtonGroup {}
       [sa/Button {:active (= (:match route) :core/resident-schedule)
                   :on-click #(>evt [:goto :resident :schedule])
                   :class-name "header__menu-item"}
        [sa/Icon {:name :calendar :size :large}]
        "Schedule"]
       [sa/Button {:active (= (:. (nth (:parents route) 2 nil))
                              :core/resident-messages)
                   :disabled (not (is-approved))
                   :on-click #(>evt [:goto :resident :messages])
                   :class-name "header__menu-item"}
        [sa/Icon {:name :mail :size :large}]
        "Messages"]
       [sa/Button {:active (= (:match route) :core/resident-statistics)
                   :on-click #(>evt [:goto :resident :statistics])
                   :class-name "header__menu-item"}
        [sa/Icon {:name "line graph" :size :large}]
        "Statistics"]
       [sa/Button {:active (= (:. (last (:parents route))) :core/resident-profile)
                   :on-click #(>evt [:goto :resident :profile])
                   :class-name "header__menu-item"}
        [sa/Icon {:name :user :size :large}]
        "Profile"]]]]))

(defn SchedulerHeader []
  (let [route (<sub [:route-map/current-route])]
    [sa/GridRow {:class-name "header__wrapper"}
     [sa/Container {:class-name "header__content"}
      [HeaderLogo]
      [sa/ButtonGroup {}
       [sa/Button {:active (= (:match route) :core/scheduler-schedule)
                   :on-click #(rf/dispatch [:goto :scheduler :schedule])
                   :class-name "header__menu-item"}
        [sa/Icon {:name :calendar :size :large}]
        "Schedule"]
       [sa/Button {:active (= (:. (nth (:parents route) 2 nil))
                              :core/scheduler-messages)
                   :on-click #(rf/dispatch [:goto :scheduler :messages])
                   :class-name "header__menu-item"}
        [sa/Icon {:name :mail :size :large}]
        "Messages"]
       [sa/Button {:active (= (:match route) :core/scheduler-statistics)
                   :on-click #(rf/dispatch [:goto :scheduler :statistics])
                   :class-name "header__menu-item"}
        [sa/Icon {:name "line graph" :size :large}]
        "Statistics"]
       [sa/Button {:active (= (:. (last (:parents route))) :core/scheduler-profile)
                   :on-click #(rf/dispatch [:goto :scheduler :profile])
                   :class-name "header__menu-item"}
        [sa/Icon {:name :user :size :large}]
        "Profile"]]]]))
