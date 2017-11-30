(ns ui.signup.core
  (:require
   [reagent.core :as reagent]
   [ui.db.misc :refer [get-url >event >atom <sub]]
   [ui.db.account :refer [scheduler-form-fields resident-form-fields]]
   [ui.pages :as pages]
   [ui.routes :refer [href]]
   [ui.widgets :refer [FormRadio BuildForm FormWrapper]]
   [ui.widgets.error-message :refer [ErrorMessage]]
   [re-frame.core :as rf]
   [clojure.string :as str]
   [soda-ash.core :as sa]))

(defn FormContent [mode form-cursor]
  (if (= mode :resident)
    [BuildForm form-cursor resident-form-fields]
    [BuildForm form-cursor scheduler-form-fields]))

(defn Form []
  (rf/dispatch-sync [:init-sign-up-form])
  (let [sign-up-form-cursor (<sub [:sign-up-form-cursor])
        mode-cursor (reagent/cursor sign-up-form-cursor [:mode])
        modes {:resident "Resident" :scheduler "Scheduler"}]
    (fn []
      (let [status (<sub [:sign-up-form-status])]
        [sa/Form {:error (= status :failure) :class-name "position _relative"}
         [sa/FormGroup
          [FormRadio {:items modes :cursor mode-cursor :label "Register as"}]
          [sa/FormField {:width 6 :class-name "gray-font"}
           "Scheduler is a user, who is able to create shifts for booking"]]
         [FormContent @mode-cursor sign-up-form-cursor]
         [:div
          [sa/FormButton {:color :blue
                          :loading (= status :loading)
                          :on-click (>event [:do-sigh-up [:goto :sign-up :thanks]])} "Sign up"]]]))))

(defn Index [params]
  (fn [params]
    [FormWrapper
     [sa/Header {:as :h1 :class-name "moonlight-form-header"} "Welcome to Dr. Moonlight!"]
     [Form]
     [:div {:class-name "signup__back"}
      [:p "Already a member? " [:a {:href (href :login)} "Log In"]]]]))

(defn BackButton [url text]
  [sa/Button {:basic true
              :color :blue
              :on-click (>event [:goto url])
              :class-name "signup__back"} text])

(defn Thanks [params]
  [FormWrapper
   [:div {:class-name "signup-thanks__wrapper"}
    [sa/Icon {:name "check circle" :color "green" :size "huge"}]
    [sa/Header {:as :h1 :class-name "moonlight-form-header"} "Thanks for registering!"]
    [:p "You’ve just been sent an email to confirm your email address. Please click on the link in this email to confirm your registration to Dr. Moonlight."]
    [:p "if you don’t get the email in 10 minutes, you can " [:a {} "resend the confirmation email"] "."]
    [BackButton "/" "Back to Home"]]])

(defn ActivationLoading []
  [FormWrapper
   [:div {:class-name "signup-thanks__wrapper"}
    [sa/Icon {:name "wait" :color "blue" :size "huge"}]
    [sa/Header {:as :h1 :class-name "moonlight-form-header"} "Waiting for the activation!"]
    [BackButton "/" "Back to Home"]]])

(defn ActivationSucceed []
  [FormWrapper
   [:div {:class-name "signup-thanks__wrapper"}
    [sa/Icon {:name "check circle" :color "green" :size "huge"}]
    [sa/Header {:as :h1 :class-name "moonlight-form-header"} "You account is successfully activated"]
    [BackButton :login "Log in"]]])

(defn ActivationFailure [errors]
  [FormWrapper
   [:div {:class-name "signup-thanks__wrapper"}
    [sa/Icon {:name "remove circle" :color "red" :size "huge"}]
    [sa/Header {:as :h1 :class-name "moonlight-form-header"} "You account is failed to activate"]
    [ErrorMessage {:errors errors}]
    [BackButton "/" "Back to Home"]]])

(defn Activate [params]
  (rf/dispatch [:activate params])
  (fn [params]
    (let [activation-response (<sub [:activation-response])]
      (cond
        (= (:status activation-response) :loading) [ActivationLoading]
        (= (:status activation-response) :succeed) [ActivationSucceed]
        (= (:status activation-response) :failure) [ActivationFailure (:errors activation-response)]
        :else [:div (str activation-response)]))))

(defn Confirm [params]
  (rf/dispatch [:init-password-reset-form])
  (let [password-cursor (<sub [:password-reset-form-password-cursor])]
    (fn [params]
      (let [{status :status errors :errors} (<sub [:password-reset-form-response])]
        [FormWrapper
         [sa/Header {:as :h1 :class-name "moonlight-form-header"} "Setup you new password"]
         [sa/Form {:error (= status :failure)}
          [sa/FormField
           [:label "Password"]
           [sa/Input
            {:type "password"
             :error (= status :failure)
             :value @password-cursor
             :on-change (>atom password-cursor)}]]
          (when (= status :failure)
            [ErrorMessage {:errors errors
                           :field-names-map {:non-field-errors ""}}])
          [sa/FormGroup {:class-name "flex-direction _column"}
           [sa/FormButton {:color :blue
                           :loading (= status :loading)
                           :on-click (>event [:confirm-rest-password params])}
           "Setup new password"]]]
          [sa/FormField {:class-name "forgot-password__back"}
           [:a {:href (href :login)} "Back to login"]]]))))

(pages/reg-page :core/sign-up Index)
(pages/reg-page :core/sign-up-thanks Thanks)
(pages/reg-page :core/activate Activate)
(pages/reg-page :core/confirm Confirm)
