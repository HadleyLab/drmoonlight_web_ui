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

   ; header styles
   [:.header__wrapper {:height (px 80)
                       :align-items "center!important"
                       :padding-bottom "0!important"
                       :background-color "#FFF"
                       :margin-bottom (px 20)
                       :box-shadow "0 2px 8px 0 rgba(0,0,0,0.1)"}]
   [:.header__content {:display "flex!important"
                       :justify-content "space-between"
                       :align-items "center!important"}
    [:.header {:margin-bottom "0!important"}]]
   [:.header__logo {:font-size "24px!important" :cursor "pointer"}]
   [:.header__menu-item {:background "none!important"
                         :font-size "15px!important"
                         :padding "0!important"
                         :color "#000!important"
                         :margin-left "60px!important"
                         :display "inline-flex!important"
                         :align-items :center}
    [:.icon {:color "#8C97B2!important"
             :opacity "1!important"
             :height "auto!important"
             :transition "color 0.2s!important"}]
    [:&:hover [:.icon {:color "#000!important"}]]
    [:&.active {:color "#2185d0!important"}
     [:.icon {:color "inherit!important"}]]]

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
   [:.form__group {:margin-top (px 30)}]
   [:.form__group-title {:display "inline-block"
                         :color "#8C97B2"
                         :margin-bottom (px 10)}]
   [:.form__label {:display "inline-block!important"}]
   [:.form__label-desc {:font-weight "normal"
                        :color "#8C97B2"}]
   [:.form__radio {:display "inline-block"
                   :margin-left "20px!important"
                   :vertical-align "top"}]
   [:.toggler [:label:after
               {:box-shadow
                "0 1px 2px 0 rgba(34,36,38,.15), 0 0 0 1px rgba(34,36,38,.15) inset!important"}]]

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
   [:.signup__user-desc {:position "absolute" :right 0 :top 0}]
   [:.signup__container.ui.form [:.field.eleven.wide {:width "62.5%!important"}]]

   ; schedule screen styles
   [:.schedule__shifts-menu-item {:display "flex"
                                  :color "#8C97B2"
                                  :font-size (px 14)
                                  :line-height (px 15)
                                  :margin-bottom (px 12)
                                  :justify-content "space-between"}]
   [:.schedule__shifts-menu-item-label {:cursor "pointer"
                                        :position "relative"
                                        :padding-left (px 17)
                                        :transition "color 0.2s"}
    [:&:hover {:color "#000"}]]
   [:.schedule__shifts-menu-item-checkbox {:margin-left (px 7)}]
   [:.schedule__shifts-menu-item-color {:display "inline-block"
                                        :position "absolute"
                                        :left 0
                                        :top (px 5)
                                        :width (px 7)
                                        :height (px 7)
                                        :border-radius "50%"}
    [:&._completed {:background-color "#00BFDD"}]
    [:&._active {:background-color "#FFAB00"}]
    [:&._without_applies {:background-color "#E64C66"}]
    [:&._coverage_completed {:background-color "#44BE8D"}]
    [:&._require_approval {:background-color "#7874CF"}]]

   ; calendar styles
   [:.calendar__table {:table-layout :fixed}]
   [:.calendar__table-header [:th {:padding "7px!important"
                                   :text-align "center!important"}]]
   [:.calendar__controls {:display "flex"
                          :align-items "center"
                          :justify-content "center"}]
   [:.calendar__button {:display "inline-block"
                        :color "#8C97B2"
                        :cursor "pointer"
                        :transition "color 0.2s"}
    [:&:hover {:color "#000"}]]
   [:.calendar__month-name {:font-size (px 18)
                            :font-weight "bold"
                            :letter-spacing (px -0.12)
                            :margin "0 10px"}]
   [:.calendar__cell {:position "relative"
                      :width "14.28%"
                      :max-width "14.28%"
                      :padding "0!important"
                      :vertical-align "top"}]
   [:.calendar__cell-inner {:height "inherit"
                            :width "100%"
                            :padding "22px 7px 5px"
                            :min-height (px 105)}]
   [:.calendar__date {:position "absolute"
                      :right (px 7)
                      :top (px 6)
                      :color "rgba(140,151,178,0.3)"
                      :font-size (px 12)}]
   [:.calendar__cell-inner._current-month [:.calendar__date {:color "#888A8C"}]]
   [:.calendar__cell-inner._today
    [:.calendar__cell-bg {:position "absolute"
                          :left 0
                          :top 0
                          :right 0
                          :bottom 0
                          :background-color "rgba(140,151,178,0.17)"}]
    [:.calendar__date {:font-weight "bold"
                       :color "#000"}]]
   ; shifts styles
   [:.shift__label {:position "relative"
                    :padding "3px 8px"
                    :color "#fff"
                    :font-size (px 12)
                    :line-height (px 14)
                    :border-radius (px 4)
                    :margin-top (px 2)
                    :cursor "default"
                    :word-wrap "break-word"}
    [:&._completed {:background-color "#00BFDD"}]
    [:&._active {:background-color "#FFAB00"}]
    [:&._without_applies {:background-color "#E64C66"}]
    [:&._coverage_completed {:background-color "#44BE8D"}]
    [:&._require_approval {:background-color "#7874CF"}]]
   [:.shift__popup {:padding "0!important"}]
   [:.shift__popup-content {:padding "20px 20px 10px"}]
   [:.shift__popup-footer._scheduler {:display :flex
                                      :justify-content "space-between"
                                      :align-items "center"
                                      :padding "0 20px 20px"}]
   [:.shift__popup-footer._resident
    [:p {:margin 0
         :padding "0 20px 20px"
         :font-weight "bold"}]
    [:.ui.button {:border-top-left-radius "0"
                  :border-top-right-radius "0"}]]
   [:.shift__row {:margin "10px 0"
                  :font-size (px 14)
                  :line-height (px 17)}
    [:i {:color "#8C97B2"
         :font-style "normal"}]]
   [:.shift__remove-shift {:color "#E64C66" :cursor "pointer"}]

   ; profile screen styles
   [:.profile__menu-wrapper {:position "relative"
                             :margin-right "-15px"
                             :z-index 1}]
   [:.profile__container {:margin "0 0 0 -1em"
                          :border "1px solid #d4d4d5"
                          :border-radius "0 3px 3px"}
    [:.form__label {:width "52%"}]
    [:.form__group {:position "relative"
                    :margin "20px 0 40px"}]]
   [:.profile__content {:margin-top (px -20)
                        :min-height (px 600)}]
   [:.profile__content._notifications
    [:label {:margin-top (px 15)}]
    [:.form__group-title {:margin-top 0}]]
   [:.profile__content._edit
    [:.profile__button {:margin-top (px 25)}]
    [:.form__group:after {:content "\"\""
                          :position "absolute"
                          :left (px -70)
                          :right (px -70)
                          :bottom (px -25)
                          :height (px 1)
                          :background-color "#DFE2E9"}]
    [:.form__group._account-settings
     {:margin-bottom 0}
     [:&:after {:content "none"}]]]))
