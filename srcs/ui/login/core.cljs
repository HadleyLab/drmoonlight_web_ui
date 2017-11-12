(ns ui.login.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require
   [reagent.core :as reagent]
   [ui.pages :as pages]
   [ui.routes :refer [href]]
   [re-frame.core :as rf]
   [clojure.string :as str]
   [sodium.core :as na]))


(defn form []
  (let [login-form @(rf/subscribe [:cursor [:login-form]])
        email (reagent/cursor login-form [:email])
        password (reagent/cursor login-form [:password])]
    (fn []
      [na/form {}
       [na/form-input {:label "Email" :type "email" :on-change (na/>atom email)}]
       [na/form-input {:label "Password" :type "password" :on-change (na/>atom password)}]
       [na/form-group {}
        [na/form-button {:content "Login"
                         :color :blue
                         :on-click (na/>event [:login @email @password])}]
        [:a {:href (href :forgot-password)} "Forgot password?"]]])))

(defn index [params]
  [na/grid {}
   [na/grid-row {}]
   [na/grid-row {}
    [na/grid-column {:width 4} [:div]]
    [na/grid-column {:width 8}
     [na/container {:text? true}
      [na/header {} "Welcome back to Dr. Moonlight!"]
      [na/grid {}
       [na/grid-row {}
        [na/grid-column {:width 12} [(form)]]
        [na/grid-column {:width 4} [:div [:p "Don't have account yet?"] [:a {:href (href :sign-up)} "Sign Up"]]]]]]
     [na/grid-column {:width 4} [:div]]]
    [na/grid-row {}]]])

(pages/reg-page :core/login index)
