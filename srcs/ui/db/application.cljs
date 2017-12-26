(ns ui.db.application
  (:require
   [reagent.core :as reagent]
   [ui.db.misc :refer [get-url <sub]]
   [ui.db.shift :refer [parse-date-time]]
   [re-frame.core :as rf]))

(def schema
  {::application
   {:list {:status :not-asked}
    :detail {}
    :messages {}
    :filter-value nil
    :comment-form {:fields {:text ""} :response {:status :not-asked}}}})

(def application-statuses
  {1 "New application"
   2 "Approved"
   3 "Rejected"
   4 "Postponed"
   5 "Confirmed"
   6 "Cancelled"
   7 "Failed"
   8 "Completed"})

(defn get-application-status-name [state]
  (if (nil? state)
    "All messages"
    (map
     (fn [[key value]] (if (= key state) value))
     application-statuses)))

(defn format-application-shift-date [start finish]
  (let [start-date (.format start "DD/MM")
        finish-date (.format finish "DD/MM")
        start-time (.format start "h:mm a")
        finish-time (.format finish "h:mm a")]
    (if (= start-date finish-date)
      (str " at " start-date " from " start-time " to " finish-time)
      (str " from " start-date " at " start-time " to " finish-date " at " finish-time))))

(rf/reg-event-fx
 :get-applications
 (fn [{db :db} _]
   {:json/fetch->path {:path [::application :list]
                       :uri (get-url db "/api/shifts/application/")
                       :token (<sub [:token])
                       :map-result (fn [data] (map #(update-in % [:shift] parse-date-time) data))}}))
(rf/reg-sub
 ::application
 (fn [db path]
   (get-in db path)))

(rf/reg-sub
 :applications
 #(<sub [::application :list]))

(<sub [:applications])

(rf/reg-sub
 :applications-filter-state
 #(<sub [::application :filter-value]))

(rf/reg-event-db
 :set-applications-filter-state
 (fn [db [_ state]]
   (let [current-state (<sub [:applications-filter-state])]
     (assoc-in db [::application :filter-value] (if (= current-state state) nil state)))))

(rf/reg-sub
 :count-applications-by-state
 (fn [db [_ state]]
   (count
    (filter
     #(= state (:state %))
     (<sub [::application :list :data])))))

(rf/reg-event-fx
 :get-application-info
 (fn [{db :db} [_ application-pk]]
   {:json/fetch->path {:path [::application :detail application-pk]
                       :uri (get-url db "/api/shifts/application/" application-pk "/")
                       :token (<sub [:token])
                       :map-result #(update-in % [:shift] parse-date-time)}}))

(rf/reg-sub
 :application-info
 (fn [db [_ application-pk]]
   (or (<sub [::application :detail application-pk]) {:status :not-asked})))

(rf/reg-sub
 :available-transitions
 (fn [db [_ application-pk]]
   (or (<sub [::application :detail application-pk :data :available-transitions])
       [])))

(rf/reg-sub
 :application-participant
 (fn [db [_ application-pk participant-id]]
   (let [scheduler (<sub [::application :detail application-pk :data :shift :owner])
         resident (<sub [::application :detail application-pk :data :owner])
         {first-name :first-name
          last-name :last-name} (if (= (:id scheduler) participant-id) scheduler resident)]
     (str first-name " " last-name))))

(rf/reg-event-fx
 :get-application-messages
 (fn [{db :db} [_ application-pk]]
   {:json/fetch->path {:path [::application :messages application-pk]
                       :uri (get-url db "/api/shifts/application/" application-pk "/message/")
                       :token (<sub [:token])}}))

(rf/reg-sub
 :application-messages
 (fn [db [_ application-pk]]
   (or (<sub [::application :messages application-pk]) {:status :not-asked})))

;; Comments
(rf/reg-event-db
 :init-comment-form
 (fn [db _]
   (assoc-in db [::application :comment-form] (get-in schema [::application :comment-form]))))

(rf/reg-sub
 :comment-cursor
 #(<sub [:cursor [::application :comment-form :fields :text]]))

(rf/reg-sub
 :comment-form
 #(<sub [::application :comment-form :response]))

(rf/reg-event-fx
 :add-comment
 (fn [{db :db} [_ application-pk transition]]
   {:json/fetch->path {:path [::application :comment-form :response]
                       :uri (get-url db "/api/shifts/application/" application-pk "/" transition "/")
                       :method "POST"
                       :token (<sub [:token])
                       :body {:text @(<sub [:comment-cursor])}
                       :succeed-fx [:reset-comment-text]}}))

(rf/reg-event-fx
 :application-state-changed
 (fn [{db :db} [_ {{pk :pk :as application} :application message :message}]]
   (merge
    {:db (assoc-in db [::application :detail (str pk)] {:status :succeed :data application})}
    (when-not (nil? message) {:dispatch [:message-created {:message message}]}))))

(rf/reg-event-db
 :message-created
 (fn [db [_ {{application :application :as message} :message}]]
   (update-in db [::application :messages (str application) :data] #(cons message %))))

(rf/reg-event-db
 :reset-comment-text
 (fn [db [_]]
   (assoc-in db [::application :comment-form :fields :text] "")))
