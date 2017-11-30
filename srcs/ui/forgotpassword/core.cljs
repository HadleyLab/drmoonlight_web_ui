(ns ui.forgotpassword.core
  (:require
   [reagent.core :as reagent]
   [ui.db.misc :refer [get-url >event >atom <sub]]
   [ui.pages :as pages]
   [ui.routes :refer [href]]
   [ui.widgets :refer [FormWrapper]]
   [re-frame.core :as rf]
   [clojure.string :as str]
   [soda-ash.core :as sa]))

(defn Form []
  (rf/dispatch [:init-forgot-password-form])
  (let [email (<sub [:forgot-password-form-email-cursor])]
    (fn []
      (let [{status :status {email-errors :email} :errors} (<sub [:forgot-password-form-response])]
        [sa/Form {:error (= status :failure)}
         [sa/FormField
          [:label "Email"]
          [sa/Input
           {:type "email"
            :error (not= email-errors nil)
            :value @email
            :on-change (>atom email)}]]
         (when email-errors
           [:div {:class "error"} (clojure.string/join " " email-errors)])
         [sa/FormGroup {:class-name "forgot-password__buttons flex-direction _column"}
          [sa/FormButton {:color :blue
                          :loading (= status :loading)
                          :on-click (>event [:rest-password [:goto :forgot-password :thanks]])}
           "Request reset link"]
          [sa/FormField {:class-name "forgot-password__back"}
           [:a {:href (href :login)} "Back to login"]]]]))))

(defn Index [params]
  (fn [params]
    [FormWrapper
     [sa/Header {:as :h1 :class-name "moonlight-form-header"} "Forgot your password?"]
     [:div {:class-name "forgot-password__subheader"}
      "Enter your email address below and we'll get you back on track."]
     [Form]]))

(defn Thanks [params]
  [FormWrapper
   [sa/Header {:as :h1 :class-name "moonlight-form-header"} "You have reseted your password!"]
   [:p "You’ve just been sent an email with reset link. Please click on the link in this email to change your password"]
   [sa/Button {:basic true :color :blue :content "Go Home" :on-click (>event [:goto "/"])}]])

(pages/reg-page :core/forgot-password Index)
(pages/reg-page :core/forgot-password-thanks Thanks)
