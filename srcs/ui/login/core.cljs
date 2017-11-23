(ns ui.login.core
  (:require
   [reagent.core :as reagent]
   [ui.db :refer [get-url]]
   [ui.pages :as pages]
   [ui.routes :refer [href]]
   [ui.widgets :refer [form-wrapper pp]]
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
        response-cursor @(rf/subscribe [:cursor [root-path :response]])
        email (reagent/cursor login-form [:email])
        password (reagent/cursor login-form [:password])]
    (fn []
      [na/grid {}
       [na/grid-row {}
        [na/grid-column {:width 10}
         [na/form {:error? (= (:status @response-cursor) :failure)}
          [na/form-input {:label "Email" :type "email" :value @email :on-change (na/>atom email)}]
          [na/form-input {:label "Password" :type "password" :value @password :on-change (na/>atom password)}]
          [na/form-group {}
           [na/form-button {:content "Login"
                            :color :blue
                            :loading? (= (:status @response-cursor) :loading)
                            :on-click (na/>event [:login])}]
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
   {:db (assoc-in db [:account] {:token (:authToken data)})
    :json/fetch {:uri (get-url db "/api/accounts/me/")
                 :token (:authToken data)
                 :success {:event :loaded-login-data}
                 :error {:event :login-failure}}}))

(rf/reg-event-fx
 :loaded-login-data
 (fn [{db :db} [_ {data :data}]]
   (let [user-type (cond
                     (:isResident data) :resident
                     (:isScheduler data) :scheduler
                     (:isAccountManager data) :account-manager)
         _ (.log js/console data)
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

(pages/reg-page :core/login index)
