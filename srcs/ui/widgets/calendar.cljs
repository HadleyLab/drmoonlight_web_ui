(ns ui.widgets.calendar
  (:require
   [reagent.core :as reagent]
   [ui.db.misc :refer (>event <sub)]
   [ui.pages :as pages]
   [ui.routes :refer [href]]
   [ui.widgets :refer [concatv]]
   [re-frame.core :as rf]
   [clojure.string :as str]
   [soda-ash.core :as sa]
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

(def days-of-week
  ["Sun" "Mon" "Tue" "Wed" "Thu" "Fri" "Sat"])

; Start week 6, end week 10 => number of weeks in month (10 - 6) + 1 = 5
; Special case for end of a year:
; Last day of a month can be placed on first week of next year.
; In this case last week of a year will be dropped, and we should add it in weeks sum.
; Start week 48, end week 1, weeks in year 52 => number of weeks in month (52 - 48) + 2 = 6
(defn get-weeks-number [month]
  (let [start-of-month (.startOf (js/moment month) "month")
        end-of-month (.endOf (js/moment month) "month")
        start-week (.week start-of-month)
        end-week (.week end-of-month)
        weeks-in-year (.weeksInYear (js/moment month))]
    (if (< start-week end-week)
      (+ (- end-week start-week) 1)
      (+ (- weeks-in-year start-week) 2))))

(defn get-week-bounds [current-month week-index]
  (let [month-days-offset (.weekday current-month)
        weeks-number (get-weeks-number current-month)
        is-first-week (= week-index 0)
        is-last-week (= week-index weeks-number)

        first-week-start (.subtract (js/moment current-month) month-days-offset "days")
        last-week-end (.add (js/moment current-month) (- 6 month-days-offset) "days")

        week-offset (* 7 week-index)
        week-start-date (if is-first-week
                          first-week-start
                          (.add (js/moment first-week-start) week-offset "days"))
        week-end-date (if is-last-week
                        last-week-end
                        (.add (js/moment first-week-start) (+ week-offset 6) "days"))]
    {:week-start (.startOf week-start-date "day")
     :week-end (.startOf week-end-date "day")}))

(defn get-week-shifts [{:keys [week-start week-end]}]
  (let [plain-shifts-list (<sub [:plain-shifts-list])]
    (filter (fn [shift]
              (let [date-start (js/moment (:date-start shift))
                    date-end (js/moment (:date-end shift))
                    shift-start-on-this-week (.isBetween date-start week-start week-end "day" "[]")
                    shift-ends-on-this-week (.isBetween date-end week-start week-end "day" "[]")
                    shift-includes-the-hole-week (and
                                                  (.isBetween week-start date-start date-end "day" "[]")
                                                  (.isBetween week-end date-start date-end "day" "[]"))]
                (or shift-start-on-this-week
                    shift-ends-on-this-week
                    shift-includes-the-hole-week)))
            plain-shifts-list)))

(defn prepare-shifts-data [shifts {:keys [week-start week-end]}]
  (mapv (fn [shift]
          (let [date-end (js/moment (:date-end shift))
                date-start (js/moment (:date-start shift))
                ends-on-this-week (.isBetween date-end week-start week-end "day" "[]")
                starts-on-this-week (.isBetween date-start week-start week-end "day" "[]")
                end (if ends-on-this-week (.startOf date-end "day") week-end)
                start (if starts-on-this-week (.startOf date-start "day") week-start)
                length (+ (.diff end start "days") 1)]
            (merge shift
                   {:draw-data {:length length
                                :offset-left (.diff start week-start "days")
                                :offset-right (.diff week-end end "days")
                                :starts-on-prev-week (not starts-on-this-week)
                                :ends-on-next-week (not ends-on-this-week)}})))
        shifts))

(defn draw-shift [shift ShiftLabel]
  (let [draw-data (:draw-data shift)
        {:keys [length offset-left offset-right]} draw-data]
    [:div {:style {:width (str (* (/ length 7) 100) "%")
                   :margin-left (str (* (/ offset-left 7) 100) "%")
                   :margin-right (str (* (/ offset-right 7) 100) "%")
                   :padding "0 7px"}}
     [ShiftLabel shift
      (select-keys draw-data
                   [:starts-on-prev-week :ends-on-next-week])]]))

(defn render-calendar-cell [current-month {:keys [week-start]} day-index]
  (let [current-date (.add (js/moment week-start) day-index "days")
        is-current-month (.isSame (js/moment current-month) current-date "month")
        is-today (.isSame (js/moment) current-date "day")]
    [:div {:class-name
           (str "segment"
                (when is-current-month " _current-month")
                (when is-today " _today"))}
     [:div.bg]
     [:div.date (.date current-date)]]))

(defn Calendar [current-month shifts ShiftLabel]
  (fn [current-month shifts ShiftLabel]
    [:div {:style {:padding-bottom "100px"}}
     [:div.calendar calendar-styles
      (concatv [:div.header] (mapv (fn [day] [:div day]) days-of-week))
      (concatv [:div]
               (mapv (fn [week-index]
                       (let [week-bounds (get-week-bounds current-month week-index)]
                         [:div.row
                          (concatv [:div.colums]
                                   (mapv (fn [day-index]
                                           [render-calendar-cell current-month week-bounds day-index])
                                         (range 7)))
                          [:div.shifts
                           (let [week-shifts (get-week-shifts week-bounds)
                                 prepared-shifts (prepare-shifts-data week-shifts week-bounds)]
                             (when-not (empty? prepared-shifts)
                               (concatv [:div.shifts-row]
                                        (mapv #(draw-shift % ShiftLabel) prepared-shifts))))]]))
                     (range (get-weeks-number current-month))))]]))

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

(defn CalendarMonthControl [parent]
  (into parent
        [[:div.calendar-controls calendar-controls-styles
          [:div.button {:on-click (>event [:dec-calendar-month])}
           [sa/Icon {:name "angle left" :size "big"}]]
          [:span.month-name (<sub [:calendar-month-formated])]
          [:div.button {:on-click (>event [:inc-calendar-month])}
           [sa/Icon {:name "angle right" :size "big"}]]]
         [sa/GridColumn {:width 6}]]))
