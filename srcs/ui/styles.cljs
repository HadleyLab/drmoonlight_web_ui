(ns ui.styles
  (:require [garden.core :as garden]
            [garden.units :as units]))

(def px units/px)

(defn style [css]
  [:style (garden/css css)])

(def basic-style
  (garden/css
   [:body
    [:.error {:color :red}]
    [:div.moonlight-container {:background-color "#F3F8FC"}]
    [:div.moonlight-form-wrapper {:padding-top (px 70)
                                  :padding-bottom (px 70)}]
    [:h1.moonlight-form-header {:margin-top "20px!important"
                                :margin-left "170px!important"}]
    [:div.moonlight-form {:background-color "#FFF"
                          :border "1px solid #ACB5BE"
                          :border-radius (px 3)
                          :padding-bottom (px 70)}]
    [:form.moonlight-inner-form {:background-color "#FFF"
                                 :padding-left (px 70)
                                 :padding-right (px 70)}]
    [:div.moonlight-form-group {:margin-top (px 40)}]
    [:div.moonlight-form-group [:label {:color :gray}]]]))
