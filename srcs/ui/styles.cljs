(ns ui.styles
  (:require [garden.core :as garden]
            [garden.units :as units]))

(def px units/px)

(defn style [css]
  [:style (garden/css css)])

(def basic-style
  (garden/css
   [:body
    [:div.container {:background-color "#F3F8FC"}]
    [:div.moonlight-form {:background-color "#FFF" :padding-top (px 70)}]
    [:div.moonlight-form-group {:margin-top (px 40)}]
    [:div.moonlight-form-group [:label {:color :gray}]]]))
