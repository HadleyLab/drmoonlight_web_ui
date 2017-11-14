(ns ui.signup.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require
   [reagent.core :as reagent]
   [ui.pages :as pages]
   [ui.routes :refer [href]]
   [ui.widgets :refer [form-radio build-form]]
   [re-frame.core :as rf]
   [clojure.string :as str]
   [sodium.core :as na]))

(def sceduler-form-fields
  {"Personal Information"
   {:first-name "First Name"
    :last-name "Last Name"
    :facility-name "Hospital / Facility name"
    :department-name "Department name"}
   "Account settings"
   {:email "Email"
    :password "Password"}})

(def resident-form-fields
  {"Personal Information"
   {:first-name "First Name"
    :last-name "Last Name"}
   "Account settings"
   {:email "Email"
    :password "Password"}})

(def root-path :sign-up-page)

(def schema
  {:sign-up-form
   {:fields (into {} (map (fn [[key _]] [key ""]) (mapcat second sceduler-form-fields)))
    :response {:status :not-asked}}
   :mode :resident})

(defn form-content [{mode :mode}]
    (if (= mode :resident)
      [build-form {:path [root-path :sign-up-form] :field-sets resident-form-fields}]
      [build-form {:path [root-path :sign-up-form] :field-sets sceduler-form-fields}]))

(defn form []
  (let [sign-up-page @(rf/subscribe [:cursor [root-path]])
        sign-up-form (reagent/cursor sign-up-page [:sign-up-form])
        mode (reagent/cursor sign-up-page [:mode])
        modes {:resident "Resident" :scheduler "Scheduler"}]
    (fn []
      (let [status (get-in @sign-up-form [:response :status])]
        [na/form {:class-name "moonlight-inner-form" :error? (= status :failure)}
         [form-radio {:items modes :cursor mode :label "Register as"}]
         (when @sign-up-form [form-content {:mode @mode}])
         [:div.moonlight-form-group
          [na/form-button {:content "Sign up"
                           :color :blue
                           :loading? (= status :loading)
                           :on-click (na/>event [:do-sigh-up])}]]]))))

(defn index [params]
  (rf/dispatch [:init-sign-up-page])
  (fn [params]
    [:div.moonlight-form-wrapper
     [na/container {:text? true :class-name "moonlight-form"}
      [na/header {:as :h1 :class-name "moonlight-form-header"} "Welcome to Dr. Moonlight!"]
      [(form)]]]))

(rf/reg-event-db
 :init-sign-up-page
 (fn [db [_]]
   (if (= (root-path db) nil)
     (assoc-in db [root-path] schema)
     db)))

(rf/reg-event-fx
 :do-sigh-up
 (fn [{db :db} [_]]
   (let [mode (get-in db [root-path :mode])
         data (get-in db [root-path :sign-up-form :fields])]
     {:json/fetch {:uri "http://localhost:8000/api/accounts/resident/"
                   :method "post"
                   :body data
                   :success {:event :do-sigh-up-succeed}
                   :error {:event :do-sigh-up-failure}}
      :db (assoc-in db [root-path :sign-up-form :response :status] :loading)})))

(rf/reg-event-db
 :do-sigh-up-failure
 (fn [db [_ {data :data}]]
   (-> db
       (assoc-in [root-path :sign-up-form :response :status] :failure)
       (assoc-in [root-path :sign-up-form :response :errors] data))))

(pages/reg-page :core/sigh-up index)
