(ns ui.dashboard.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require
   [reagent.core :as reagent]
   [ui.pages :as pages]
   [re-frame.core :as rf]
   [clojure.string :as str]
   [sodium.core :as na]))

(defn index [params]
  [na/grid {}
   [na/grid-row {}
    [na/grid-column {:width 4}
     [na/header {} "Dr. Moonlight"]]
    [na/grid-column {:width 8} [:div]]
    [na/grid-column {:width 4}
     [na/button {:basic? true :color :blue :content "Log In" :on-click (na/>event [:goto :login])}]
     [na/button {:color :blue :content "Sign up" :on-click (na/>event [:goto :sign-up])}]]
    ]
   [na/grid-row {}
    [na/grid-column {:width 4} [:div]]
    [na/grid-column {:width 8}
     [na/container {:text? true}
      [na/header {} "Medical moonlighting ma easy"]
      [:p "Find greateclinical jobs without agency"]]]
    [na/grid-column {:width 4} [:div]]]
   [na/grid-row {}]])

(pages/reg-page :core/index index)
