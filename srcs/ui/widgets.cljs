(ns ui.widgets
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as rf]
   [cljs.pprint :as pp]
   [soda-ash.core :as sa]
   [sodium.core :as na]))

(defn pp [data]
  [:pre (with-out-str (pp/pprint data))])

(def concatv (comp (partial into []) concat))

(defn form-radio [{items :items cursor :cursor label :label}]
  (concatv [na/form-group {} [:div.field [:label label]]]
           (->> items
                (mapv (fn [[key value]]
                        [sa/FormRadio {:key key
                                       :label key
                                       :value value
                                       :checked (= @cursor key)
                                       :on-change #(reset! cursor key)}])))))

(defn get-default-type [field]
  (cond
    (= field :password) :password
    (= field :email) :email
    :else :text))

(defn render-input [{cursor :cursor field :field}]
  (let [field-cursor (reagent/cursor cursor [:fields field])
        errors-cursor (reagent/cursor cursor [:response :errors field])]
    (fn [{field :field label :label}]
      (let [errors @errors-cursor]
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

(defn build-form [cursor field-sets]
  (concatv [:div]
           (->> field-sets
                (mapv (fn [[title fields]]
                        (concatv [:div {:key title} [:label title]]
                                 (->> fields
                                      (mapv (fn [[field label]]
                                              [render-input {:cursor cursor :field field :label label :key field}])))))))))

(defn form-wrapper []
  (let [this (reagent/current-component)]
    [:div.moonlight-form-wrapper
     [na/container {:text? true :class-name "moonlight-form"}
      (into [:div {:class "moonlight-inner-form"}]
            (reagent/children this))]]))
