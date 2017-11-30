(ns ui.db
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [akiroz.re-frame.storage :refer [reg-co-fx!]]
            [clojure.string :as str]
            [ui.db.misc :refer [insert-by-path]]
            [ui.db.account]
            [ui.db.application]
            [ui.db.constants]
            [ui.db.resident]
            [ui.db.shift]
            [clojure.string :refer [replace escape]]))

(reg-co-fx! :de-moonlight
            {:fx :store
             :cofx :store})

(rf/reg-sub-raw :route (fn [db _] (reaction (:route @db))))
(rf/reg-sub-raw :db (fn [db] db))
(rf/reg-sub-raw :cursor (fn [db [k path]] (reaction (reagent/cursor db path))))
(rf/reg-sub-raw :db/by-path
                (fn [db [_ path]]
                  (.log js/console "by-path" path)
                  (reaction (get-in @db path))))
(rf/reg-event-db
 :db/write
 (fn [db [_ path val]]
   (insert-by-path db path val)))

(rf/reg-event-fx
 :dispatch-fx
 (fn [_ [_ fx]]
   {:dispatch fx}))

(def initial-db
  (merge {}
         ui.db.account/schema
         ui.db.application/schema
         ui.db.constants/schema
         ui.db.resident/schema))
