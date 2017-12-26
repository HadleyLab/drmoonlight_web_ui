(ns ui.db.shift
  (:require
   [ui.db.misc :refer [get-url <sub fields->schema setup-form-initial-values]]
   [re-frame.core :as rf]))

(def shift-form-fields
  {"" {:date-start
       {:type :date-time-picker
        :label "Starts"
        :date-format "DD/MM/YYYY"}
       :date-end
       {:type :date-time-picker
        :label "Ends"
        :date-format "DD/MM/YYYY"}}
   "Required staff" {:speciality
                     {:type :select
                      :label "Speciality name"
                      :items #(<sub [:speciality-as-options])}

                     :payment-amount {:type :input-with-drop-down
                                      :label "Payment amount, $"
                                      :drop-down {:cursor :payment-per-hour
                                                  :options [{:key "true" :value "true" :text "hourly"}
                                                            {:key "false" :value "false" :text "per shift"}]}
                                      :width 5}
                     :payment-per-hour {:type :mock}
                     :description {:type :textarea :label "Description" :desc "optional"}
                     :residency-program {:type :select
                                         :label "Residency program"
                                         :items #(<sub [:residency-program-as-options])
                                         :desc "optional"}
                     :residency-years-required {:type :input
                                                :label "Residency years required"
                                                :width 3
                                                :desc "optional"}}})

(def shift-form-default-values
  {:date-start (.set (js/moment) "minute" 0)
   :date-end (.set (js/moment) "minute" 0)
   :payment-per-hour "false"})

(def schema
  {::shift
   {:list {:status :not-asked}
    :detail {}
    :filter-value nil
    :apply-requests {}
    :new-shift-modal false
    :edit-shift-modal {}
    :edit-shift-popup {}
    :shift-form
    {:fields (fields->schema shift-form-fields shift-form-default-values)
     :response {:status :not-asked}}}})

;; Public utils

(defn as-apply-date-time [date-time]
  (.format date-time))

(defn as-hours-interval [start finish]
  (/ (.diff finish start "minutes") 60))

(defn parse-date-time [shift]
  (let [date-keys #{:date-start :date-end :date-created :date-modified}]
    (into {} (map (fn [[key value]]
                    (if (date-keys key) [key (js/moment value)]
                        [key value])) shift))))

;; Private utils

(defn- get-date [date-time]
  (.format date-time "YYYY-MM-DD"))

(defn- convert-shifts [data]
  (group-by (comp get-date :date-start) (map parse-date-time data)))

;; API for calendar

(rf/reg-event-fx
 :load-shifts
 (fn [{db :db} [_]]
   {:json/fetch->path {:path [::shift :list]
                       :uri (get-url db "/api/shifts/shift/")
                       :token @(rf/subscribe [:token])
                       :map-result convert-shifts}}))

(rf/reg-sub
 ::shift
 (fn [db path]
   (get-in db path)))

(rf/reg-sub
 :shifts
 #(<sub [::shift :list]))

(rf/reg-sub
 :shifts-count-by-state
 #(into {}
        (map (fn [[key value]] [key (count value)])
             (group-by identity
                       (mapcat (fn [[key values]]
                                 (map :state values))
                               (<sub [::shift :list :data]))))))
(rf/reg-sub
 :shifts-filtered-by-state
 (fn [db [_ filter-state]]
   (let [shifts (<sub [:shifts])]
     (if (nil? filter-state) shifts
         {:status (:status shifts)
          :data (into {}
                      (map
                       (fn [[key values]]
                         [key (filter #(= filter-state (:state %)) values)])
                       (:data shifts)))}))))

(rf/reg-sub
 :shifts-filter-state
 #(<sub [::shift :filter-value]))

(rf/reg-event-db
 :set-shifts-filter-state
 (fn [db [_ state]]
   (let [new-state (if (= state (<sub [:shifts-filter-state]))
                     nil state)]
     (assoc-in db [::shift :filter-value] new-state))))

(rf/reg-sub
 :shifts-status
 #(<sub [::shift :list :status]))

(rf/reg-sub
 :shifts-data
 #(<sub [::shift :list :data]))

;; API for verbose shift information

(defn prepare-payment-per-hour [shift]
  (let [payment-per-hour (:payment-per-hour shift)]
    (merge shift {:payment-per-hour (if payment-per-hour "true" "false")})))

(defn prepare-shift-data [shift]
  (-> shift
      (prepare-payment-per-hour)
      (parse-date-time)))

(rf/reg-event-fx
 :get-shift-info
 (fn [{db :db} [_ shift-pk fx]]
   {:json/fetch->path {:path [::shift :detail shift-pk]
                       :uri (get-url db "/api/shifts/shift/" shift-pk "/")
                       :token (<sub [:token])
                       :map-result prepare-shift-data
                       :succeed-fx fx}}))

(rf/reg-sub
 :shift-info
 (fn [db [_ shift-pk]]
   (or (<sub [::shift :detail shift-pk])
       {:status :not-asked})))

(rf/reg-event-fx
 :apply-for-shift
 (fn [{db :db} [_ shift-pk]]
   {:json/fetch->path {:path [::shift :apply-requests shift-pk]
                       :uri (get-url db "/api/shifts/application/apply/")
                       :method "POST"
                       :token @(rf/subscribe [:token])
                       :body {:shift shift-pk}
                       :succeed-fx (fn [data] [:goto :resident :messages shift-pk :discuss (:pk data)])}}))

(rf/reg-sub
 :apply-for-shift
 (fn [db [_ shift-pk]]
   (or (<sub [::shift :apply-requests shift-pk])
       {:status :not-asked})))

; --------------------------------------------------

(rf/reg-sub
 :new-shift-modal
 #(<sub [::shift :new-shift-modal]))

(rf/reg-event-db
 :open-new-shift-modal
 (fn [db _]
   (assoc-in db [::shift :new-shift-modal] true)))

(rf/reg-event-db
 :close-new-shift-modal
 (fn [db _]
   (assoc-in db [::shift :new-shift-modal] false)))

(rf/reg-sub
 :edit-shift-modal
 (fn [db [_ pk]]
   (get-in db [::shift :edit-shift-modal pk] false)))

(rf/reg-event-db
 :open-edit-shift-modal
 (fn [db [_ pk]]
   (assoc-in db [::shift :edit-shift-modal pk] true)))

(rf/reg-event-db
 :close-edit-shift-modal
 (fn [db [_ pk]]
   (assoc-in db [::shift :edit-shift-modal pk] false)))

(rf/reg-sub
 :edit-shift-popup
 (fn [db [_ pk]]
   (get-in db [::shift :edit-shift-popup pk] false)))

(rf/reg-event-db
 :open-edit-shift-popup
 (fn [db [_ pk]]
   (assoc-in db [::shift :edit-shift-popup pk] true)))

(rf/reg-event-db
 :close-edit-shift-popup
 (fn [db [_ pk]]
   (assoc-in db [::shift :edit-shift-popup pk] false)))

(rf/reg-sub
 :new-shift-form-cursor
 #(<sub [:cursor [::shift :shift-form]]))

(rf/reg-event-db
 :init-new-shift-form
 (fn [db [_]]
   (assoc-in db [::shift :shift-form :fields]
             (fields->schema shift-form-fields shift-form-default-values))))

(rf/reg-event-fx
 :init-edit-shift-form
 (fn [db [_ shift-pk]]
   {:dispatch [:get-shift-info shift-pk [:load-initial-form-data shift-pk]]}))

(rf/reg-event-db
 :load-initial-form-data
 (fn [db [_ pk]]
   (-> db
       (assoc-in  [::shift :shift-form]
                  (get-in schema [::shift :shift-form]))
       (update-in [::shift :shift-form :fields]
                  (setup-form-initial-values (:data (<sub [:shift-info pk])))))))

(rf/reg-sub
 :shift-form-response
 #(<sub [::shift :shift-form :response]))

(defn shift->api-data [shift]
  (-> shift
      (update-in [:residency-years-required] #(if (= "") 0 %))))

(rf/reg-event-fx
 :create-new-shift
 (fn [{db :db} [_]]
   {:json/fetch->path {:path [::shift :shift-form :response]
                       :uri (get-url db "/api/shifts/shift/")
                       :method "POST"
                       :token (<sub [:token])
                       :body (shift->api-data (<sub [::shift :shift-form :fields]))
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
   {:json/fetch->path {:path [::shift :shift-form :response]
                       :uri (get-url db "/api/shifts/shift/" pk "/")
                       :method "PUT"
                       :token (<sub [:token])
                       :body (shift->api-data (<sub [::shift :shift-form :fields]))
                       :succeed-fx [:update-shift-succeed pk]}}))

(rf/reg-event-fx
 :delete-shift
 (fn [{db :db} [_ pk]]
   {:json/fetch->path {:path [::shift :list]
                       :uri (get-url db "/api/shifts/shift/" pk "/")
                       :method "DELETE"
                       :token (<sub [:token])
                       :succeed-fx [:load-shifts]}}))

(rf/reg-event-fx
 :update-shift-succeed
 (fn [{db :db} [_ pk]]
   {:dispatch-n [[:load-shifts]
                 [:close-edit-shift-modal pk]
                 [:init-new-shift-form]]}))
