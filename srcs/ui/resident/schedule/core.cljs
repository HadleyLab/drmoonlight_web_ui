(ns ui.resident.schedule.core
  (:require
   [reagent.core :as reagent]
   [ui.db :refer [>event dispatch-set! get-url <sub]]
   [ui.db.shift :refer [as-apply-date-time as-hours-interval]]
   [ui.pages :as pages]
   [ui.routes :refer [href]]
   [ui.widgets :refer [concatv]]
   [ui.widgets.calendar :refer [Calendar]]
   [ui.resident.layout :refer [ResidentLayout]]
   [re-frame.core :as rf]
   [clojure.string :as str]
   [soda-ash.core :as sa]
   [cljs-time.core :as dt]
   [cljs-time.format :as format]))

(def root-path :resident-schedule-page)

(defn schema []
  {:selected (dt/date-time (dt/year (dt/now)) (dt/month (dt/now)) 1)})

(defn ShiftLabel [{speciality :speciality
                   start :date-start
                   finish :date-end
                   pk :pk
                   payment-amount :payment-amount
                   payment-per-hour :payment-per-hour
                   description :description}]
  (let [speciality-name (:name (<sub [:speciality speciality]))]
    [:div [sa/Popup
           {:trigger (reagent/as-element [sa/Label {:color :blue} speciality-name])
            :flowing true
            :position "left center"
            :hoverable true}
           [:div
            [:p [:strong "Starts: "] (as-apply-date-time start)]
            [:p [:strong "Ends: "] (as-apply-date-time finish)]
            [:p [:strong "Total: "] (as-hours-interval start finish) " hours"]
            [:p [:strong "Required staff: "] speciality-name]
            [:p [:strong "Payment amount: "] (str "$" payment-amount " per " (if payment-per-hour "hour" "shift"))]
            ;; [:p description] TODO fix max width
            [sa/Button {:on-click (>event [:goto :resident :messages pk])} "Apply"]]] [:br] [:br]]))

(defn Index [params]
  (rf/dispatch-sync [::init-resident-shedule-page])
  (let [selected-cursor @(rf/subscribe [:cursor [root-path :selected]])]
    (fn [params]
      [ResidentLayout
       [sa/Grid {}
        [sa/GridRow {}
         [sa/GridColumn {:width 6}]
         [sa/GridColumn {:width 4}
          [sa/Button {:icon "angle left"
                      :on-click #(dispatch-set! selected-cursor (dt/minus @selected-cursor (dt/months 1)))}]
          [:span (format/unparse (format/formatter "MMMM YYYY") @selected-cursor)]
          [sa/Button {:icon "angle right"
                      :on-click #(dispatch-set! selected-cursor (dt/plus @selected-cursor (dt/months 1)))}]
          [sa/GridColumn {:width 6}]]]
        [sa/GridRow {}
         [sa/GridColumn {:width 16}
          [Calendar @selected-cursor (<sub [:shifts]) ShiftLabel]]]]])))

(rf/reg-event-fx
 ::init-resident-shedule-page
 (fn [{db :db} [_]]
   {:db (if (= (root-path db) nil)
          (assoc-in db [root-path] (schema))
          db)
    :dispatch [:load-shifts]}))

(pages/reg-page :core/resident-schedule Index)
