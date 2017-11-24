(ns ui.forgotpassword.core
  (:require
   [reagent.core :as reagent]
   [ui.db :refer [get-url >event >atom]]
   [ui.pages :as pages]
   [ui.routes :refer [href]]
   [ui.widgets :refer [FormWrapper]]
   [re-frame.core :as rf]
   [clojure.string :as str]
   [soda-ash.core :as sa]))

(def root-path :reset-password-page)

(def schema
  {:reset-password-form
   {:email ""}
   :response {:status :not-asked}})

(defn Form []
  (let [reset-password-page @(rf/subscribe [:cursor [root-path]])
        email (reagent/cursor reset-password-page [:reset-password-form :email])
        response (reagent/cursor reset-password-page [:response])]
    (fn []
      (let [status (:status @response)
            email-errors (:email (:errors @response))]
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
         [sa/FormGroup {:class-name "flex-direction _column"}
          [sa/FormButton {:color :blue
                          :loading (= status :loading)
                          :on-click (>event [:rest-password])}
           "Request reset link"]
          [sa/FormField {:class-name "forgot-password__back"}
           [:a {:href (href :login)} "Back to login"]]]]))))

(defn Index [params]
  (rf/dispatch-sync [::init-forgot-password-page])
  (fn [params]
    [FormWrapper
     [sa/Header {:as :h1 :class-name "moonlight-form-header"} "Forgot your password?"]
     [:div {:class-name "forgot-password__subheader"}
      "Enter your email address below and we'll get you back on track."]
     [Form]]))

(rf/reg-event-db
 ::init-forgot-password-page
 (fn [db [_]]
   (let [login-form-email (get-in db [:login-page :login-form :email])]
     (assoc-in (if (= (root-path db) nil)
                 (assoc-in db [root-path] schema)
                 db) [root-path :reset-password-form :email] (or login-form-email "")))))

(rf/reg-event-fx
 :rest-password
 (fn [{db :db} [_]]
   (let [email (get-in db [root-path :reset-password-form :email])]
     {:json/fetch {:uri (get-url db "/api/accounts/password/reset/")
                   :method "post"
                   :body {:email email}
                   :success {:event :rest-password-succeed}
                   :error {:event :rest-password-failure}}
      :db (assoc-in db [root-path :response :status] :loading)})))

(rf/reg-event-fx
 :rest-password-succeed
 (fn [{db :db} [_]]
   {:dispatch [:goto :forgot-password :thanks]
    :db (assoc-in db [root-path] schema)}))

(rf/reg-event-db
 :rest-password-failure
 (fn [db [_ {data :data}]]
   (-> db
       (assoc-in [root-path :response :status] :failure)
       (assoc-in [root-path :response :errors] data))))

(defn Thanks [params]
  [FormWrapper
   [sa/Header {:as :h1 :class-name "moonlight-form-header"} "You have reseted your password!"]
   [:p "You’ve just been sent an email with reset link. Please click on the link in this email to change your password"]
   [sa/Button {:basic true :color :blue :content "Go Home" :on-click (>event [:goto "/"])}]])

(pages/reg-page :core/forgot-password Index)
(pages/reg-page :core/forgot-password-thanks Thanks)
