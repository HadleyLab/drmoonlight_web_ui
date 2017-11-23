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
   [:div.moonlight-form-wrapper {:padding-top (px 70)}]
                                  ; :padding-bottom (px 70)}]
   [:h1.moonlight-form-header {:margin-top "20px!important"
                               :margin-left "100px!important"}]
   [:div.moonlight-form {:background-color "#FFF"
                         :border "1px solid #ACB5BE"
                         :border-radius (px 3)
                         :padding-bottom (px 70)}]
   [:.moonlight-inner-form {:background-color "#FFF"
                            :padding-left (px 70)
                            :padding-right (px 70)}]
   [:div.moonlight-form-group {:margin-top (px 40)}]
   [:div.moonlight-form-group [:label {:color :gray}]]))
