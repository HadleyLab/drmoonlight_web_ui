(ns ui.scheduler.message
  (:require
   [reagent.core :as reagent]
   [ui.db.misc :refer [>event >atom <sub get-url reduce-statuses text-with-br]]
   [ui.db.shift :refer [as-apply-date-time as-hours-interval]]
   [ui.db.application :refer [application-statuses]]
   [ui.pages :as pages]
   [ui.routes :refer [href]]
   [ui.scheduler.layout :refer [SchedulerLayout]]
   [ui.widgets.message :refer [Discussion]]
   [re-frame.core :as rf]
   [clojure.string :as str]
   [soda-ash.core :as sa]
   [ui.scheduler.schedule.core]))

(defn ShowAllApplications [params]
  (rf/dispatch [:get-applications])
  (fn [params]
    (let [applications (<sub [:applications])
          filter-state (<sub [:applications-filter-state])]
      [SchedulerLayout
       [sa/Grid {}
        [sa/GridRow {}
         [sa/GridColumn {:width 3}
          (doall (for [[state-key state] application-statuses]
                   [:div {:key state}
                    [sa/Label {:active (= state-key filter-state)
                               :on-click (>event [:set-applications-filter-state state-key])}
                     state
                     [sa/LabelDetail (<sub [:count-applications-by-state state-key])]]
                    [:br]
                    [:br]]))]
         [sa/GridColumn {:width 13}
          [sa/SegmentGroup
           (doall (for [application (:data applications)
                 :let [{{speciality :speciality
                         start :date-start
                         finish :date-end
                         pk :pk
                         payment-amount :payment-amount
                         payment-per-hour :payment-per-hour
                         description :description
                         :as shift} :shift} application]
                 :when (if (nil? filter-state)
                         true
                         (= filter-state (:state application)))]
             [sa/Segment {:key (:pk application)}
              [sa/Grid
               [sa/GridColumn {:width 4}
                [sa/Header (:name (<sub [:speciality speciality]))]
                [:p [:strong "Starts: "] (as-apply-date-time start)]
                [:p [:strong "Ends: "] (as-apply-date-time finish)]
                [:p [:strong "Total: "] (as-hours-interval start finish) " hours"]
                [:p [:strong "Payment amount: "] (str "$" payment-amount " per " (if payment-per-hour "hour" "shift"))]]
               [sa/GridColumn {:width 9} description]
               [sa/GridColumn {:width 3}
                [sa/Button
                 {:on-click (>event [:goto :scheduler :messages pk :discuss (:pk application)])}
                 "Go to Chat"]]]]))]]]]])))

(defn ApplicationLayout [application-info content]
  (let [{status :status application :data} application-info
        data-is-loading (or (and (nil? application) (= status :loading)) (= status :not-asked))]
  [SchedulerLayout
   [sa/Grid {}
    [sa/GridRow {}
     [sa/GridColumn {:width 3}
      [sa/Segment
       [sa/Dimmer {:active  data-is-loading} [sa/Loader]]
       (when (not data-is-loading)
         (let [{owner :owner} application]
           [:div
            [sa/Header (str (:first-name owner) " " (:last-name owner))]]))]]
     [sa/GridColumn {:width 13 :class-name :moonlight-white}
      content]]]]))

(defn DiscussShift [{application-pk :application-pk shift-pk :shift-pk}]
  (rf/dispatch [:get-shift-info shift-pk])
  (rf/dispatch [:get-application-info application-pk])
  (rf/dispatch [:get-application-messages application-pk])
  (fn [{application-pk :application-pk shift-pk :shift-pk}]
    (let [{shift-status :status shift :data} (<sub [:shift-info shift-pk])
          {application-status :status application :data :as application-info} (<sub [:application-info application-pk])
          {messages-status :status messages :data} (<sub [:application-messages application-pk])
          status (reduce-statuses shift-status application-status messages-status)]
      [ApplicationLayout application-info [Discussion shift application messages status]])))

(pages/reg-page :core/scheduler-messages ShowAllApplications)
(pages/reg-page :core/scheduler-messages-discuss DiscussShift)
