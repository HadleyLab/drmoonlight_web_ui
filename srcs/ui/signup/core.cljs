(ns ui.signup.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require
   [reagent.core :as reagent]
   [ui.pages :as pages]
   [ui.routes :refer [href]]
   [re-frame.core :as rf]
   [clojure.string :as str]
   [soda-ash.core :as sa]
   [sodium.core :as na]))

(defn form-radio [{items :items cursor :cursor label :label}]
  (into [] (concat
            [na/form-group {}
             [:label label]]
            (for [[key value] items]
              [sa/FormRadio {:key key
                             :label key
                             :value value
                             :checked (= @cursor key)
                             :on-change #(reset! cursor key)}]))))

(def sceduler-form-fields
  {"Personal Information"
   {:first-name "First Name"
    :last-name "Last Name"
    :facility-name "Hospital / Facility name"
    :department-name "Department name"
    }
   "Account settings"
   {:email "Email"
    :password "Password"
    }})

(def resident-form-fields
  {"Personal Information"
   {:first-name "First Name"
    :last-name "Last Name"
    }
   "Account settings"
   {:email "Email"
    :password "Password"
    }})


(defn get-default-type [field]
  (cond
    (= field :password) :password
    (= field :email) :email
    :else :text))

(defn build-form [cursor field-sets]
  (into [] (concat [:div]
                   (for [[title fields] field-sets]
                     (into [] (concat
                               [:div.moonlight-form-group [:label title]]
                                (for [[field label] fields
                                      :let [field-cursor (reagent/cursor cursor [field])]]
                                  [na/form-input
                                   {:label label
                                    :type (get-default-type field)
                                    :value @field-cursor
                                    :on-change (na/>atom field-cursor)}])))))))

(defn form-content [{mode :mode cursor :cursor}]
  (if (= mode :resident)
    (build-form cursor resident-form-fields)
    (build-form cursor sceduler-form-fields)))

(defn form []
  (let [sign-up-form @(rf/subscribe [:cursor [:sign-up-form]])
        mode (reagent/cursor sign-up-form [:mode])
        modes {:resident "Resident" :scheduler "Scheduler"}]
    (fn []
      [na/form {}
       [form-radio {:items modes :cursor mode :label "Register as"}]
       [form-content {:mode @mode :cursor sign-up-form}]
       [:div.moonlight-form-group
        [na/form-button {:content "Sign up"
                         :color :blue
                         :on-click (na/>event [:register @sign-up-form])}]]])))

(defn index [params]
  [na/container {:text? true :class-name "moonlight-form"}
   [na/header {:as :h1} "Welcome to Dr. Moonlight!"]
   [(form)]])
 

  ;; [na/grid {}
  ;;  [na/grid-row {}
  ;;   [na/grid-column {:width 3} [:div]]
  ;;   [na/grid-column {:width 10 :class-name "moonlight-form"}
  ;;    [na/grid {}
  ;;     [na/grid-row {}
  ;;      [na/grid-column {:width 12}
  ;;       [na/header {:as :h1} "Welcome to Dr. Moonlight!"]]
  ;;       [(form)]]]]
  ;;   [na/grid-column {:width 3} [:div]]]])

(pages/reg-page :core/sigh-up index)
