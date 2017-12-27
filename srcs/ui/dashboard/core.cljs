(ns ui.dashboard.core
  (:require
   [reagent.core :as reagent]
   [ui.db.misc :refer [>event <sub]]
   [ui.pages :as pages]
   [re-frame.core :as rf]
   [clojure.string :as str]
   [soda-ash.core :as sa]))

(defn CommonHeader []
  (let [user-type (<sub [:user-type])]
    [sa/GridRow {}
     [sa/GridColumn {:width 4}
      [sa/Header {} "Dr. Moonlight"]]
     [sa/GridColumn {:width 12 :text-align "right"}
      (when (nil? user-type) [sa/Button {:basic true :color :blue :content "Log In" :on-click (>event [:goto :login])}])
      (when (nil? user-type) [sa/Button {:color :blue :content "Sign up" :on-click (>event [:goto :sign-up])}])
      (when-not (nil? user-type) [sa/Button {:color :blue :content "Go to profile" :on-click (>event [:goto user-type :profile])}])
      (when-not (nil? user-type) [sa/Button {:basic true :color :blue :content "Log Out" :on-click (>event [:logout])}])]]))

(defn Index [params]
  (let [user-type (<sub [:user-type])]
    [sa/Grid {:container true :padded "vertically" :class-name "full-screen flex-direction _column"}
     [CommonHeader]
     [sa/GridRow {:centered true :class-name "flex _1 align-items _center"}
      [sa/GridColumn {:width 8}]
      [sa/GridColumn {:width 8}
       [:div {:class-name "dashboard__header"}
        "Find local moonlighting, quick and easy, and supplement your income"]]]
     [sa/GridRow {}
      [sa/GridColumn {:width 16 :class-name "dashboard__how-it-works"}
       [:a {:href "#"} "How it works"]]]]))

(pages/reg-page :core/index Index)
