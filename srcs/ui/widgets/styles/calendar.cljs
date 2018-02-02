(ns ui.widgets.styles.calendar
  (:require
   [clojure.string :as str]
   [ui.styles :refer [style]]))

(def calendar-styles
  (style [:.calendar {:background-color "#fff"
                      :border-radius "4px"}
          [:.header {:display "flex"
                     :width "100%"
                     :flex-direction "row"
                     :align-items "stretch"
                     :font-size "14px"
                     :line-height "20px"
                     :font-weight "bold"}
           [:div {:flex-grow 7
                  :flex-shrink 0
                  :background-color "rgb(249, 250, 251)"
                  :max-width (str (/ 100 7) "%")
                  :padding "7px"
                  :text-align "center"
                  :border-top "1px solid rgba(34,36,38,.15)"
                  :border-right "1px solid rgba(34,36,38,.15)"
                  :box-sizing "border-box"}
            [:&:first-child {:border-left "1px solid rgba(34,36,38,.15)"
                             :border-top-left-radius "4px"}]
            [:&:last-child {:border-top-right-radius "4px"}]]]
          [:.row {:position "relative"
                  :min-height "105px"}
           [:.colums {:position "absolute"
                      :display "flex"
                      :width "100%"
                      :flex-direction "row"
                      :align-items "stretch"
                      :height "100%"}]
           [:.segment {:position "relative"
                       :flex-grow 7
                       :flex-shrink 0
                       :height "100%"
                       :border-right "1px solid rgba(34,36,38,.15)"
                       :border-bottom "1px solid rgba(34,36,38,.15)"
                       :max-width (str (/ 100 7) "%")}
            [:&:first-child {:border-left "1px solid rgba(34,36,38,.15)"}]
            [:.date {:position "absolute"
                     :right "7px"
                     :top "6px"
                     :color "rgba(140,151,178,0.3)"
                     :font-size "12px"}]]
           [:&:first-child {}
            [:.segment {:border-top "1px solid rgba(34,36,38,.15)"}]]
           [:&:last-child {}
            [:.segment:first-child {:border-bottom-left-radius "4px"}]
            [:.segment:last-child {:border-bottom-right-radius "4px"}]]
           [:.segment._current-month [:.date {:color "#888A8C"}]]
           [:.segment._today [:.bg {:position "absolute"
                                    :left 0
                                    :top 0
                                    :right 0
                                    :bottom 0
                                    :background-color "rgba(140,151,178,0.17)"}]]
           [:.shifts {:position "relative"
                      :display "flex"
                      :width "100%"
                      :flex-direction "column"
                      :padding-top "24px"
                      :padding-bottom "7px"}]
           [:.shifts-row {:display "flex"
                          :width "100%"
                          :flex-direction "row"
                          :flex-wrap "wrap"}]]]))

(def calendar-controls-styles
  (style [:.calendar-controls {:display "flex"
                               :align-items "center"
                               :justify-content "center"}
          [:.button {:display "inline-block"
                     :color "#8C97B2"
                     :cursor "pointer"
                     :transition "color 0.2s"}
           [:&:hover {:color "#000"}]]
          [:.month-name {:font-size "18px"
                         :font-weight "bold"
                         :letter-spacing "-0.12px"
                         :margin "0 10px"}]]))
