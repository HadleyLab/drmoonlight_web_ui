(ns ui.db.shift
  (:require
   [ui.db.misc :refer [get-url <sub]]
   [re-frame.core :as rf]
   [cljs-time.core :as dt]
   [cljs-time.format :as format]))

(def schema
  {::shift
   {:list {:status :not-asked}
    :detail {}
    :filter-value nil
    :apply-requests {}}})

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

;; Private utils

(defn- get-date [date-time]
  (try
    (let [date-format "yyyy-MM-dd"]
      (format/unparse (format/formatter date-format) date-time))
    (catch js/Object e "")))

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

(rf/reg-event-fx
 :get-shift-info
 (fn [{db :db} [_ shift-pk]]
   {:json/fetch->path {:path [::shift :detail shift-pk]
                       :uri (get-url db "/api/shifts/shift/" shift-pk "/")
                       :token (<sub [:token])
                       :map-result parse-date-time}}))

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
