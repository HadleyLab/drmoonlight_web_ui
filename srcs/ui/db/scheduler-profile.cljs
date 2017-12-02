(ns ui.db.scheduler-profile
  (:require
   [reagent.core :as reagent]
   [ui.db.misc :refer [get-url <sub fields->schema cljstime->drf-date-time
                       setup-form-initial-values]]
   [ui.db.shift :refer [parse-date-time]]
   [re-frame.core :as rf]
   [cljs-time.core :as dt]
   [cljs-time.format :as format]))

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

(def schema
  {::scheduler-profile
   {:new-shift-modal false
    :edit-shift-modal {}
    :shift-form
    {:fields (fields->schema shift-form-fields)
     :response {:status :not-asked}}}})

(rf/reg-sub
 ::scheduler-profile
 (fn [db path]
   (get-in db path)))

(rf/reg-sub
 :new-shift-modal
 #(<sub [::scheduler-profile :new-shift-modal]))

(rf/reg-event-db
 :open-new-shift-modal
 (fn [db _]
   (assoc-in db [::scheduler-profile :new-shift-modal] true)))

(rf/reg-event-db
 :close-new-shift-modal
 (fn [db _]
   (assoc-in db [::scheduler-profile :new-shift-modal] false)))

(rf/reg-sub
 :edit-shift-modal
 (fn [db [_ pk]]
   (get-in db [::scheduler-profile :edit-shift-modal pk] false)))

(rf/reg-event-db
 :open-edit-shift-modal
 (fn [db [_ pk]]
   (assoc-in db [::scheduler-profile :edit-shift-modal pk] true)))

(rf/reg-event-db
 :close-edit-shift-modal
 (fn [db [_ pk]]
   (assoc-in db [::scheduler-profile :edit-shift-modal pk] false)))

(rf/reg-sub
 :new-shift-form-cursor
 #(<sub [:cursor [::scheduler-profile :shift-form]]))

(rf/reg-event-db
 :init-new-shift-form
 (fn [db [_]]
   (assoc-in db [::scheduler-profile :shift-form] (get-in schema [::scheduler-profile :shift-form]))))

(rf/reg-event-fx
 :init-edit-shift-form
 (fn [db [_ shift-pk]]
   {:dispatch [:get-shift-info shift-pk [:load-initial-form-data shift-pk]]}))

(rf/reg-event-db
 :load-initial-form-data
  (fn [db [_ pk]]
       (-> db
           (assoc-in  [::scheduler-profile :shift-form]
                      (get-in schema [::scheduler-profile :shift-form]))
           (update-in [::scheduler-profile :shift-form :fields]
                      (setup-form-initial-values (:data (<sub [:shift-info pk])))))))

(rf/reg-sub
 :shift-form-response
 #(<sub [::scheduler-profile :shift-form :response]))

(rf/reg-event-fx
 :create-new-shift
 (fn [{db :db} [_]]
   {:json/fetch->path {:path [::scheduler-profile :shift-form :response]
                       :uri (get-url db "/api/shifts/shift/")
                       :method "POST"
                       :token (<sub [:token])
                       :body (-> (<sub [::scheduler-profile :shift-form :fields])
                                 (update-in [:date-start] cljstime->drf-date-time)
                                 (update-in [:date-end] cljstime->drf-date-time))
                       :succeed-fx [:create-new-shift-succeed]}}))

(rf/reg-event-fx
 :create-new-shift-succeed
 (fn [{db :db} _]
   {:dispatch-n [[:load-shifts]
                 [:close-new-shift-modal]
                 [:init-new-shift-form]]}))


(rf/reg-event-fx
 :update-shift
 (fn [{db :db} [_ pk]]
   {:json/fetch->path {:path [::scheduler-profile :shift-form :response]
                       :uri (get-url db "/api/shifts/shift/" pk "/")
                       :method "PUT"
                       :token (<sub [:token])
                       :body (-> (<sub [::scheduler-profile :shift-form :fields])
                                 (update-in [:date-start] cljstime->drf-date-time)
                                 (update-in [:date-end] cljstime->drf-date-time))
                       :succeed-fx [:update-shift-succeed pk]}}))

(rf/reg-event-fx
 :update-shift-succeed
 (fn [{db :db} [_ pk]]
   {:dispatch-n [[:load-shifts]
                 [:close-edit-shift-modal pk]
                 [:init-new-shift-form]]}))

