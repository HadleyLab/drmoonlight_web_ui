(ns ui.widgets
  (:require
   [reagent.core :as reagent]
   [ui.db.misc :refer [>event >atom dispatch-set!]]
   [re-frame.core :as rf]
   [cljs.pprint :as pp]
   [cljsjs.react-datepicker]
   [soda-ash.core :as sa]))

(def DatePicker (reagent/adapt-react-class (.-default js/DatePicker)))

(defn pp [data]
  [:pre (with-out-str (pp/pprint data))])

(def concatv (comp (partial into []) concat))

(defn- Description [desc]
  (if desc
    [:span {:class-name "form__label-desc"} " (" desc ")"]))

(defn FormRadio [{items :items cursor :cursor label :label desc :desc}]
  (concatv [sa/FormField {:width 11} [:label {:class-name "form__label"}
                                      label [Description desc]]]
           (->> items
                (mapv (fn [[key value]]
                        [sa/FormRadio {:key key
                                       :class-name "form__radio"
                                       :label value
                                       :value key
                                       :checked (= @cursor key)
                                       :on-change #(dispatch-set! cursor key)}])))))

(defn FormToggle [{cursor :cursor label :label}]
  [sa/FormField {:width 11}
   [sa/Checkbox
    {:toggle true
     :label label
     :class-name "toggler"
     :checked (boolean @cursor)
     :on-change (>atom cursor)}]])

(defn FormDatepicker [{cursor :cursor label :label :as info_}]
  (let [info (dissoc info_ :cursor :label :type)]
    [sa/FormField {:width 11}
     [:label label]
     [DatePicker (merge {:selected @cursor
                         :on-change #(reset! cursor %)} info)]]))

(defn get-default-type [field]
  (cond
    (= field :password) :password
    (= field :email) :email
    :else :text))

(defn FormTextarea [{cursor :cursor label :label desc :desc}]
  [sa/FormField {:width 11}
   [:label.form__label label [Description desc]]
   [sa/TextArea {:value @cursor :on-change (>atom cursor)}]])

(defn FormInputWithDropDown [field-cursor cursor {label :label drop-down :drop-down width :width}]
  (let [drop-down-cursor (reagent/cursor cursor [:fields (:cursor drop-down)])
        _ (.log js/console drop-down)]
    (fn [field-cursor cursor {label :label drop-down :drop-down}]
      [sa/FormField {:width (or width 11)}
       [:label label]
       [sa/Input {:label (reagent/as-element
                          [sa/Dropdown {:value @drop-down-cursor
                                        :on-change (>atom drop-down-cursor)
                                        :options (:options drop-down)}])
                  :value @field-cursor
                  :on-change (>atom field-cursor)
                  :label-position :right}]])))

(defn FormSelect [{cursor :cursor label :label items :items desc :desc}]
  [sa/FormField {:width 11}
   [:label.form__label label [Description desc]]
   [sa/FormSelect {:value @cursor
                   :placeholder label
                   :on-change (>atom cursor)
                   :options (items)}]])

(defn FormMultySelect [{cursor :cursor label :label items :items}]
  [sa/FormField {:width 11}
   [:label label]
   [sa/Dropdown {:value @cursor
                 :placeholder label
                 :fluid true
                 :multiple true
                 :selection true
                 :on-change (>atom cursor)
                 :options (items)}]])

(defn RenderInput [{cursor :cursor field :field hide-error :hide-error}]
  (let [field-cursor (reagent/cursor cursor [:fields field])
        errors-cursor (reagent/cursor cursor [:response :errors field])]
    (fn [{field :field info :info}]
      (let [errors @errors-cursor]
        (if (not= :mock (:type info))
          [sa/FormGroup {}
           (if (or (= :input (:type info)) (string? info))
             [sa/FormField {:width (or (:width info) 11)}
              [:label.form__label (or (:label info) info) [Description (:desc info)]]
              [sa/Input {:type (get-default-type field)
                         :value @field-cursor
                         :error (not= errors nil)
                         :on-change (>atom field-cursor)}]]
             (cond
               (= :radio (:type info)) [FormRadio (merge {:cursor field-cursor} info)]
               (= :toggle (:type info)) [FormToggle (merge {:cursor field-cursor} info)]
               (= :textarea (:type info)) [FormTextarea (merge {:cursor field-cursor} info)]
               (= :date-picker (:type info)) [FormDatepicker (merge {:cursor field-cursor} info)]
               (= :input-with-drop-down (:type info)) [FormInputWithDropDown field-cursor cursor info]
               (= :select (:type info)) [FormSelect (merge {:cursor field-cursor} info)]
               (= :multy-select (:type info)) [FormMultySelect (merge {:cursor field-cursor} info)]
               :else [:div (str info)]))
           (if-not (or (true? hide-error) (nil? errors))
             [sa/FormField {:width 5 :class-name "input-info error"}
              (clojure.string/join " " errors)])])))))

(defn BuildForm [cursor field-sets hide-field-errors]
  (concatv [:div]
           (->> field-sets
                (mapv (fn [[title fields]]
                        (concatv [:div {:key title :class-name (str "form__group"
                                                                    (if (= title "") " _without-title"))}
                                  (if (not= title "") [:label {:class-name "form__group-title"} title])]
                                 (->> fields
                                      (mapv (fn [[field info]]
                                              [RenderInput {:cursor cursor
                                                            :field field
                                                            :info info :key field
                                                            :hide-error hide-field-errors}])))))))))

(defn FormWrapper []
  (let [this (reagent/current-component)]
    [sa/Container {:text true :class-name "moonlight-form"}
     [sa/Segment {:class-name "moonlight-form-inner"}
      (into [:div {}]
            (reagent/children this))]]))
