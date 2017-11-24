(ns ui.routes
  (:require [clojure.string :as str]
            [ui.db :refer [<sub]]
            [route-map.core :as route-map]))

;; application routes represented as hash-map (see https://github.com/niquola/route-map)
;; each leaf is key which mapped to page component thro pages hash-map

;; You could define custom re-frame event under :context  key,
;; while matching routes all :context events will be fired with parameter :init
;; We also save previos contexts and fire event with :deinit flag for disposed contexts
;; You could use context to init common state for some branch of routes

(defn is-resident []
  (= (<sub [:user-type]) :resident))

(defn is-scheduler []
  (= (<sub [:user-type]) :scheduler))

(defn is-approved []
  (= (<sub [:user-state])  3))

(def routes {:. :core/index
             "login" :core/login
             "activate" {[:uid] {[:token] :core/activate}}
             "sign-up" {:. :core/sign-up
                        "thanks" {:. :core/sign-up-thanks}}
             "forgot-password" {:. :core/forgot-password
                                "thanks" {:. :core/forgot-password-thanks}}
             "resident" {:interceptors [is-resident]
                         :. :core/resident
                         "schedule" {:. :core/resident-schedule
                                     :interceptors [is-approved]}
                         "messages" {:. :core/resident-messages
                                     [:pk] {"apply" {:. :core/resident-messages}}
                                     :interceptors [is-approved]}
                         "statistics" {:. :core/resident-statistics
                                       :interceptors [is-approved]}
                         "profile" {:. :core/resident-profile
                                    "notification" :core/resident-profile-notification}}
             "scheduler" {:interceptors [is-scheduler]
                          :. :core/scheduler
                          "schedule" {:. :core/scheduler-schedule}
                          "messages" {:. :core/scheduler-messages
                                      [:pk] {"apply" {:. :core/scheduler-messages}}}
                          "statistics" {:. :core/scheduler-statistics}
                          "profile" {:. :core/scheduler-profile}}})

(defn href
  ;; helper function to build urls also check url
  ;; is valid for current routing
  ;; (href :patients 5) => #/patients/5
  [& parts]
  (let [url (str "/" (str/join "/" (map (fn [x] (if (keyword? x) (name x) (str x))) parts)))]
    (when-not  (route-map/match [:. url] routes)
      (.error js/console (str url " is not matches routes")))
    (str "#" url)))
