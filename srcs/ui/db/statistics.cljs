(ns ui.db.statistics
  (:require
   [ui.db.misc :refer [get-url <sub]]
   [re-frame.core :as rf]))

(def statistic-field-names-map
  {:available-shifts-count "Number of available shifts"
   :average-shifts-per-months-count "Average shifts per month"
   :residents-count "Number of residents"
   :required-specialities-top "Most required specialities"
   :total-earned-money-amount "Total money earned"
   :last-month-earned-money-amount "Money earned for the last month"
   :last-month-worked-residents-count "Number of residents worked last month"})

(def schema
  {::statistics {:status :loading}})

(rf/reg-event-fx
 :load-statistics
 (fn [{db :db}]
   {:json/fetch->path {:path [::statistics]
                       :uri (get-url db "/api/statistics/")}}))

(rf/reg-sub
 :statistics-data
 (fn [db [_ id]]
   (get-in db [::statistics :data])))
