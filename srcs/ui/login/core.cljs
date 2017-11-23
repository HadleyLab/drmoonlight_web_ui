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
          [sa/FormGroup {}
           [sa/FormButton {:color :blue
                           :loading (= (:status @response-cursor) :loading)
                           :on-click (>event [:login])} "Login"]
           [:a {:href (href :forgot-password)} "Forgot password?"]]]]
        [sa/GridColumn {:width 5}
         [:p "Don't have account yet?"] [:a {:href (href :sign-up)} "Sign Up"]]]])))

(defn Index [params]
  (rf/dispatch-sync [::init-login-page])
  (fn [params]
    [FormWrapper
     [sa/Header {:as :h1 :class-name "moonlight-form-header"} "Welcome back to Dr. Moonlight!"]
     [Form]]))

(rf/reg-event-db
 ::init-login-page
 (fn [db [_]]
   (if (= (root-path db) nil)
     (assoc-in db [root-path] schema)
     db)))

(rf/reg-event-fx
 :login
 (fn [{db :db} _]
   (let [body (get-in db [root-path :login-form])]
     {:json/fetch {:uri (get-url db "/api/accounts/token/create/")
                   :method "post"
                   :body body
                   :success {:event :login-succeed}
                   :error {:event :login-failure}}
      :db (assoc-in db [root-path :response :status] :loading)})))

(rf/reg-event-fx
 :login-succeed
 (fn [{db :db} [_ {data :data}]]
   {:db (assoc-in db [:account] {:token (:auth-token data)})
    :json/fetch {:uri (get-url db "/api/accounts/me/")
                 :token (:auth-token data)
                 :success {:event :loaded-login-data}
                 :error {:event :login-failure}}}))

(rf/reg-event-fx
 :loaded-login-data
 (fn [{db :db} [_ {data :data}]]
   (let [user-type (cond
                     (:is-resident data) :resident
                     (:is-scheduler data) :scheduler
                     (:is-account-manager data) :account-manager)
         token (get-in db [:account :token])]
     {:db (assoc-in db [:account :user-type] user-type)
      :json/fetch {:uri (get-url db (str "/api/accounts/" (name user-type) "/" (:pk data) "/"))
                   :token token
                   :success {:event :loaded-user-data}
                   :error {:event :login-failure}}})))

(rf/reg-event-fx
 :loaded-user-data
 (fn [{db :db} [_ {data :data}]]
   {:db (-> db
            (assoc-in [:account :user-info] data)
            (assoc-in [root-path] schema))
    :dispatch [:goto (get-in db [:account :user-type])]}))

(rf/reg-event-db
 :login-failure
 (fn [db [_ {data :data}]]
   (-> db
       (assoc-in [root-path :response :status] :failure)
       (assoc-in [root-path :response :errors] data))))

(pages/reg-page :core/login Index)
