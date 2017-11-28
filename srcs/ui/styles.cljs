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
   [:.flex-direction._column
    :.field.flex-direction._column {:flex-direction "column!important"}]
   [:.flex._1 {:flex 1}]
   [:.align-items._center
    :.row.align-items._center
    :.column.align-items._center {:align-items "center"
                                  :justify-content "center"}]
   [:.justify-content._space-between {:justify-content "space-between"}]
   [:.position._relative {:position "relative"}]
   [:.gray-font {:color "#8C97B2"}]
   [:.moonlight-white {:background-color "#FFF"}]
   [:.moonlight-header {:height (px 80)
                        :background-color "#FFF"
                        :margin-bottom (px 35)
                        :box-shadow "0 2px 1px -1px gray"}]

   ; dashboard styles
   [:.dashboard__header {:font-size (px 46)
                         :line-height (px 55)
                         :font-weight "bold"}]
   [:.dashboard__how-it-works {:text-align "center"
                               :padding "50px 0"
                               :font-size (px 28)
                               :line-height (px 34)
                               :font-weight "bold"}]
   [:.dashboard__how-it-works
    [:a {:color "#000"}
     [:&:hover {:color "#000"
                :text-decoration "underline"}]]]

   ; login screen styles
   [:.login__right-column {:text-align "center"
                           :display "inline-flex!important"
                           :flex-direction "column"}]
   [:.field.login__forgot-password {:padding-top (px 10)
                                    :text-align "right"}]

   ; forgot password screen styles
   [:.forgot-password__subheader {:margin-bottom (px 30)
                                  :text-align "center"}]
   [:.forgot-password__buttons {:margin "0 -0.5em!important"}]
   [:.forgot-password__back {:padding-top (px 30)
                             :text-align "center"}]

   ; signup screen styles
   [:.signup__back {:text-align "center"
                    :margin-top "30px!important"}]
   [:.signup-thanks__wrapper {:text-align "center"}]

   ; form styles
   [:.input-info {:display "flex"
                  :align-items "center"
                  :padding-top (px 23)}]
   [:.error {:color :red}]
   [:.moonlight-form-header {:margin-bottom "40px!important"
                             :text-align "center"}]
   [:.moonlight-form {:padding "70px 0"}]
   [:.moonlight-form-inner
    :.segment.moonlight-form-inner {:padding "40px 70px 70px"}]
   [:.moonlight-form-group {:margin-top (px 30)}]
   [:.moonlight-form-group [:label {:display "inline-block"
                                    :color "#8C97B2"
                                    :margin-bottom (px 10)}]]))
