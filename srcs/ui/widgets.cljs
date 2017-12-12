(ns ui.widgets
  (:require
   [reagent.core :as reagent]
   [ui.db.misc :refer [>event >atom dispatch-set!]]
   [re-frame.core :as rf]
   [cljs.pprint :as pp]
   [cljsjs.react-datepicker]
   [clojure.string :as str]
   [soda-ash.core :as sa]))

(def DatePicker (reagent/adapt-react-class (.-default js/DatePicker)))

(defn pp [data]
  [:pre (with-out-str (pp/pprint data))])

(def concatv (comp (partial into []) concat))

(defn- Description [desc]
  (if desc
    [:span {:class-name "form__label-desc"} " (" desc ")"]))

(defn FormRadio [{items :items cursor :cursor label :label desc :desc error :error}]
  (concatv [sa/FormField {:width 11 :error error :class-name "_radio"}
            [:label {:class-name "form__label"}
             label [Description desc]]]
           (->> items
                (mapv (fn [[key value]]
                        [sa/FormRadio {:key key
                                       :class-name "form__radio"
                                       :label value
                                       :value key
                                       :checked (= @cursor key)
                                       :on-change #(dispatch-set! cursor key)}])))))

(defn FormToggle [{cursor :cursor label :label error :error}]
  [sa/FormField {:width 11 :error error}
   [sa/Checkbox
    {:toggle true
     :label label
     :class-name "toggler"
     :checked (boolean @cursor)
     :on-change (>atom cursor)}]])

(defn get-time-options []
  (for [x (range 96)
        :let [total-minutes (* x 15)
              hours  (quot total-minutes 60)
              minutes (* 15 (mod x 4))
              minutes (if (= minutes 0) "00" minutes)
              value (str hours ":" minutes)
              text (if (> hours 12)
                     (str (- hours 12) ":" minutes " pm")
                     (str value " am"))]]
    {:key value :value value :text text}))

(defn FormDateTimePicker [{cursor :cursor label :label error :error :as info_}]
  (let [info (dissoc info_ :cursor :label :type :error)
        value @cursor
        value (if (or (= value "") (nil? value)) (.set (js/moment) "minute" 0) value)]
    [sa/FormField {:width 11 :error error}
     [:label label]
     [sa/FormGroup
      [sa/FormField {:width 6 :class-name "date-time-picker__field"}
       [DatePicker (merge {:selected value
                           :on-change #(reset! cursor %)} info)]
       [sa/Icon {:name "calendar outline" :class-name "date-time-picker__icon"}]]
      [sa/FormField {:width 5 :class-name "date-time-picker__field"}
       [sa/FormSelect {:value (.format value "H:mm")
                       :placeholder "Select time"
                       :on-change
                       (fn [e v]
                         (let [time (or (.-value v) (.-checked v))
                               [hour minute] (str/split time #":")]
                           (reset!
                            cursor
                            (-> (js/moment value)
                                (.set "hour" hour)
                                (.set "minute" minute)))))
                       :options (get-time-options)}]
       [sa/Icon {:name "clock" :class-name "date-time-picker__icon"}]]]]))

(defn get-default-type [field]
  (cond
    (= field :password) :password
    (= field :email) :email
    :else :text))

(defn FormTextarea [{cursor :cursor label :label desc :desc error :error}]
  [sa/FormField {:width 11 :error error}
   [:label.form__label label [Description desc]]
   [sa/TextArea {:value @cursor :on-change (>atom cursor)}]])

(defn FormInputWithDropDown [field-cursor cursor {drop-down :drop-down}]
  (let [drop-down-cursor (reagent/cursor cursor [:fields (:cursor drop-down)])]
    (fn [field-cursor cursor {label :label drop-down :drop-down width :width error :error}]
      [sa/FormField {:width (or width 11) :error error}
       [:label label]
       [sa/Input {:label (reagent/as-element
                          [sa/Dropdown {:value @drop-down-cursor
                                        :on-change (>atom drop-down-cursor)
                                        :options (:options drop-down)}])
                  :value @field-cursor
                  :on-change (>atom field-cursor)
                  :label-position :right}]])))

(defn FormSelect [{cursor :cursor label :label items :items desc :desc error :error}]
  [sa/FormField {:width 11 :error error}
   [:label.form__label label [Description desc]]
   [sa/FormSelect {:value @cursor
                   :placeholder label
                   :on-change (>atom cursor)
                   :options (items)}]])

(defn FormMultySelect [{cursor :cursor label :label items :items error :error}]
  [sa/FormField {:width 11 :error error}
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
             [sa/FormField {:width (or (:width info) 11) :error (:error info)}
              [:label.form__label (or (:label info) info) [Description (:desc info)]]
              [sa/Input {:type (get-default-type field)
                         :value @field-cursor
                         :error (not= errors nil)
                         :on-change (>atom field-cursor)}]]
             (case (:type info)
               :radio [FormRadio (merge {:cursor field-cursor} info)]
               :toggle [FormToggle (merge {:cursor field-cursor} info)]
               :textarea [FormTextarea (merge {:cursor field-cursor} info)]
               :date-time-picker [FormDateTimePicker (merge {:cursor field-cursor} info)]
               :input-with-drop-down [FormInputWithDropDown field-cursor cursor info]
               :select [FormSelect (merge {:cursor field-cursor} info)]
               :multy-select [FormMultySelect (merge {:cursor field-cursor} info)]
               [:div (str info)]))
           (if-not (or (true? hide-error) (nil? errors))
             [sa/FormField {:width 5 :class-name "input-info error"}
              (clojure.string/join " " errors)])])))))

(defn check-if-dropdown-error [data errors]
  (if (= (:type data) :input-with-drop-down)
    (some? ((:cursor (:drop-down data)) errors))))

(defn prepare-data [cursor data field]
  (let [errors-cursor (reagent/cursor cursor [:response :errors])]
    (if (or (some? (field @errors-cursor))
            (check-if-dropdown-error data @errors-cursor))
      (merge data {:error true})
      data)))

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
                                                            :info (prepare-data cursor info field)
                                                            :key field
                                                            :hide-error hide-field-errors}])))))))))

(defn FormWrapper []
  (let [this (reagent/current-component)]
    [sa/Container {:text true :class-name "moonlight-form"}
     [sa/Segment {:class-name "moonlight-form-inner"}
      (into [:div {}]
            (reagent/children this))]]))
