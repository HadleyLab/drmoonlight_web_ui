(ns ui.db.statistics
  (:require
   [ui.db.misc :refer [get-url <sub]]
   [re-frame.core :as rf]))

(def schema
  {::statistics {:status :loading}})

(rf/reg-event-fx
 :load-statistics
 (fn [{db :db}]
   {:json/fetch->path {:path [::statistics]
                       :uri (get-url db "/api/statistics/")}}))
