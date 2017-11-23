(ns ui.scheduler.core
  (:require
   [reagent.core :as reagent]
   [ui.pages :as pages]
   [ui.routes :refer [href]]
   [ui.scheduler.layout :refer [SchedulerLayout]]
   [re-frame.core :as rf]
   [clojure.string :as str]
   [soda-ash.core :as sa]
   [ui.scheduler.schedule.core]))

(def root-path :scheduler)

(def schema {})

(defn with-init [render]
  (fn []
    (rf/dispatch-sync [::init-scheduler-page])
    render))

(rf/reg-event-db
 ::init-scheduler-page
 (fn [db [_]]
   (if (= (root-path db) nil)
     (assoc-in db [root-path] schema)
     db)))

(pages/reg-page :core/scheduler (with-init (fn [] [SchedulerLayout [sa/Header {} "index"]])))
(pages/reg-page :core/scheduler-statistics (with-init (fn [] [SchedulerLayout [sa/Header {} "statistics"]])))
(pages/reg-page :core/scheduler-messages (with-init (fn [] [SchedulerLayout [sa/Header {} "messages"]])))
(pages/reg-page :core/scheduler-profile (with-init (fn [] [SchedulerLayout [sa/Header {} "profile"]])))
