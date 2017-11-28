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
    [sa/GridColumn {:width 8}]
    [sa/GridColumn {:width 8}
     [:div {:class-name "dashboard__header"}
      "Find local moonlighting, quick and easy, and supplement your income"]]]
   [sa/GridRow {}
    [sa/GridColumn {:width 16 :class-name "dashboard__how-it-works"}
     [:a {:href "#"} "How it works"]]]])

(pages/reg-page :core/index Index)
