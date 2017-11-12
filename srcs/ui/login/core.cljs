(ns ui.login.core
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
     [na/grid-column {:width 4} [:div]]
     [na/grid-column {:width 8}
      [na/container {:text? true}
       [na/header {} "Welcome back to Dr. Moonlight!"]]
     [na/grid-column {:width 4} [:div]]]
    [na/grid-row {}]]])

(pages/reg-page :core/login index)
