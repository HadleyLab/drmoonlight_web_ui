(ns ui.core
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
   [ui.db.misc :refer [<sub >event]]
   [ui.dashboard.core]
   [ui.login.core]
   [ui.signup.core]
   [ui.forgotpassword.core]
   [ui.resident.core]
   [ui.scheduler.core]
   [ui.account-manager.core]
   [ui.pages :as pages]
   [ui.routes :as routes]
   [ui.layout :as layout]
   [cljsjs.moment]
   [cljsjs.moment-timezone]
   [soda-ash.core :as sa]))

(defn- NotFound []
  [sa/Container {:class-name "not-found__container align-items _center"}
   [sa/Icon {:name "frown" :size "massive" :fitted true :class-name "not-found__icon"}]
   [:div.not-found__title (str "404")]
   [:div.not-found__subtitle (str "Page not found")]
   [:div "The Page you are looking for doesn't exist or an other error occured."]
   [:div "Go back or head over to "
    [:span.not-found__link {:on-click (>event [:goto "/"])} "Dr. Moonlight Home"]
    " to choose another direction."]])

(defn- Preloader []
  [sa/Header
   [:div.preloader (str "Loading")]])

;; this is the root component wich switch pages
;; using current-route key from database
(defn CurrentPage []
  (let [{page :match params :params} (<sub [:route-map/current-route])
        routes-initialized (<sub [:route-map/initialized])]
    (if routes-initialized
      (if page
        (if-let [cmp (get @pages/pages page)]
          [:div [cmp params]]
          [:div (str "Page " (str page) " is not registered")])
        (do
          (when (nil? (<sub [:token]))
            ;; TODO: save route and redirect to it after succeed login
            ;; TODO: move logic out from render
            (rf/dispatch [:goto :login]))
          [NotFound]))
      [Preloader])))

;; this is first event, which should initialize
;; application
;; handler use coefects cookies & openid to check for
;; user in cookies or in location string (after OpenId redirect)
(rf/reg-event-fx
 ::initialize
 [(rf/inject-cofx :store)]
 (fn [{store :store} [_ base-url]]
   (let [token (:token store)]
     {:dispatch-n (concat
                   [[:load-constants]]
                   [(if (nil? token)
                      [:route-map/init routes/routes]
                      [:save-token token [:route-map/init routes/routes]])])
      :db (merge ui.db/initial-db {:base-url base-url})})))

(defn- mount-root []
  (reagent/render
    [layout/layout [CurrentPage]]
    (.getElementById js/document "app")))

(defn init! [base-url]
  (rf/dispatch [::initialize base-url])
  (mount-root))
