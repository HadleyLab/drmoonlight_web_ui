(ns ui.widgets
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as rf]
   [cljs.pprint :as pp]
   [soda-ash.core :as sa]
   [cljs-time.core :as dt]
   [cljs-time.format :as format]
   [cljsjs.react-datepicker]
   [cljsjs.moment]
   [sodium.core :as na]))


(defn moment->cljs [m]
  (format/parse (.format m)))

(defn cljs->moment [c]
  (let [date-format "yyyy-MM-dd hh:mm a Z"]
  (if (not= c "")
    (js/moment (format/unparse (format/formatter date-format) c))
    (js/moment))))

(def date-picker (reagent/adapt-react-class (.-default js/DatePicker)))

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
(defn form-toggle [{cursor :cursor label :label}]
  (na/checkbox {:toggle true
                :label label
                :checked? (boolean @cursor)
                :on-change (na/>atom cursor)}))

(defn form-datepicker [{cursor :cursor label :label :as info_}]
  (let [info (dissoc info_ :cursor :label :type)]
    [:div.field
     [:label label]
     [date-picker (merge {:selected (cljs->moment @cursor) :onChange #(reset! cursor (moment->cljs %))} info)]]))


(defn get-default-type [field]
  (cond
    (= field :password) :password
    (= field :email) :email
    :else :text))

(defn form-textarea [{cursor :cursor label :label}]
  [:div.field
   [:label label]
   [na/text-area {:value @cursor :on-change (na/>atom cursor)}]])

(defn render-input [{cursor :cursor field :field}]
  (let [field-cursor (reagent/cursor cursor [:fields field])
        errors-cursor (reagent/cursor cursor [:response :errors field])]
    (fn [{field :field info :info}]
      (let [errors @errors-cursor]
        [na/form-group {}
         (if (string? info)
           [na/form-input
            {:label info
             :type (get-default-type field)
             :value @field-cursor
             :error? (not= errors nil)
             :on-change (na/>atom field-cursor)}]
           (cond
             (= :radio (:type info)) [form-radio (merge {:cursor field-cursor} info)]
             (= :toggle (:type info)) [form-toggle (merge {:cursor field-cursor} info)]
             (= :textarea (:type info)) [form-textarea (merge {:cursor field-cursor} info)]
             (= :date-picker (:type info)) [form-datepicker (merge {:cursor field-cursor} info)]
             :else [:div (str info)]))
         (if (= errors nil)
           [:div]
           [:div {:class "error"} (clojure.string/join " " errors)])]))))

(defn build-form [cursor field-sets]
  (concatv [:div]
           (->> field-sets
                (mapv (fn [[title fields]]
                        (concatv [:div {:key title} [:label title]]
                                 (->> fields
                                      (mapv (fn [[field info]]
                                              [render-input {:cursor cursor :field field :info info :key field}])))))))))

(defn form-wrapper []
  (let [this (reagent/current-component)]
    [:div.moonlight-form-wrapper
     [na/container {:text? true :class-name "moonlight-form"}
      (into [:div {:class "moonlight-inner-form"}]
            (reagent/children this))]]))

(defn fields->schema [fields]
  (into {} (map (fn [[key _]] [key ""]) (mapcat second fields))))
