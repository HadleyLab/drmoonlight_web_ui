(ns ui.dashboard.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require
   [reagent.core :as reagent]
   [ui.db :refer [>event]]
   [ui.pages :as pages]
   [re-frame.core :as rf]
   [clojure.string :as str]
   [soda-ash.core :as sa]))

(defn Index [params]
  [sa/Grid {:container true :padded "vertically" :class-name "full-screen flex-direction _column"}
   [sa/GridRow {}
    [sa/GridColumn {:width 4}
     [sa/Header {} "Dr. Moonlight"]]
    [sa/GridColumn {:width 12 :text-align "right"}
     [sa/Button {:basic true :color :blue :content "Log In" :on-click (>event [:goto :login])}]
     [sa/Button {:color :blue :content "Sign up" :on-click (>event [:goto :sign-up])}]]]
   [sa/GridRow {:centered true :class-name "flex _1 align-items _center"}
    [:div {:class-name "dashboard__header"}
     [sa/Header {:as "h1" :text-align "left"}
      [sa/HeaderContent {}
       [:div "Medical moonlighting made easy"]
       [sa/HeaderSubheader {:class-name "dashboard__subheader"}
        "Find great clinical jobs without agency recruiters"]]]]]])

(pages/reg-page :core/index Index)
