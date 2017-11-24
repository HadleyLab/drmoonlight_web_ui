(ns ui.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require
   [clojure.string :as str]
   [cljsjs.react]
   [reagent.core :as reagent]
   [re-frame.core :as rf]
   [frames.routing]
   [frames.xhr]
   [frames.debounce]
   [frames.cookies :as cookies]
   [frames.openid :as openid]
   [frames.redirect :as redirect]
   [ui.db]
   [ui.dashboard.core]
   [ui.login.core]
   [ui.signup.core]
   [ui.forgotpassword.core]
   [ui.resident.core]
   [ui.scheduler.core]
   [ui.pages :as pages]
   [ui.routes :as routes]
   [ui.layout :as layout]))

;; this is the root component wich switch pages
;; using current-route key from database
(defn current-page []
  (let [{page :match params :params} @(rf/subscribe [:route-map/current-route])]
    (if page
      (if-let [cmp (get @pages/pages page)]
        [:div [cmp params]]
        [:div.not-found (str "Page not found [" (str page) "]")])
      [:div.not-found (str "Route not found ")])))

;; this is first event, which should initialize
;; application
;; handler use coefects cookies & openid to check for
;; user in cookies or in location string (after OpenId redirect)
(rf/reg-event-fx
 ::initialize
 [(rf/inject-cofx :store)]
 (fn [{store :store}]
   (let [base-url "http://localhost:8000"
         token (:token store)]
     {:dispatch-n (concat [[:route-map/init routes/routes]] [(when-not (nil? token) [:load-account-info token])])
      :db (merge
           {:base-url base-url
            :constants {:status :loading}}
           (when-not (nil? token) {:account {:token token}}))
      :json/fetch {:uri (str base-url "/api/constants/")
                   :success {:event :constants-succeed}
                   :error {:event :constants-failure}}})))

(defn list->map [data]
  (into {} (map (fn [{pk :pk :as item}] [pk item]) data)))

(rf/reg-event-db
 :constants-succeed
 (fn [db [_ {data :data}]]
   (-> db
       (assoc-in [:constants :status] :succeed)
       (assoc-in [:constants :data] (into {} (map (fn [[key values]] [key (list->map values)]) data))))))

(rf/reg-event-db
 :constants-failure
 (fn [db [_ {data :data}]]
   (-> db
       (assoc-in [:constants :status] :failure)
       (assoc-in [:constants :error] :data))))

(defn- mount-root []
  (reagent/render
   [layout/layout [current-page]]
   (.getElementById js/document "app")))

(defn init! []
  (rf/dispatch [::initialize])
  (mount-root))
