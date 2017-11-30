(ns ui.login.core
  (:require
   [reagent.core :as reagent]
   [ui.db.misc :refer [get-url >event >atom <sub]]
   [ui.pages :as pages]
   [ui.routes :refer [href]]
   [ui.widgets :refer [FormWrapper pp]]
   [re-frame.core :as rf]
   [clojure.string :as str]
   [soda-ash.core :as sa]
   [ui.widgets.error-message :refer [ErrorMessage]]))

(defn Form []
  (let [login-form (<sub [:login-form-cursor])
        email (reagent/cursor login-form [:email])
        password (reagent/cursor login-form [:password])]
    (fn []
      (let [{status :status errors :errors} (<sub [:login-form-response])]
        [sa/Grid {:divided true}
         [sa/GridRow {}
          [sa/GridColumn {:width 11}
           [sa/Form {:error (= status :failure)}
            [sa/FormField {:error (contains? errors :email)}
             [:label "Email"]
             [sa/Input {:type "email" :value @email :on-change (>atom email)}]]
            [sa/FormField {:error (contains? errors :password)}
             [:label "Password"]
             [sa/Input {:type "password" :value @password :on-change (>atom password)}]]
            [ErrorMessage {:errors errors
                           :visible (= status :failure)
                           :field-names-map {:non-field-errors ""}}]
            [sa/FormGroup {:class-name "justify-content _space-between"}
             [sa/FormButton {:color :blue
                             :loading (= status :loading)
                             :on-click (>event [:submit-login-form])} "Log in"]
             [sa/FormField {:class-name "login__forgot-password"}
              [:a {:href (href :forgot-password)} "Forgot password?"]]]]]
          [sa/GridColumn {:width 5 :class-name "login__right-column align-items _center"}
           [:div "Don't have account yet?"] [:a {:href (href :sign-up)} "Sign up"]]]]))))

(defn Index [params]
  (fn [params]
    [FormWrapper
     [sa/Header {:as :h1 :class-name "moonlight-form-header"} "Welcome back to Dr. Moonlight!"]
     [Form]]))

(pages/reg-page :core/login Index)
