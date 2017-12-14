(ns ui.resident.message
  (:require
   [reagent.core :as reagent]
   [ui.db.misc :refer [>event >atom <sub get-url reduce-statuses text-with-br]]
   [ui.db.shift :refer [as-apply-date-time as-hours-interval]]
   [ui.db.application :refer [application-statuses]]
   [ui.pages :as pages]
   [ui.routes :refer [href]]
   [ui.widgets :refer [concatv]]
   [ui.widgets.message :refer [Discussion]]
   [ui.resident.layout :refer [ResidentLayout ResidentProfileLayout]]
   [re-frame.core :as rf]
   [clojure.string :as str]
   [soda-ash.core :as sa]))

(defn ApplyForm [shift-pk name]
  (let [apply-for-shift-result (<sub [:apply-for-shift shift-pk])]
    [sa/Segment
     (when (= (:status apply-for-shift-result) :failure)
       [sa/Message {:negative true}
        [sa/MenuHeader "Error"]
        [:p (str (:errors apply-for-shift-result))]])
     [sa/Form
      [:p (str "I would like to apply for the " name " shift")]
      [sa/FormButton {:color :green :on-click (>event [:apply-for-shift shift-pk])} "Apply"]]]))

(defn ShiftLayout [shift-pk content]
  (let [{status :status shift :data} (<sub [:shift-info shift-pk])
        data-is-loading (or (and (nil? shift) (= status :loading)) (= status :not-asked))]
    [ResidentLayout
     [sa/Grid {}
      [sa/GridRow {}
       [sa/GridColumn {:width 3}
        [sa/Segment
         [sa/Dimmer {:active  data-is-loading} [sa/Loader]]
         (when (not data-is-loading)
           (let [{speciality :speciality
                  start :date-start
                  finish :date-end
                  pk :pk
                  payment-amount :payment-amount
                  payment-per-hour :payment-per-hour
                  description :description} shift]
             [:div
              [sa/Header (:name (<sub [:speciality speciality]))]
              [:p [:strong "Starts: "] (as-apply-date-time start)]
              [:p [:strong "Ends: "] (as-apply-date-time finish)]
              [:p [:strong "Total: "] (as-hours-interval start finish) " hours"]
              [:p [:strong "Payment amount: "] (str "$" payment-amount " per " (if payment-per-hour "hour" "shift"))]
              [:p description]]))]]
       [sa/GridColumn {:width 13 :class-name :moonlight-white}
        content]]]]))

(defn ApplyToShift [{shift-pk :shift-pk}]
  (rf/dispatch [:get-shift-info shift-pk])
  (fn [{shift-pk :shift-pk}]
    (let [{status :status shift :data} (<sub [:shift-info shift-pk])]
      [ShiftLayout shift-pk
       [sa/SegmentGroup
        (when (= status :succeed)
          [ApplyForm
           shift-pk
           (:name (<sub [:speciality (:speciality shift)]))])]])))

(defn DiscussShift [{application-pk :application-pk shift-pk :shift-pk}]
  (rf/dispatch [:get-shift-info shift-pk])
  (rf/dispatch [:get-application-info application-pk])
  (rf/dispatch [:get-application-messages application-pk])
  (fn [{application-pk :application-pk shift-pk :shift-pk}]
    (let [{shift-status :status shift :data} (<sub [:shift-info shift-pk])
          {application-status :status application :data} (<sub [:application-info application-pk])
          {messages-status :status messages :data} (<sub [:application-messages application-pk])
          status (reduce-statuses shift-status application-status messages-status)]
      [ShiftLayout shift-pk [Discussion shift application messages status]])))

(defn format-date-time [start finish]
  (let [start-date (.format start "DD/MM")
        finish-date (.format finish "DD/MM")
        start-time (.format start "h:mm a")
        finish-time (.format finish "h:mm a")]
    (if (= start-date finish-date)
      (str " at " start-date " from " start-time " to " finish-time)
      (str " from " start-date " at " start-time " to " finish-date " at " finish-time))))

(defn ShowAllApplications [params]
  (rf/dispatch [:get-applications])
  (fn [params]
    (let [applications (<sub [:applications])
          filter-state (<sub [:applications-filter-state])]
      [ResidentLayout
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
                                :as shift} :shift
                               date-created :date-created
                               state :state
                               {message-text :text
                                :as last-message} :last-message} application]
                        :when (if (nil? filter-state)
                                true
                                (= filter-state (:state application)))]

                    [sa/Segment {:key (:pk application)
                                 :class-name "messages__message-container"
                                 :on-click (>event [:goto :resident :messages pk :discuss (:pk application)])}
                     (.log js/console "application" application)
                     [sa/Grid {:class-name "messages__message-grid"}
                      [sa/GridColumn {:width 3}
                       [:div.gray-font (.fromNow (js/moment date-created))]]
                      [sa/GridColumn {:width 10}
                       [:b (:name (<sub [:speciality speciality])) (format-date-time start finish)]
                       (if message-text [:div.messages__message-text message-text])]
                      [sa/GridColumn {:width 3}
                       (map
                        (fn [[key value]] (if (= key state) value))
                        application-statuses)]]]))]]]]])))

(pages/reg-page :core/resident-messages ShowAllApplications)
(pages/reg-page :core/resident-messages-apply ApplyToShift)
(pages/reg-page :core/resident-messages-discuss DiscussShift)
