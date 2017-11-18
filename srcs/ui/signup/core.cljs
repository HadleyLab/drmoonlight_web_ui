(ns ui.signup.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require
   [reagent.core :as reagent]
   [ui.db :refer [get-url]]
   [ui.pages :as pages]
   [ui.routes :refer [href]]
   [ui.widgets :refer [form-radio build-form form-wrapper]]
   [re-frame.core :as rf]
   [clojure.string :as str]
   [sodium.core :as na]))

(def sceduler-form-fields
  {"Personal Information"
   {:firstName "First Name"
    :lastName "Last Name"
    :facilityName "Hospital / Facility name"
    :departmentName "Department name"}
   "Account settings"
   {:email "Email"
    :password "Password"}})

(def resident-form-fields
  {"Personal Information"
   {:firstName "First Name"
    :lastName "Last Name"}
   "Account settings"
   {:email "Email"
    :password "Password"}})

(def root-path :sign-up-page)

(def schema
  {:sign-up-form
   {:fields (into {} (map (fn [[key _]] [key ""]) (mapcat second sceduler-form-fields)))
    :response {:status :not-asked}}
   :mode :resident
   :activation-response {:status :not-asked}})

(defn form-content [mode sign-up-form]
  (if (= mode :resident)
    [build-form sign-up-form resident-form-fields]
    [build-form sign-up-form sceduler-form-fields]))

(defn form []
  (let [sign-up-page-cursor @(rf/subscribe [:cursor [root-path]])
        sign-up-form-cursor (reagent/cursor sign-up-page-cursor [:sign-up-form])
        mode-cursor (reagent/cursor sign-up-page-cursor [:mode])
        modes {:resident "Resident" :scheduler "Scheduler"}]
    (fn []
      (let [status (get-in @sign-up-form-cursor [:response :status])]
        [na/form {:class-name "moonlight-inner-form" :error? (= status :failure)}
         [form-radio {:items modes :cursor mode-cursor :label "Register as"}]
         [form-content @mode-cursor sign-up-form-cursor]
         [:div.moonlight-form-group
          [na/form-button {:content "Sign up"
                           :color :blue
                           :loading? (= status :loading)
                           :on-click (na/>event [:do-sigh-up])}]]]))))

(defn index [params]
  (rf/dispatch-sync [::init-sign-up-page])
  (fn [params]
    [form-wrapper
     [na/header {:as :h1 :class-name "moonlight-form-header"} "Welcome to Dr. Moonlight!"]
     [form]
     [:p "Already a member? " [:a {:href (href :login)} "Log In"]]]))

(rf/reg-event-db
 ::init-sign-up-page
 (fn [db [_]]
   (if (= (root-path db) nil)
     (assoc-in db [root-path] schema)
     db)))

(rf/reg-event-fx
 :do-sigh-up
 (fn [{db :db} [_]]
   (let [mode (get-in db [root-path :mode])
         data (get-in db [root-path :sign-up-form :fields])]
     {:json/fetch {:uri (get-url db (str "/api/accounts/" (name mode) "/"))
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

(rf/reg-event-fx
:do-sigh-up-succeed
 (fn [{db :db} [_]]
   {:dispatch [:goto :sign-up :thanks]
    :db (assoc-in db [root-path] schema)}))

(defn thanks [params]
  [form-wrapper
   [na/header {:as :h1 :class-name "moonlight-form-header"} "Thanks for registering !"]
   [:p "You’ve just been sent an email to confirm your email address. Please click on the link in this email to confirm your registration to Dr. Moonlight."]
   [:p "if you don’t get the email in 10 minutes, you can " [:a {} "resend the confirmation email"] "."]
   [na/button {:basic? true :color :blue :content "Go Home" :on-click (na/>event [:goto "/"])}]])

(defn activation-loading []
  [form-wrapper
   [na/header {:as :h1 :class-name "moonlight-form-header"} "Waiting for the activation!"]
   [na/button {:basic? true :color :blue :content "Go Home" :on-click (na/>event [:goto "/"])}]])

(defn activation-succeed []
  [form-wrapper
   [na/header {:as :h1 :class-name "moonlight-form-header"} "You account is successfully activated"]
   [na/button {:basic? true :color :blue :content "Sign Up" :on-click (na/>event [:goto :login])}]])

(defn activation-failure [errors]
  [form-wrapper
   [na/header {:as :h1 :class-name "moonlight-form-header"} "You account is failed to activate"]
   [:p.error (:detail errors)]
   [na/button {:basic? true :color :blue :content "Go Home" :on-click (na/>event [:goto "/"])}]])

(defn activate [params]
  (rf/dispatch-sync [::init-sign-up-page])
  (rf/dispatch [:activate params])
  (let [activation-response @(rf/subscribe [:cursor [root-path :activation-response]])]
    (fn [params]
      (cond
        (= (:status @activation-response) :loading) (activation-loading)
        (= (:status @activation-response) :succeed) (activation-succeed)
        (= (:status @activation-response) :failure) (activation-failure (:errors @activation-response))
        :else [:div (str @activation-response)]))))

(rf/reg-event-fx
 :activate
 (fn [{db :db} [_ data]]
   {:json/fetch {:uri (get-url db "/api/accounts/activate/")
                 :method "post"
                 :body data
                 :success {:event :activate-succeed}
                 :error {:event :activate-failure}}
    :db (assoc-in db [root-path :activation-response :status] :loading)}))

(rf/reg-event-db
 :activate-succeed
 (fn [db [_]]
   (assoc-in db [root-path :activation-response :status] :succeed)))

(rf/reg-event-db
 :activate-failure
 (fn [db [_ {data :data}]]
   (-> db
       (assoc-in [root-path :activation-response :status] :failure)
       (assoc-in [root-path :activation-response :errors] data))))

(pages/reg-page :core/sign-up index)
(pages/reg-page :core/sign-up-thanks thanks)
(pages/reg-page :core/activate activate)
