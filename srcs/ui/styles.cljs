(ns ui.styles
  (:require [garden.core :as garden]
            [garden.units :as units]))

(def px units/px)

(defn style [css]
  [:style (garden/css css)])

(def basic-style
  (garden/css
   ; common styles
   [:body {:background-color "#F3F8FC"}]
   [:.full-screen {:height "100vh"
                   :min-height (px 600)}]
   [:.flex-direction._column {:flex-direction "column"}]
   [:.flex._1 {:flex 1}]
   [:.align-items._center
    :.row.align-items._center {:align-items "center"}]
   [:.moonlight-white {:background-color "#FFF"}]
   [:.moonlight-header {:height (px 80)
                        :background-color "#FFF"
                        :margin-bottom (px 35)
                        :box-shadow "0 2px 1px -1px gray"}]

   ; dashboard styles
   [:.dashboard-header {:margin-top (px -100)}]

   ; form styles
   [:.error {:color :red}]
   [:.moonlight-form-header {:margin-bottom "40px!important"
                             :text-align "center"}]
   [:.moonlight-form {:padding "70px 0"}]
   [:.moonlight-form-inner
    :.segment.moonlight-form-inner {:padding "40px 70px 70px"}]
   [:div.moonlight-form-group {:margin-top (px 40)}]
   [:div.moonlight-form-group [:label {:color :gray}]]))
