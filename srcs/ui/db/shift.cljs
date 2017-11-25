(ns ui.db.shift
  (:require
   [ui.db.misc :refer [get-url]]
   [cljs-time.format :as format]
   [re-frame.core :as rf]))

(defn- get-date [date-time]
  (try
    (let [date-format "yyyy-MM-dd"]
      (format/unparse (format/formatter date-format) (format/parse date-time)))
    (catch js/Object e "")))

(defn- convert-shifts [data]
  (group-by (comp get-date :date-start) data))

(rf/reg-event-fx
 :load-shifts
 (fn [{db :db} [_]]
   {:db (assoc-in db [:shifts :status] :loading)
    :json/fetch {:uri (get-url db "/api/shifts/shift/")
                 :token @(rf/subscribe [:token])
                 :success {:event :load-shifts-succeed}
                 :error {:event :load-shifts-failure}}}))

(rf/reg-event-db
 :load-shifts-succeed
 (fn [db [_ {data :data}]]
   (-> db
       (assoc-in [:shifts :status] :success)
       (assoc-in [:shifts :data] (convert-shifts data)))))

(rf/reg-event-db
 :load-shifts-failure
 (fn [db [_ {data :data}]]
   (-> db
       (assoc-in [:shifts :status] :failure)
       (assoc-in [:shifts :errors] data))))

(rf/reg-sub
 :shifts
 (fn [db _]
   (get-in db [:shifts])))

(rf/reg-sub
 :shifts-status
 (fn [db _]
   (get-in db [:shifts :stauts])))

(rf/reg-sub
 :shifts-data
 (fn [db _]
   (get-in db [:shifts :data])))
