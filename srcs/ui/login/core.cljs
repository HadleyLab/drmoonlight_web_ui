(ns ui.login.core
  (:require
   [reagent.core :as reagent]
   [ui.db :refer [get-url >event >atom]]
   [ui.pages :as pages]
   [ui.routes :refer [href]]
   [ui.widgets :refer [FormWrapper pp]]
   [re-frame.core :as rf]
   [clojure.string :as str]
   [soda-ash.core :as sa]))

(def root-path :login-page)

(def schema
  {:login-form
   {:email ""
    :password ""}
   :response {:status :not-asked}})

(defn Form []
  (let [login-form @(rf/subscribe [:cursor [root-path :login-form]])
        response-cursor @(rf/subscribe [:cursor [root-path :response]])
        email (reagent/cursor login-form [:email])
        password (reagent/cursor login-form [:password])]
    (fn []
      [sa/Grid {:divided true}
       [sa/GridRow {}
        [sa/GridColumn {:width 11}
         [sa/Form {:error (= (:status @response-cursor) :failure)}
          [sa/FormField
           [:label "Email"]
           [sa/Input {:type "email" :value @email :on-change (>atom email)}]]
          [sa/FormField
           [:label "Password"]
           [sa/Input {:type "password" :value @password :on-change (>atom password)}]]
          [sa/FormGroup {:class-name "justify-content _space-between"}
           [sa/FormButton {:color :blue
                           :loading (= (:status @response-cursor) :loading)
                           :on-click (>event [:login])} "Login"]
           [sa/FormField {:class-name "login__forgot-password"}
            [:a {:href (href :forgot-password)} "Forgot password?"]]]]]
        [sa/GridColumn {:width 5 :class-name "login__right-column align-items _center"}
         [:div "Don't have account yet?"] [:a {:href (href :sign-up)} "Sign Up"]]]])))

(defn Index [params]
  (rf/dispatch-sync [::init-login-page])
  (fn [params]
    [FormWrapper
     [sa/Header {:as :h1 :class-name "moonlight-form-header"} "Welcome back to Dr. Moonlight!"]
     [Form]]))

(rf/reg-event-db
 ::init-login-page
 (fn [db [_ & force]]
   (if (or force (= (root-path db) nil))
     (assoc-in db [root-path] schema)
     db)))

(rf/reg-event-fx
 :login
 (fn [{db :db} _]
   (let [body (get-in db [root-path :login-form])]
     {:json/fetch {:uri (get-url db "/api/accounts/token/create/")
                   :method "post"
                   :body body
                   :success {:event :login-succeed
                             :fxs {:succeed-fx (fn [db2]
                                                 {:dispatch-n
                                                  [[::init-login-page :force]
                                                   [:goto (get-in db2 [:account :user-type])]]})}}
                   :error {:event :login-failure}}
      :db (assoc-in db [root-path :response :status] :loading)})))

(rf/reg-event-db
 :login-failure
 (fn [db [_ {data :data}]]
   (-> db
       (assoc-in [root-path :response :status] :failure)
       (assoc-in [root-path :response :errors] data))))

(pages/reg-page :core/login Index)
