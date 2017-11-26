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
   [ui.db.account]
   [ui.db.constants]
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
     {:dispatch-n (concat
                   [[:load-constants]]
                   [(if (nil? token)
                      [:route-map/init routes/routes]
                      [:load-account-info token {:succeed-fx
                                                 {:dispatch [:route-map/init routes/routes]}}])])
      :db
      (merge
       {:base-url base-url
        :constants {:status :loading}
        :shifts {:status :not-asked}}
       (if (nil? token) {} {:account {:token token}}))})))

(defn- mount-root []
  (reagent/render
   [layout/layout [current-page]]
   (.getElementById js/document "app")))

(defn init! []
  (rf/dispatch [::initialize])
  (mount-root))
