(ns ui.routes
  (:require [clojure.string :as str]
            [ui.db.account :refer [is-scheduler is-resident
                                   is-approved is-account-manager]]
            [route-map.core :as route-map]))

;; application routes represented as hash-map (see https://github.com/niquola/route-map)
;; each leaf is key which mapped to page component thro pages hash-map

;; You could define custom re-frame event under :context  key,
;; while matching routes all :context events will be fired with parameter :init
;; We also save previos contexts and fire event with :deinit flag for disposed contexts
;; You could use context to init common state for some branch of routes

(def routes {:. :core/index
             "login" :core/login
             "activate" {[:uid] {[:token] :core/activate}}
             "confirm" {[:uid] {[:token] :core/confirm}}
             "sign-up" {:. :core/sign-up
                        "thanks" {:. :core/sign-up-thanks}}
             "forgot-password" {:. :core/forgot-password
                                "thanks" :core/forgot-password-thanks}
             "account-manager" {:interceptors [is-account-manager]
                                :. :account-manager/index
                                "detail" {[:resident-pk] :account-manager/resident-profile-detail}}
             "resident" {:interceptors [is-resident]
                         :. :core/resident
                         "schedule" {:. :core/resident-schedule}
                         "messages" {:. :core/resident-messages
                                     [:shift-pk] {:. :core/resident-messages-apply
                                                  "discuss" {[:application-pk] :core/resident-messages-discuss}}
                                     :interceptors [is-approved]}
                         "statistics" {:. :core/resident-statistics}
                         "profile" {:. :core/resident-profile
                                    "notification" :core/resident-profile-notification
                                    "change-password" :core/resident-profile-change-password}}
             "scheduler" {:interceptors [is-scheduler]
                          :. :core/scheduler
                          "detail" {[:resident-pk] :scheduler/resident-profile-detail}
                          "schedule" {:. :core/scheduler-schedule}
                          "messages" {:. :core/scheduler-messages
                                      [:shift-pk] {"discuss" {[:application-pk] :core/scheduler-messages-discuss}}}
                          "statistics" {:. :core/scheduler-statistics}
                          "profile" {:. :core/scheduler-profile
                                     "change-password" :core/scheduler-profile-change-password}}})

(defn href
  ;; helper function to build urls also check url
  ;; is valid for current routing
  ;; (href :patients 5) => #/patients/5
  [& parts]
  (let [url (str "/" (str/join "/" (map (fn [x] (if (keyword? x) (name x) (str x))) parts)))]
    (when-not  (route-map/match [:. url] routes)
      (.error js/console (str url " is not matches routes")))
    (str "#" url)))
