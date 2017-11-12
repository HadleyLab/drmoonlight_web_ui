(ns ui.layout
  (:require-macros [reagent.ratom :refer [reaction]])

  (:require
   [reagent.core :as reagent]
   [ui.styles :as styles]
   [re-frame.core :as rf]
   [ui.routes :refer [href]]
   [ui.styles :as styles]
   [clojure.string :as str]))

(defn current-page? [route key]
  (= (:match route) key))

(defn nav-item [route k path title]
  [:li.nav-item {:class (when (current-page? @route k) "active")}
   [:a.nav-link {:href (apply href path)} title]])

(defn layout [content]
  (fn []
    (let [route (rf/subscribe [:route-map/current-route])]
        [:div.container content])))
