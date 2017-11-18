(ns ui.login.core
  (:require
   [reagent.core :as reagent]
   [ui.pages :as pages]
   [ui.routes :refer [href]]
   [ui.widgets :refer [form-wrapper]]
   [re-frame.core :as rf]
   [clojure.string :as str]
   [sodium.core :as na]))


(def root-path :login-page)

(def schema
  {:login-form
   {:email ""
    :password ""}
   :response {:status :not-asked}})

(defn form []
  (let [login-form @(rf/subscribe [:cursor [root-path :login-form]])
        email (reagent/cursor login-form [:email])
        password (reagent/cursor login-form [:password])]
    (fn []
      [na/grid {}
       [na/grid-row {}
        [na/grid-column {:width 10}
          [na/form {}
          [na/form-input {:label "Email" :type "email" :value @email :on-change (na/>atom email)}]
          [na/form-input {:label "Password" :type "password" :value @password :on-change (na/>atom password)}]
          [na/form-group {}
            [na/form-button {:content "Login"
                            :color :blue
                            :on-click (na/>event [:login @email @password])}]
           [:a {:href (href :forgot-password)} "Forgot password?"]]]]
        [na/grid-column {:width 6}
          [:p "Don't have account yet?"] [:a {:href (href :sign-up)} "Sign Up"]]]])))

(defn index [params]
  (rf/dispatch-sync [::init-login-page])
  (fn [params]
    [form-wrapper
    [na/header {:as :h1 :class-name "moonlight-form-header"} "Welcome back to Dr. Moonlight!"]
    [(form)]]))

(rf/reg-event-db
 ::init-login-page
 (fn [db [_]]
   (if (= (root-path db) nil)
     (assoc-in db [root-path] schema)
     db)))

(pages/reg-page :core/login index)
