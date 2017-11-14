(ns ui.widgets
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as rf]
   [cljs.pprint :as pp]
   [soda-ash.core :as sa]
   [sodium.core :as na]))

(defn pp [data]
  [:pre (with-out-str (pp/pprint data))])

(defn form-radio [{items :items cursor :cursor label :label}]
  (into []
        (concat [na/form-group {} [:div.field [:label label]]]
                (for [[key value] items]
                  [sa/FormRadio {:key key
                                 :label key
                                 :value value
                                 :checked (= @cursor key)
                                 :on-change #(reset! cursor key)}]))))

(defn get-default-type [field]
  (cond
    (= field :password) :password
    (= field :email) :email
    :else :text))

(defn render-input [path field label]
  (let [cursor @(rf/subscribe [:cursor path])
        field-cursor (reagent/cursor cursor [:fields field])]
    (fn []
      (let [errors (get-in @cursor [:response :errors field])]
        [na/form-group {}
         [na/form-input
          {:label label
           :type (get-default-type field)
           :value @field-cursor
           :error? (not= errors nil)
           :on-change (na/>atom field-cursor)}]
         (if (= errors nil)
           [:div]
           [:div {:class "error"} (clojure.string/join " " errors)])]))))

(defn build-form [{field-sets :field-sets path :path}]
  (into [] (concat [:div]
                   (for [[title fields] field-sets]
                     (into [] (concat
                               [:div [:label title]]
                               (for [[field label] fields]
                                 [(render-input path field label) ])))))))

(defn form-wrapper [& children]
  [:div.moonlight-form-wrapper
   [na/container {:text? true :class-name "moonlight-form"}
    [:div {:class "moonlight-inner-form"}
     children]]])
