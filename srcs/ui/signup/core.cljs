(ns ui.signup.core
  (:require
   [reagent.core :as reagent]
   [ui.db :refer [get-url >event >atom]]
   [ui.pages :as pages]
   [ui.routes :refer [href]]
   [ui.widgets :refer [FormRadio BuildForm FormWrapper fields->schema]]
   [re-frame.core :as rf]
   [clojure.string :as str]
   [soda-ash.core :as sa]))

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
   {:fields (fields->schema sceduler-form-fields)
    :response {:status :not-asked}}
   :mode :resident
   :activation-response {:status :not-asked}
   :password-reset {:response {:status :not-asked}
                    :password ""}})

(defn FormContent [mode form-cursor]
  (if (= mode :resident)
    [BuildForm form-cursor resident-form-fields]
    [BuildForm form-cursor sceduler-form-fields]))

(defn Form []
  (let [sign-up-page-cursor @(rf/subscribe [:cursor [root-path]])
        sign-up-form-cursor (reagent/cursor sign-up-page-cursor [:sign-up-form])
        mode-cursor (reagent/cursor sign-up-page-cursor [:mode])
        modes {:resident "Resident" :scheduler "Scheduler"}]
    (fn []
      (let [status (get-in @sign-up-form-cursor [:response :status])]
        [sa/Form {:error (= status :failure) :class-name "position _relative"}
         [sa/FormGroup
          [sa/FormField {:width 10}
           [FormRadio {:items modes :cursor mode-cursor :label "Register as"}]]
          [sa/FormField {:width 6 :class-name "gray-font"}
           "Scheduler is a user, who is able to create shifts for booking"]]
         [FormContent @mode-cursor sign-up-form-cursor]
         [:div
          [sa/FormButton {:color :blue
                          :loading (= status :loading)
                          :on-click (>event [:do-sigh-up])} "Sign up"]]]))))

(defn Index [params]
  (rf/dispatch-sync [::init-sign-up-page])
  (fn [params]
    [FormWrapper
     [sa/Header {:as :h1 :class-name "moonlight-form-header"} "Welcome to Dr. Moonlight!"]
     [Form]
     [:div {:class-name "signup__to-login"}
      [:p "Already a member? " [:a {:href (href :login)} "Log In"]]]]))

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

(defn Thanks [params]
  [FormWrapper
   [sa/Header {:as :h1 :class-name "moonlight-form-header"} "Thanks for registering !"]
   [:p "You’ve just been sent an email to confirm your email address. Please click on the link in this email to confirm your registration to Dr. Moonlight."]
   [:p "if you don’t get the email in 10 minutes, you can " [:a {} "resend the confirmation email"] "."]
   [sa/Button {:basic true :color :blue :on-click (>event [:goto "/"])} "Go Home"]])

(defn ActivationLoading []
  [FormWrapper
   [sa/Header {:as :h1 :class-name "moonlight-form-header"} "Waiting for the activation!"]
   [sa/Button {:basic true :color :blue :on-click (>event [:goto "/"])} "Go Home"]])

(defn ActivationSucceed []
  [FormWrapper
   [sa/Header {:as :h1 :class-name "moonlight-form-header"} "You account is successfully activated"]
   [sa/Button {:basic true :color :blue :on-click (>event [:goto :login])} "Sign Up"]])

(defn ActivationFailure [errors]
  [FormWrapper
   [sa/Header {:as :h1 :class-name "moonlight-form-header"} "You account is failed to activate"]
   [:p.error (:detail errors)]
   [sa/Button {:basic true :color :blue :on-click (>event [:goto "/"])} "Go Home"]])

(defn Activate [params]
  (rf/dispatch-sync [::init-sign-up-page])
  (rf/dispatch [:activate params])
  (let [activation-response @(rf/subscribe [:cursor [root-path :activation-response]])]
    (fn [params]
      (cond
        (= (:status @activation-response) :loading) [ActivationLoading]
        (= (:status @activation-response) :succeed) [ActivationSucceed]
        (= (:status @activation-response) :failure) [ActivationFailure (:errors @activation-response)]
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

(defn Confirm [params]
  (rf/dispatch-sync [::init-sign-up-page])
  (let [password-reset-response-cursor @(rf/subscribe [:cursor [root-path :password-reset :response]])
        password-cursor @(rf/subscribe [:cursor [root-path :password-reset :password]])
        status-cursor (reagent/cursor password-reset-response-cursor [:status])
        errors-cursor (reagent/cursor password-reset-response-cursor [:errors])]
    (fn [params]
      (let [status @status-cursor
            password-errors (or (:new-password @errors-cursor)
                                (:non-field-errors @errors-cursor))]
      [FormWrapper
       [sa/Header {:as :h1 :class-name "moonlight-form-header"} "Setup you new password"]
       [sa/Form {:error (= status :failure)}
        [sa/FormField
         [:label "Password"]
         [sa/Input
          {:type "password"
           :error (not= password-errors nil)
           :value @password-cursor
           :on-change (>atom password-cursor)}]]
        (when password-errors
          [:div {:class "error"} (clojure.string/join " " password-errors)])
        [sa/FormGroup {:class-name "flex-direction _column"}
         [sa/FormButton {:color :blue
                         :loading (= status :loading)
                         :on-click (>event [:confirm-rest-password (merge params {:new-password @password-cursor})])}
          "Setup new password"]]]
       [sa/FormField {:class-name "forgot-password__back"}
        [:a {:href (href :login)} "Back to login"]]]))))

(rf/reg-event-fx
 :confirm-rest-password
 (fn [{db :db} [_ data]]
   {:json/fetch {:uri (get-url db "/api/accounts/password/reset/confirm/")
                 :method "post"
                 :body data
                 :success {:event :confirm-rest-password-succeed}
                 :error {:event :confirm-rest-password-failure}}
    :db (assoc-in db [root-path :password-reset :response :status] :loading)}))

(rf/reg-event-fx
 :confirm-rest-password-succeed
 (fn [{db :db} [_]]
   {:db (assoc-in db [root-path :password-reset] (:password-reset schema))
    :dispatch [:goto :login]}))

(rf/reg-event-db
 :confirm-rest-password-failure
 (fn [db [_ {data :data}]]
   (-> db
       (assoc-in [root-path :password-reset :response :status] :failure)
       (assoc-in [root-path :password-reset :response :errors] data))))



(pages/reg-page :core/sign-up Index)
(pages/reg-page :core/sign-up-thanks Thanks)
(pages/reg-page :core/activate Activate)
(pages/reg-page :core/confirm Confirm)
