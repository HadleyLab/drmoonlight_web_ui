(ns ui.scheduler.core
  (:require
   [reagent.core :as reagent]
   [ui.pages :as pages]
   [ui.routes :refer [href]]
   [ui.scheduler.layout :refer [SchedulerLayout]]
   [re-frame.core :as rf]
   [clojure.string :as str]
   [soda-ash.core :as sa]
   [ui.scheduler.schedule.core]
   [ui.widgets.resident-profile-detail :refer [ResidentProfileDetail]]
   [ui.scheduler.message]))

(defn WrappedResidentProfileDetail [params]
  [SchedulerLayout [ResidentProfileDetail params]])

(pages/reg-page :core/scheduler (fn [] [SchedulerLayout [sa/Header {} "index"]]))
(pages/reg-page :core/scheduler-statistics (fn [] [SchedulerLayout [sa/Header {} "statistics"]]))
(pages/reg-page :core/scheduler-profile (fn [] [SchedulerLayout [sa/Header {} "profile"]]))
(pages/reg-page :scheduler/resident-profile-detail WrappedResidentProfileDetail)
