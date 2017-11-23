(ns ui.scheduler.schedule.core
  (:require
   [reagent.core :as reagent]
   [ui.pages :as pages]
   [ui.routes :refer [href]]
   [ui.widgets :refer [concatv BuildForm fields->schema]]
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
   "Requid staff" {:speciality "Speciality name"
                   :payment-amount {:type :input-with-drop-down
                                   :label "Payment amount, $"
                                   :drop-down {:cursor :payment-per-hour
                                               :options [{:key true :value true :text "hourly"}
                                                         {:key false :value false :text "per shift"}]}}
                   :payment-per-hour {:type :mock}
                   :description {:type :textarea :label "Description"}
                   :residency-program "Residency Program"
                   :residency-years-required "Residency years required"}})

(defn schema []
  {:selected (dt/date-time (dt/year (dt/now)) (dt/month (dt/now)) 1)
   :shift-form
   {:fields (fields->schema shift-form-fields)
    :response {:status :not-asked}}
   :shifts {:status :not-asked}})

(defn ShiftLabel [{title :title start :start finish :finish total :total pk :pk}]
  [:div
   [sa/Label {:color :blue} title] [:br] [:br]])

(defn CreateNewShift [new-shift-form-cursor]
  [sa/Modal {:trigger (reagent/as-element [sa/Button {:color :blue} [sa/Icon {:name :plus}] "Create new shift"])
             :dimmer :blurring
             :size :small}
   [sa/ModalHeader "Create a new shift"]
   [sa/ModalContent {:image true}
    [sa/ModalDescription
     [sa/Form {}
      [BuildForm new-shift-form-cursor shift-form-fields]]]]
   [sa/ModalActions [sa/Button {:color :blue} "Done"]]])

(defn Index [params]
  (rf/dispatch-sync [::init-scheduler-shedule-page])
  (let [selected-cursor @(rf/subscribe [:cursor [root-path :selected]])
        shifts-cursor @(rf/subscribe [:cursor [root-path :shifts]])
        new-shift-form-cursor @(rf/subscribe [:cursor [root-path :shift-form]])]
    (fn [params]
      [SchedulerLayout
       [sa/Grid {}
        [sa/GridRow {}
         [sa/GridColumn {:width 3} [CreateNewShift new-shift-form-cursor]]
         [sa/GridColumn {:width 3}]
         [sa/GridColumn {:width 4}
          [sa/Button {:icon "angle left"
                      :on-click #(reset! selected-cursor (dt/minus @selected-cursor (dt/months 1)))}]
          [:span (format/unparse (format/formatter "MMMM YYYY") @selected-cursor)]
          [sa/Button {:icon "angle right"
                      :on-click #(reset! selected-cursor (dt/plus @selected-cursor (dt/months 1)))}]
          [sa/GridColumn {:width 6}]]]
        [sa/GridRow {}
         [sa/GridColumn {:width 3}]
         [sa/GridColumn {:width 13}
          [Calendar @selected-cursor @shifts-cursor ShiftLabel]]]]])))

(rf/reg-event-fx
 ::init-scheduler-shedule-page
 (fn [{db :db} [_]]
   {:db (if (= (root-path db) nil)
          (assoc-in db [root-path] (schema))
          db)
    :dispatch [::load-shifts]}))

(rf/reg-event-db
 ::load-shifts
 (fn [db [_]]
   (assoc-in db [root-path :shifts]
             {:status :succeed
              :data {"2017-11-30" [{:pk 1 :date (dt/date-time 2017 11 30) :title "doctor" :start "8pm" :finish "11pm" :total "6 hours"}
                                   {:pk 2 :date (dt/date-time 2017 11 30) :title "doctor" :start "8pm" :finish "11pm" :total "6 hours"}
                                   {:pk 3 :date (dt/date-time 2017 11 30) :title "doctor" :start "8pm" :finish "11pm" :total "6 hours"}]
                     "2017-12-01" [{:pk 4 :date (dt/date-time 2017 12 1) :title "doctor" :start "8pm" :finish "11pm" :total "6 hours"}]
                     "2017-12-02" [{:pk 5 :date (dt/date-time 2017 12 1) :title "doctor" :start "8pm" :finish "11pm" :total "6 hours"}]
                     "2017-12-03" [{:pk 6 :date (dt/date-time 2017 12 1) :title "doctor" :start "8pm" :finish "11pm" :total "6 hours"}]
                     "2017-12-04" [{:pk 7 :date (dt/date-time 2017 12 1) :title "doctor" :start "8pm" :finish "11pm" :total "6 hours"}]
                     "2017-12-05" [{:pk 8 :date (dt/date-time 2017 12 1) :title "doctor" :start "8pm" :finish "11pm" :total "6 hours"}]
                     "2017-12-06" [{:pk 9 :date (dt/date-time 2017 12 6) :title "doctor" :start "8pm" :finish "11pm" :total "6 hours"}
                                   {:pk 10 :date (dt/date-time 2017 12 6) :title "doctor" :start "8pm" :finish "11pm" :total "6 hours"}
                                   {:pk 11 :date (dt/date-time 2017 12 6) :title "doctor" :start "8pm" :finish "11pm" :total "6 hours"}]}})))

(pages/reg-page :core/scheduler-schedule Index)
