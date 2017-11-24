(ns ui.db.constants
  (:require
   [ui.db.misc :refer [get-url]]
   [re-frame.core :as rf]))

(rf/reg-event-fx
 :load-constants
 (fn [{db :db}] {:json/fetch {:uri (get-url db "/api/constants/")
                      :success {:event :constants-succeed}
                      :error {:event :constants-failure}}}))

(defn list->map [data]
  (into {} (map (fn [{pk :pk :as item}] [pk item]) data)))

(rf/reg-event-db
 :constants-succeed
 (fn [db [_ {data :data}]]
   (-> db
       (assoc-in [:constants :status] :succeed)
       (assoc-in [:constants :data] (into {} (map (fn [[key values]] [key (list->map values)]) data))))))

(rf/reg-event-db
 :constants-failure
 (fn [db [_ {data :data}]]
   (-> db
       (assoc-in [:constants :status] :failure)
       (assoc-in [:constants :error] :data))))

(defn as-options [get-text data]
  (map (fn [[key value]] {:key key :text (get-text value) :value key}) data))

(rf/reg-sub
 :residency-program
 (fn [db _]
   (get-in db [:constants :data :residency-program] [])))

(rf/reg-sub
 :residency-program-as-options
 #(rf/subscribe [:residency-program])
 #(as-options :name %))

(rf/reg-sub
 :speciality
 (fn [db _]
   (get-in db [:constants :data :speciality] [])))

(rf/reg-sub
 :speciality-as-options
 #(rf/subscribe [:speciality])
 #(as-options :name %))
