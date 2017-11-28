(ns ui.db.application
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require
   [reagent.core :as reagent]
   [ui.db.misc :refer [get-url <sub]]
   [ui.db.shift :refer [parse-date-time]]
   [re-frame.core :as rf]))

(def application-statuses
  {1 "New"
   2 "Approved"
   3 "Rejected"
   4 "Postponed"
   5 "Confirmed"
   6 "Cancelled"
   7 "Failed"
   8 "Completed"})

(rf/reg-event-fx
 :get-applications
 (fn [{db :db} _]
   {:json/fetch->path {:path [:applications]
                       :uri (get-url db "/api/shifts/application/")
                       :token (<sub [:token])
                       :map-result (fn [data] (map #(update-in % [:shift] parse-date-time) data))}}))
(rf/reg-sub
 :applications
 (fn [db _]
   (get-in db [:applications] {:status :not-asked})))

(rf/reg-event-db
 :set-applications-filter-state
 (fn [db [_ state]]
   (let [current-state (<sub [:applications-filter-state])]
     (assoc db :applications-filter-state (if (= current-state state) nil state)))))

(rf/reg-sub
 :applications-filter-state
 (fn [db _]
   (get-in db [:applications-filter-state])))

(rf/reg-sub
 :count-applications-by-state
 (fn [db [_ state]]
   (count
    (filter
     #(= state (:state %))
     (get-in db [:applications :data] [])))))

(rf/reg-event-fx
 :get-application-info
 (fn [{db :db} [_ application-pk]]
   {:json/fetch->path {:path [:applications-info application-pk]
                       :uri (get-url db "/api/shifts/application/" application-pk "/")
                       :token (<sub [:token])}}))

(rf/reg-sub
 :application-info
 (fn [db [_ application-pk]]
   (get-in db [:applications-info application-pk] {:status :not-asked})))

(:available-transitions (:data (<sub [:application-info "37"])))





(rf/reg-sub
 :application-participant
 (fn [db [_ application-pk participant-id]]
   (let [scheduler (get-in db [:applications-info application-pk :data :shift :owner])
         resident (get-in db [:applications-info application-pk :data :owner])
         {first-name :first-name
          last-name :last-name} (if (= (:id scheduler) participant-id) scheduler resident)]
     (str first-name " " last-name))))

(rf/reg-event-fx
 :get-application-messages
 (fn [{db :db} [_ application-pk]]
   {:json/fetch->path {:path [:applications-messages application-pk]
                       :uri (get-url db "/api/shifts/application/" application-pk "/message/")
                       :token (<sub [:token])}}))

(rf/reg-sub
 :application-messages
 (fn [db [_ application-pk]]
   (get-in db [:applications-messages application-pk] {:status :not-asked})))

;; Comments

(rf/reg-event-db
 :init-comment-form
 (fn [db _]
   (assoc db :comment-form {:fields {:text ""} :response {:status :not-asked}})))

(rf/reg-sub-raw
 :comment-cursor
 (fn [db _]
   (reaction (reagent/cursor db [:comment-form :fields :text]))))

(rf/reg-sub
 :comment-form
 (fn [db _]
   (get-in db [:comment-form :response])))

(rf/reg-event-fx
 :add-comment
 (fn [{db :db} [_ application-pk transition]]
   {:json/fetch->path {:path [:comment-form :response]
                       :uri (get-url db "/api/shifts/application/" application-pk "/" transition "/")
                       :method "POST"
                       :token (<sub [:token])
                       :body {:text @(<sub [:comment-cursor])}}}))

(rf/reg-event-fx
 :application-state-changed
 (fn [{db :db} [_ {{pk :pk :as application} :application message :message}]]
   (merge
    {:db (assoc-in db [:applications-info (str pk)] {:status :succeed :data application})}
    (when-not (nil? message) {:dispatch [:message-created {:message message}]}))))

(rf/reg-event-db
 :message-created
 (fn [db [_ {message :message}]]
   (update-in db [:applications-messages (str (:application message)) :data] #(cons message %))))
