(ns ui.db.shift
  (:require
   [ui.db.misc :refer [get-url]]
   [re-frame.core :as rf]
   [cljs-time.core :as dt]
   [cljs-time.format :as format]))

;; Private utils

(defn- get-date [date-time]
  (try
    (let [date-format "yyyy-MM-dd"]
      (format/unparse (format/formatter date-format) date-time))
    (catch js/Object e "")))

(defn- convert-shifts [data]
  (group-by (comp get-date :date-start) (map parse-date-time data)))

;; Public utils

(defn as-apply-date-time [date-time]
  (try
    (let [date-format "dd/MM/yyyy' at 'hh:mm a"]
      (format/unparse (format/formatter date-format) date-time))
    (catch js/Object e "")))

(defn as-hours-interval [start finish]
  (/
   (dt/in-minutes
    (dt/interval
     start
     finish))
   60))

(defn parse-date-time [shift]
  (let [date-keys #{:date-start :date-end :date-created :date-modified}]
    (into {} (map (fn [[key value]]
                    (if (date-keys key) [key (format/parse value)]
                        [key value])) shift))))

;; API for calendar

(rf/reg-event-fx
 :load-shifts
 (fn [{db :db} [_]]
   {:db (assoc-in db [:shifts :status] :loading)
    :json/fetch {:uri (get-url db "/api/shifts/shift/")
                 :token @(rf/subscribe [:token])
                 :success {:event :load-shifts-succeed}
                 :error {:event :load-shifts-failure}}}))

(rf/reg-event-db
 :load-shifts-succeed
 (fn [db [_ {data :data}]]
   (-> db
       (assoc-in [:shifts :status] :succeed)
       (assoc-in [:shifts :data] (convert-shifts data)))))

(rf/reg-event-db
 :load-shifts-failure
 (fn [db [_ {data :data}]]
   (-> db
       (assoc-in [:shifts :status] :failure)
       (assoc-in [:shifts :errors] data))))

(rf/reg-sub
 :shifts
 (fn [db _]
   (get-in db [:shifts])))

(rf/reg-sub
 :shifts-status
 (fn [db _]
   (get-in db [:shifts :stauts])))

(rf/reg-sub
 :shifts-data
 (fn [db _]
   (get-in db [:shifts :data])))

;; API for verbose shift information

(rf/reg-event-fx
 :get-shift-info
 (fn [{db :db} [_ shift-pk]]
   {:db (assoc-in db [:shifts-info shift-pk :status] :loading)
    :json/fetch {:uri (get-url db "/api/shifts/shift/" shift-pk "/")
                 :token @(rf/subscribe [:token])
                 :success {:event :get-shift-info-succeed :shift-pk shift-pk}
                 :error {:event :get-shift-info-failure :shift-pk shift-pk}}}))

(rf/reg-event-db
 :get-shift-info-succeed
 (fn [db [_ {data :data shift-pk :shift-pk}]]
   (-> db
       (assoc-in [:shifts-info shift-pk :status] :succeed)
       (assoc-in [:shifts-info shift-pk :data] (parse-date-time data)))))

(rf/reg-event-db
 :get-shift-info-failure
 (fn [db [_ {data :data shift-pk :shift-pk}]]
   (-> db
       (assoc-in [:shifts-info shift-pk :status] :failure)
       (assoc-in [:shifts-info shift-pk :errors] data))))

(rf/reg-sub
 :shift-info
 (fn [db [_ shift-pk]]
   (get-in db [:shifts-info shift-pk] {:status :not-asked})))

(rf/reg-event-fx
 :apply-for-shift
 (fn [{db :db} [_ shift-pk]]
   {:db (assoc-in db [:shift-apply-requests shift-pk :staus] :loading)
    :json/fetch {:uri (get-url db "/api/shifts/application/apply/")
                 :method "POST"
                 :token @(rf/subscribe [:token])
                 :body {:shift shift-pk}
                 :success {:event :apply-for-shift-succeed :shift-pk shift-pk}
                 :error {:event :apply-for-shift-failure :shift-pk shift-pk}}}))


(rf/reg-event-fx
 :apply-for-shift-succeed
 (fn [{db :db} [_ {data :data shift-pk :shift-pk}]]
   {:db (-> db
       (assoc-in [:apply-for-shift shift-pk :status] :succeed)
       (assoc-in [:apply-for-shift shift-pk :data] data))
    :dispatch [:goto :resident :messages shift-pk :discuss (:pk data)]}))

(rf/reg-event-db
 :apply-for-shift-failure
 (fn [db [_ {data :data shift-pk :shift-pk}]]
   (-> db
       (assoc-in [:apply-for-shift shift-pk :status] :failure)
       (assoc-in [:apply-for-shift shift-pk :errors] data))))

(rf/reg-sub
 :apply-for-shift
 (fn [db [_ shift-pk]]
   (get-in db [:apply-for-shift shift-pk] {:status :not-asked})))


