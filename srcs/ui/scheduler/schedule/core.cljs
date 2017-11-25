(ns ui.scheduler.schedule.core
  (:require
   [reagent.core :as reagent]
   [ui.db :refer [dispatch-set! <sub >event get-url]]
   [ui.pages :as pages]
   [ui.routes :refer [href]]
   [ui.widgets :refer [concatv BuildForm fields->schema cljstime->drf-date-time]]
   [ui.widgets.calendar :refer [Calendar]]
   [ui.scheduler.layout :refer [SchedulerLayout]]
   [re-frame.core :as rf]
   [clojure.string :as str]
   [soda-ash.core :as sa]
   [cljs-time.core :as dt]
   [cljs-time.format :as format]))

(def root-path :scheduler-schedule-page)

(def shift-form-fields
  {"" {:date-start
       {:type :date-picker
        :label "Starts"
        :show-time-select true
        :time-format "HH:mm"
        :date-format "LLL"
        :time-intervals 15}
       :date-end
       {:type :date-picker
        :label "Ends"
        :show-time-select true
        :time-format "HH:mm"
        :date-format "LLL"
        :time-intervals 15}}
   "Requid staff" {:speciality
                   {:type :select
                    :label "Speciality name"
                    :items #(<sub [:speciality-as-options])}

                   :payment-amount {:type :input-with-drop-down
                                    :label "Payment amount, $"
                                    :drop-down {:cursor :payment-per-hour
                                                :options [{:key "true" :value "true" :text "hourly"}
                                                          {:key "false" :value "false" :text "per shift"}]}}
                   :payment-per-hour {:type :mock}
                   :description {:type :textarea :label "Description"}
                   :residency-program {:type :select
                                       :label "Residency Program"
                                       :items #(<sub [:residency-program-as-options])}
                   :residency-years-required "Residency years required"}})

(defn schema []
  {:selected (dt/date-time (dt/year (dt/now)) (dt/month (dt/now)) 1)
   :filter-state nil
   :shift-form
   {:fields (fields->schema shift-form-fields)
    :shift-modal-open false
    :response {:status :not-asked}}})

(defn ShiftLabel [{speciality :speciality}]
  [:div
   [sa/Label {:color :blue} (:name (<sub [:speciality speciality]))] [:br] [:br]])

(defn CreateNewShift [new-shift-form-cursor modal-cursor]
  [sa/Modal {:trigger (reagent/as-element [sa/Button {:color :blue
                                                      :on-click #(dispatch-set! modal-cursor true)}
                                           [sa/Icon {:name :plus}] "Create new shift"])
             :dimmer :blurring
             :open @modal-cursor
             :size :small}
   [sa/ModalHeader "Create a new shift"]
   [sa/ModalContent {:image true}
    [sa/ModalDescription
     [sa/Form {}
      [BuildForm new-shift-form-cursor shift-form-fields]]]]
   [sa/ModalActions [sa/Button {:color :blue :on-click (>event [:create-new-shift])} "Done"]]])

(defn Index [params]
  (rf/dispatch-sync [::init-scheduler-shedule-page])
  (let [selected-cursor @(rf/subscribe [:cursor [root-path :selected]])
        new-shift-form-cursor @(rf/subscribe [:cursor [root-path :shift-form]])
        modal-cursor @(rf/subscribe [:cursor [root-path :shift-form :shift-modal-open]])]
    (fn [params]
      (let [shifts (<sub [:shifts])
            state-count (into {}
                              (map (fn [[key value]] [key (count value)])
                                   (group-by identity
                                             (mapcat (fn [[key values]]
                                                       (map :state values))
                                                     (:data shifts)))))
            filter-state (<sub [:filter-state])
            filtered-shifts (if (nil? filter-state) shifts
                                {:status (:status shifts)
                                 :data (into {}
                                             (map
                                              (fn [[key values]]
                                                [key (filter #(= filter-state (:state %)) values)])
                                              (:data shifts)))})]
        [SchedulerLayout
         [sa/Grid {}
          [sa/GridRow {}
           [sa/GridColumn {:width 3} [CreateNewShift new-shift-form-cursor modal-cursor]]
           [sa/GridColumn {:width 3}]
           [sa/GridColumn {:width 4}
            [sa/Button {:icon "angle left"
                        :on-click #(dispatch-set! selected-cursor (dt/minus @selected-cursor (dt/months 1)))}]
            [:span (format/unparse (format/formatter "MMMM YYYY") @selected-cursor)]
            [sa/Button {:icon "angle right"
                        :on-click #(dispatch-set! selected-cursor (dt/plus @selected-cursor (dt/months 1)))}]
            [sa/GridColumn {:width 6}]]]
          [sa/GridRow {}
           [sa/GridColumn {:width 3}
            (for [state ["completed" "active" "without_applies" "coverage_completed" "require_approval"]]
              [:div {:key state}
               [sa/Label {:active (= state filter-state)
                          :on-click (>event [:set-filter-state state])}
                state
                [sa/LabelDetail (state-count state 0)]]
               [:br]
               [:br]])]
           [sa/GridColumn {:width 13}
            [Calendar @selected-cursor filtered-shifts ShiftLabel]]]]]))))

(rf/reg-sub
 :filter-state
 (fn [db]
   (get-in db [root-path :filter-state])))

(rf/reg-event-db
 :set-filter-state
 (fn [db [_ state]]
   (let [new-state (if (= state (<sub [:filter-state]))
                     nil state)]
     (assoc-in db [root-path :filter-state] new-state))))

(rf/reg-event-fx
 ::init-scheduler-shedule-page
 (fn [{db :db} [_]]
   {:db (if (= (root-path db) nil)
          (assoc-in db [root-path] (schema))
          db)
    :dispatch [:load-shifts]}))

(rf/reg-event-fx
 :create-new-shift
 (fn [{db :db} [_]]
   {:db (assoc-in db [root-path :shift-form :response :status] :loading)
    :json/fetch {:uri (get-url db "/api/shifts/shift/")
                 :method "POST"
                 :token (<sub [:token])
                 :body (-> db
                           (get-in [root-path :shift-form :fields])
                           (update-in [:date-start] cljstime->drf-date-time)
                           (update-in [:date-end] cljstime->drf-date-time))
                 :success {:event :create-new-shift-succeed}
                 :error {:event :create-new-shift-failure}}}))

(rf/reg-event-fx
 :create-new-shift-succeed
 (fn [{db :db} _]
   {:db (assoc-in db [root-path :shift-form] (:shift-form (schema)))
    :dispatch [:load-shifts]}))

(rf/reg-event-db
 :create-new-shift-failure
 (fn [db [_ {data :data}]]
   (-> db
       (assoc-in [root-path :shift-form :response :status] :failure)
       (assoc-in [root-path :shift-form :response :errors] data))))

(pages/reg-page :core/scheduler-schedule Index)
