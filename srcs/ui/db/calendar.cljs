(ns ui.db.calendar
  (:require
   [re-frame.core :as rf]
   [ui.db.misc :refer [<sub]]
   [cljs-time.core :as dt]
   [cljs-time.format :as format]))

(def schema
  {::calendar
   {:month (dt/date-time (dt/year (dt/now)) (dt/month (dt/now)) 1)}})

(rf/reg-sub
 ::calendar
 (fn [db path]
   (get-in db path)))

(rf/reg-sub
 :calendar-month
 #(<sub [::calendar :month]))

(rf/reg-event-db
 :set-calendar-month
 (fn [db [_ month]]
   (assoc-in db [::calendar :month] month)))
