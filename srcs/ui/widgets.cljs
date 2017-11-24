(ns ui.widgets
  (:require
   [reagent.core :as reagent]
   [ui.db :refer [>event >atom dispatch-set!]]
   [re-frame.core :as rf]
   [cljs.pprint :as pp]
   [cljs-time.core :as dt]
   [cljs-time.format :as format]
   [cljsjs.react-datepicker]
   [cljsjs.moment]
   [soda-ash.core :as sa]))

(defn moment->cljs [m]
  (format/parse (.format m)))

(defn cljs->moment [c]
  (let [date-format "yyyy-MM-dd hh:mm a Z"]
    (if (not= c "")
      (js/moment (format/unparse (format/formatter date-format) c))
      (js/moment))))

(def DatePicker (reagent/adapt-react-class (.-default js/DatePicker)))

(defn pp [data]
  [:pre (with-out-str (pp/pprint data))])

(def concatv (comp (partial into []) concat))

(defn FormRadio [{items :items cursor :cursor label :label}]
  (concatv [sa/FormGroup {} [:div.field [:label label]]]
           (->> items
                (mapv (fn [[key value]]
                        [sa/FormRadio {:key value
                                       :label key
                                       :value value
                                       :checked (= @cursor value)
                                       :on-change #(dispatch-set! cursor value)}])))))

(defn FormToggle [{cursor :cursor label :label}]
  [sa/Checkbox
   {:toggle true
    :label label
    :checked (boolean @cursor)
    :on-change (>atom cursor)}])

(defn FormDatepicker [{cursor :cursor label :label :as info_}]
  (let [info (dissoc info_ :cursor :label :type)]
    [:div.field
     [:label label]
     [DatePicker (merge {:selected (cljs->moment @cursor) :on-change #(reset! cursor (moment->cljs %))} info)]]))

(defn get-default-type [field]
  (cond
    (= field :password) :password
    (= field :email) :email
    :else :text))

(defn FormTextarea [{cursor :cursor label :label}]
  [:div.field
   [:label label]
   [sa/TextArea {:value @cursor :on-change (>atom cursor)}]])

(defn FormInputWithDropDown [field-cursor cursor {label :label drop-down :drop-down}]
  (let [drop-down-cursor (reagent/cursor cursor [(:cursor drop-down)])]
    (fn [field-cursor cursor {label :label drop-down :drop-down}]
      [:div.field
       [:label label]
       [sa/Input {:label (reagent/as-element
                          [sa/Dropdown {:default-value true
                                        :options (:options drop-down)}])
                  :label-position :right}]])))

(defn FormSelect [{cursor :cursor label :label items :items}]
  [sa/FormSelect {:value @cursor
                  :placeholder label
                  :label label
                  :on-change (>atom cursor)
                  :options (items)}])

(defn FormMultySelect [{cursor :cursor label :label items :items}]
  [:div.field
   [:label label]
   [sa/Dropdown {:value @cursor
                 :placeholder label
                 :fluid true
                 :multiple true
                 :selection true
                 :on-change (>atom cursor)
                 :options (items)}]])

(defn RenderInput [{cursor :cursor field :field}]
  (let [field-cursor (reagent/cursor cursor [:fields field])
        errors-cursor (reagent/cursor cursor [:response :errors field])]
    (fn [{field :field info :info}]
      (let [errors @errors-cursor]
        [sa/FormGroup {}
         (if (string? info)
           [sa/FormField {:width 10}
            [:label info]
            [sa/Input {:type (get-default-type field)
                       :value @field-cursor
                       :error (not= errors nil)
                       :on-change (>atom field-cursor)}]]
           (cond
             (= :radio (:type info)) [FormRadio (merge {:cursor field-cursor} info)]
             (= :toggle (:type info)) [FormToggle (merge {:cursor field-cursor} info)]
             (= :textarea (:type info)) [FormTextarea (merge {:cursor field-cursor} info)]
             (= :date-picker (:type info)) [FormDatepicker (merge {:cursor field-cursor} info)]
             (= :mock (:type info)) nil
             (= :input-with-drop-down (:type info)) [FormInputWithDropDown field-cursor cursor info]
             (= :select (:type info)) [FormSelect (merge {:cursor field-cursor} info)]
             (= :multy-select (:type info)) [FormMultySelect (merge {:cursor field-cursor} info)]
             :else [:div (str info)]))
         (if (= errors nil)
           [sa/FormField {:width 6 :class-name "input-info gray-font"} ""]
           [sa/FormField {:width 6 :class-name "input-info error"}
            (clojure.string/join " " errors)])]))))

(defn BuildForm [cursor field-sets]
  (concatv [:div]
           (->> field-sets
                (mapv (fn [[title fields]]
                        (concatv [:div {:key title :class-name "moonlight-form-group"} [:label title]]
                                 (->> fields
                                      (mapv (fn [[field info]]
                                              [RenderInput {:cursor cursor :field field :info info :key field}])))))))))

(defn FormWrapper []
  (let [this (reagent/current-component)]
    [sa/Container {:text true :class-name "moonlight-form"}
     [sa/Segment {:class-name "moonlight-form-inner"}
      (into [:div {}]
            (reagent/children this))]]))

(defn fields->schema [fields]
  (into {} (map (fn [[key _]] [key ""]) (mapcat second fields))))

(defn setup-form-initial-values [initial-values]
  (fn [data]
    (into {} (map (fn [[key value]] [key (initial-values key value)]) data))))
