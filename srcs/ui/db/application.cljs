(ns ui.db.application
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require
   [reagent.core :as reagent]
   [ui.db.misc :refer [get-url]]
   [ui.db.shift :refer [parse-date-time]]
   [re-frame.core :as rf]))

(rf/reg-event-fx
 :get-applications
 (fn [{db :db} _]
   {:json/fetch->path {:path [:applications]
                       :uri (get-url db "/api/shifts/application/")
                       :token @(rf/subscribe [:token])
                       :map-result (fn [data] (map #(update-in % [:shift] parse-date-time) data))}}))

(rf/reg-sub
 :applications
 (fn [db _]
   (get-in db [:applications] {:status :not-asked})))

(rf/reg-event-db
 :set-applications-filter-state
 (fn [db [_ state]]
   (let [current-state @(rf/subscribe [:applications-filter-state])]
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
                       :token @(rf/subscribe [:token])}}))

(rf/reg-event-db
 :get-application-info-succeed
 (fn [db [_ {data :data application-pk :application-pk}]]
   (-> db
       (assoc-in [:applications-info application-pk :status] :succeed)
       (assoc-in [:applications-info application-pk :data] data))))

(rf/reg-event-db
 :get-application-info-failure
 (fn [db [_ {data :data application-pk :application-pk}]]
   (-> db
       (assoc-in [:applications-info application-pk :status] :failure)
       (assoc-in [:applications-info application-pk :errors] data))))

(rf/reg-sub
 :application-info
 (fn [db [_ application-pk]]
   (get-in db [:applications-info application-pk] {:status :not-asked})))

(rf/reg-sub
 :application-participant
 (fn [db [_ application-pk participant-id]]
   (let [scheduler (get-in db [:applications-info application-pk :data :shift :owner])
         resident (get-in db [:applications-info application-pk :data :owner])
         {first-name :first-name
          last-name :last-name} (if (= (:pk scheduler) participant-id) scheduler resident)]
     (str first-name " " last-name))))

(rf/reg-event-fx
 :get-application-messages
 (fn [{db :db} [_ application-pk]]
   {:json/fetch->path {:path [:applications-messages application-pk]
                       :uri (get-url db "/api/shifts/application/" application-pk "/message/")
                       :token @(rf/subscribe [:token])}}))

(rf/reg-sub
 :application-messages
 (fn [db [_ application-pk]]
   (get-in db [:applications-messages application-pk] {:status :not-asked})))

;; Comments

(rf/reg-event-db
 :init-comment-form
 (fn [db _]
   (assoc db :comment-form {:fields {:message ""} :response {:status :not-asked}})))

(rf/reg-sub-raw
 :comment-cursor
 (fn [db _]
   (reaction (reagent/cursor db [:comment-form :fields :message]))))

(rf/reg-sub
 :comment-form
 (fn [db _]
   (get-in db [:comment-form :response])))

(rf/reg-event-fx
 :add-comment
 (fn [{db :db} [_ application-pk]]
   {:json/fetch->path {:path [:comment-form :response]
                       :uri (get-url db "/api/shifts/application/" application-pk "/message/")
                       :method "POST"
                       :token @(rf/subscribe [:token])
                       :body {:message @@(rf/subscribe [:comment-cursor])}
                       :succeed-fx (fn [data] [:add-new-message-to-list application-pk data])}}))

(rf/reg-event-db
 :add-new-message-to-list
 (fn [db [_ application-pk data]]
   (update-in db [:applications-messages (str application-pk) :data] #(cons data %))))
