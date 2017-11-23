(ns ui.layout
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require
   [reagent.core :as reagent]
   [ui.styles :as styles]
   [re-frame.core :as rf]
   [ui.routes :refer [href]]
   [ui.styles :as styles]
   [clojure.string :as str]
   [soda-ash.core :as sa]))§

(defn layout [content]
  (fn []
    (let [route (rf/subscribe [:route-map/current-route])]
      [:div
       [:style styles/basic-style]
       [:div.moonlight-container content]])))
