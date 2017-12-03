(ns ui.db.calendar
  (:require
   [re-frame.core :as rf]
   [ui.db.misc :refer [<sub]]))

(def schema
  {::calendar
   {:month (.date (js/moment) 1)}})

(rf/reg-sub
 ::calendar
 (fn [db path]
   (get-in db path)))

(rf/reg-sub
 :calendar-month
 #(<sub [::calendar :month]))

(rf/reg-sub
 :calendar-month-formated
 #(.format (<sub [::calendar :month]) "MMMM YYYY"))



(rf/reg-event-db
 :inc-calendar-month
 (fn [db [_ month]]
   (update-in db [::calendar :month] #(.add (js/moment %) 1 "month"))))

(rf/reg-event-db
 :dec-calendar-month
 (fn [db [_ month]]
   (update-in db [::calendar :month] #(.subtract (js/moment %) 1 "month"))))

