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
   {:db (assoc-in db [:applications :status] :loading)
    :json/fetch {:uri (get-url db "/api/shifts/application/")
                 :token @(rf/subscribe [:token])
                 :success {:event :get-applications-succeed}
                 :error {:event :get-applications-failure}}}))

(rf/reg-event-db
 :get-applications-succeed
 (fn [db [_ {data :data}]]
   (-> db
       (assoc-in [:applications :status] :succeed)
       (assoc-in [:applications :data] (map #(update-in % [:shift] parse-date-time) data)))))

(rf/reg-event-db
 :get-applications-failure
 (fn [db [_ {data :data}]]
   (-> db
       (assoc-in [:applications :status] :failure)
       (assoc-in [:applications :errors] data))))

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
   {:db (assoc-in db [:applications-info application-pk :status] :loading)
    :json/fetch {:uri (get-url db "/api/shifts/application/" application-pk "/")
                 :token @(rf/subscribe [:token])
                 :success {:event :get-application-info-succeed :application-pk application-pk}
                 :error {:event :get-application-info-failure :application-pk application-pk}}}))

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
 :application-owner
 (fn [db [_ application-pk]]
   (let [{first-name :first-name
          last-name :last-name} (get-in db
                                        [:applications-info application-pk :data :shift :owner])]
     (str first-name " " last-name))))

(rf/reg-event-fx
 :get-application-messages
 (fn [{db :db} [_ application-pk]]
   {:db (assoc-in db [:applications-messages application-pk :status] :loading)
    :json/fetch {:uri (get-url db "/api/shifts/application/" application-pk "/message/")
                 :token @(rf/subscribe [:token])
                 :success {:event :get-application-messages-succeed :application-pk application-pk}
                 :error {:event :get-application-messages-failure :application-pk application-pk}}}))

(rf/reg-event-db
 :get-application-messages-succeed
 (fn [db [_ {data :data application-pk :application-pk}]]
   (-> db
       (assoc-in [:applications-messages application-pk :status] :succeed)
       (assoc-in [:applications-messages application-pk :data] data))))

(rf/reg-event-db
 :get-application-messages-failure
 (fn [db [_ {data :data application-pk :application-pk}]]
   (-> db
       (assoc-in [:applications-messages application-pk :status] :failure)
       (assoc-in [:applications-messages application-pk :errors] data))))

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
   {:db (assoc-in db [:comment-form :response :staus] :loading)
    :json/fetch {:uri (get-url db "/api/shifts/application/" application-pk "/message/")
                 :method "POST"
                 :token @(rf/subscribe [:token])
                 :body {:message @@(rf/subscribe [:comment-cursor])}
                 :success {:event :add-comment-succeed :application-pk application-pk}
                 :error {:event :add-comment-failure :application-pk application-pk}}}))

(rf/reg-event-db
 :add-comment-succeed
 (fn [db [_ {data :data application-pk :application-pk}]]
   (-> db
       (assoc-in [:comment-form :response :status] :succeed)
       (assoc-in [:comment-form :response :data] data)
       (update-in [:applications-messages (str application-pk) :data] #(cons data %)))))

(rf/reg-event-db
 :add-comment-failure
 (fn [db [_ {data :data application-pk :application-pk}]]
   (-> db
       (assoc-in [:comment-form :response :status] :failure)
       (assoc-in [:comment-form :response :errors] data))))
