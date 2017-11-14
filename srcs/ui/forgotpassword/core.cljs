(ns ui.forgotpassword.core
  (:require
   [reagent.core :as reagent]
   [ui.pages :as pages]
   [ui.routes :refer [href]]
   [ui.widgets :refer [form-wrapper]]
   [re-frame.core :as rf]
   [clojure.string :as str]
   [sodium.core :as na]))

(def root-path :reset-password-page)

(def schema
  {:reset-password-form
   {:email ""}
   :response {:status :not-asked}})

(defn form []
  (let [reset-password-page @(rf/subscribe [:cursor [root-path]])
        email (reagent/cursor reset-password-page [:reset-password-form :email])
        response (reagent/cursor reset-password-page [:response])]
    (fn []
      (let [status (:status @response)
            email-errors (:email (:errors @response))]
        [na/form {:error? (= status :failure)}
         [na/form-input {:label "Email"
                         :type "email"
                         :error? (not= email-errors nil)
                         :value @email
                         :on-change (na/>atom email)}]
         (when email-errors
           [:div {:class "error"} (clojure.string/join " " email-errors)])
         [na/form-group {}
          [na/form-button {:content "Request reset link"
                           :color :blue
                           :on-click (na/>event [:rest-password])}]
          [:a {:href (href :login)} "Back to login"]]]))))

(defn index [params]
  (rf/dispatch [:init-forgot-password-page])
  (fn [params]
    [form-wrapper
     [na/header {:as :h1 :class-name "moonlight-form-header"} "Forgot your password?"]
     [:p "Enter your email address below and we'll get you back on track."]
     [(form)]]))

(rf/reg-event-db
 :init-forgot-password-page
 (fn [db [_]]
   (if (= (root-path db) nil)
     (assoc-in db [root-path] schema)
     db)))

(rf/reg-event-fx
 :rest-password
 (fn [{db :db} [_]]
   (let [email (get-in db [root-path :reset-password-form :email])]
     {:json/fetch {:uri "http://localhost:8000/api/accounts/password/reset/"
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


(defn thanks [params]
  [form-wrapper
   [na/header {:as :h1 :class-name "moonlight-form-header"} "You have reseted your password!"]
   [:p "You’ve just been sent an email with reset link. Please click on the link in this email to change your password"]
   [na/button {:basic? true :color :blue :content "Go Home" :on-click (na/>event [:goto "/"])}]])


(pages/reg-page :core/forgot-password index)
(pages/reg-page :core/forgot-password-thanks thanks)
