(ns ui.db.constants
  (:require
   [ui.db.misc :refer [get-url <sub]]
   [re-frame.core :as rf]))

(def schema
  {::constants {:status :loading}})

(defn list->map [data]
  (into {} (map (fn [{pk :pk :as item}] [pk item]) data)))

(rf/reg-event-fx
 :load-constants
 (fn [{db :db}]
   {:json/fetch->path {:path [::constants]
                       :uri (get-url db "/api/constants/")
                       :map-result #(into {} (map (fn [[key values]] [key (list->map values)]) %))}}))

(defn as-options [get-text data]
  (map (fn [[key value]] {:key key :text (get-text value) :value key}) data))

(rf/reg-sub
 :residency-program
 (fn [db [_ id]]
   (if (nil? id)
     (get-in db [::constants :data :residency-program] [])
     (get-in db [::constants :data :residency-program id]))))

(rf/reg-sub
 :residency-program-name
 (fn [db [_ id]]
   (get-in db [::constants :data :residency-program id :name])))

(rf/reg-sub
 :residency-program-as-options
 #(rf/subscribe [:residency-program])
 #(as-options :name %))

(rf/reg-sub
 :speciality
 (fn [db [_ id]]
   (if (nil? id)
     (get-in db [::constants :data :speciality] [])
     (get-in db [::constants :data :speciality id]))))

(rf/reg-sub
 :speciality-name
 (fn [db [_ id]]
   (get-in db [::constants :data :speciality id :name])))

(rf/reg-sub
 :speciality-as-options
 #(rf/subscribe [:speciality])
 #(as-options :name %))
