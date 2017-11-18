(ns ui.layout
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require
   [reagent.core :as reagent]
   [ui.styles :as styles]
   [re-frame.core :as rf]
   [ui.routes :refer [href]]
   [ui.styles :as styles]
   [clojure.string :as str]
   [soda-ash.core :as sa]
   [sodium.core :as na]))

(defn current-page? [route key]
  (= (:match route) key))

(defn nav-item [route k path title]
  [:li.nav-item {:class (when (current-page? @route k) "active")}
   [:a.nav-link {:href (apply href path)} title]])

(defn layout [content]
  (fn []
    (let [route (rf/subscribe [:route-map/current-route])]
      [:div
       [:style styles/basic-style]
       [:div.moonlight-container content]])))

(defn user-layout [content]
  (let [route (rf/subscribe [:route-map/current-route])]
    [na/grid {}
     [na/grid-row {:class-name "moonlight-header"}
      [na/grid-column {:width 1}]
      [na/grid-column {:width 7} [na/header {} "Dr.Moonlight"]]
      [na/grid-column {:width 7}
       [sa/ButtonGroup {}
        [na/button {:content "Schedule"
                    :icon :calendar
                    :active? (= (:match @route) :core/resident-schedule)
                    :on-click #(rf/dispatch [:goto :resident :schedule])}]
        [na/button {:content "Messages"
                    :icon :mail
                    :active? (= (:match @route) :core/resident-messages)
                    :on-click #(rf/dispatch [:goto :resident :messages])}]
        [na/button {:content "Statistics"
                    :icon :table
                    :active? (= (:match @route) :core/resident-statistics)
                    :on-click #(rf/dispatch [:goto :resident :statistics])}]
        [na/button {:content "Profile"
                    :icon :user
                    :active? (= (:. (last (:parents @route))) :core/resident-profile)
                    :on-click #(rf/dispatch [:goto :resident :profile])}]]]]
     [na/grid-row {}
      [na/grid-column {:width 1}]
      [na/grid-column {:width 14} content]
      [na/grid-column {:width 1}]]]))
