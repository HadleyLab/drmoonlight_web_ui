(ns ui.resident.message
  (:require
   [reagent.core :as reagent]
   [ui.db.misc :refer [>event >atom <sub get-url reduce-statuses text-with-br]]
   [ui.db.shift :refer [as-apply-date-time as-hours-interval]]
   [ui.db.application :refer [application-statuses]]
   [ui.pages :as pages]
   [ui.routes :refer [href]]
   [ui.widgets :refer [concatv]]
   [ui.widgets.message :refer [Discussion]]
   [ui.resident.layout :refer [ResidentLayout ResidentProfileLayout]]
   [re-frame.core :as rf]
   [clojure.string :as str]
   [soda-ash.core :as sa]))

(defn ApplyForm [shift-pk name]
  (let [apply-for-shift-result (<sub [:apply-for-shift shift-pk])]
    [sa/Segment
     (when (= (:status apply-for-shift-result) :failure)
       [sa/Message {:negative true}
        [sa/MenuHeader "Error"]
        [:p (str (:errors apply-for-shift-result))]])
     [sa/Form
      [:p (str "I would like to apply for the " name " shift")]
      [sa/FormButton {:color :green :on-click (>event [:apply-for-shift shift-pk])} "Apply"]]]))

(defn ShiftLayout [shift-pk content]
  (let [{status :status shift :data} (<sub [:shift-info shift-pk])
        data-is-loading (or (and (nil? shift) (= status :loading)) (= status :not-asked))]
    [ResidentLayout
     [sa/Grid {}
      [sa/GridRow {}
       [sa/GridColumn {:width 3}
        [sa/Segment
         [sa/Dimmer {:active  data-is-loading} [sa/Loader]]
         (when (not data-is-loading)
           (let [{speciality :speciality
                  start :date-start
                  finish :date-end
                  pk :pk
                  payment-amount :payment-amount
                  payment-per-hour :payment-per-hour
                  description :description} shift]
             [:div
              [sa/Header (:name (<sub [:speciality speciality]))]
              [:p [:strong "Starts: "] (as-apply-date-time start)]
              [:p [:strong "Ends: "] (as-apply-date-time finish)]
              [:p [:strong "Total: "] (as-hours-interval start finish) " hours"]
              [:p [:strong "Payment amount: "] (str "$" payment-amount " per " (if payment-per-hour "hour" "shift"))]
              [:p description]]))]]
       [sa/GridColumn {:width 13 :class-name :moonlight-white}
        content]]]]))

(defn ApplyToShift [{shift-pk :shift-pk}]
  (rf/dispatch [:get-shift-info shift-pk])
  (fn [{shift-pk :shift-pk}]
    (let [{status :status shift :data} (<sub [:shift-info shift-pk])]
      [ShiftLayout shift-pk
       [sa/SegmentGroup
        (when (= status :succeed)
          [ApplyForm
           shift-pk
           (:name (<sub [:speciality (:speciality shift)]))])]])))

(defn DiscussShift [{application-pk :application-pk shift-pk :shift-pk}]
  (rf/dispatch [:get-shift-info shift-pk])
  (rf/dispatch [:get-application-info application-pk])
  (rf/dispatch [:get-application-messages application-pk])
  (fn [{application-pk :application-pk shift-pk :shift-pk}]
    (let [{shift-status :status shift :data} (<sub [:shift-info shift-pk])
          {application-status :status application :data} (<sub [:application-info application-pk])
          {messages-status :status messages :data} (<sub [:application-messages application-pk])
          status (reduce-statuses shift-status application-status messages-status)]
      [ShiftLayout shift-pk [Discussion shift application messages status]])))

(defn format-date-time [start finish]
  (let [start-date (.format start "DD/MM")
        finish-date (.format finish "DD/MM")
        start-time (.format start "h:mm a")
        finish-time (.format finish "h:mm a")]
    (if (= start-date finish-date)
      (str " at " start-date " from " start-time " to " finish-time)
      (str " from " start-date " at " start-time " to " finish-date " at " finish-time))))

(defn get-status-name [state]
  (if (nil? state)
    "All messages"
    (map
     (fn [[key value]] (if (= key state) value))
     application-statuses)))

(defn Application [params]
  (fn [params]
    (let [user-id (<sub [:user-id])
          {date-created :date-created
           state :state
           application-pk :pk
           {speciality :speciality
            start :date-start
            finish :date-end
            shift-pk :pk
            payment-amount :payment-amount
            payment-per-hour :payment-per-hour
            description :description
            :as shift} :shift
           {owner-id :id
            :as owner} :owner
           {last-message-text :text
            :as last-message} :last-message} params]
      [sa/Segment {:key application-pk
                   :class-name "messages__message-wrapper"
                   :on-click (>event [:goto :resident :messages shift-pk :discuss application-pk])}
       [sa/Grid {:class-name "messages__message-grid"}
        [sa/GridColumn {:width 3}
         [:div.gray-font (.fromNow (js/moment date-created))]]
        [sa/GridColumn {:width 10}
         [:b (:name (<sub [:speciality speciality])) (format-date-time start finish)]
         (if last-message-text [:div.messages__message-text
                                (if (= user-id owner-id) [:b "You: "])
                                last-message-text])]
        [sa/GridColumn {:width 3}
         [:div {:class-name (str "messages__message-label" " label-" state)}
          (get-status-name state)]]]])))

(defn ApplicationsDropdown [params]
  (fn [params]
    (let [applications (<sub [:applications])
          filter-state (<sub [:applications-filter-state])]
      [sa/Dropdown {:button true
                    :fluid true
                    :floating true
                    :text (get-status-name filter-state)
                    :class-name (str "messages__dropdown"
                                     (if-not (nil? filter-state) (str " _" filter-state)))}
       [sa/DropdownMenu
        [:div {:class-name (str "messages__dropdown-item"
                                (if (nil? filter-state) " _active"))
               :on-click (>event [:set-applications-filter-state nil])}
         "All messages" [:span.gray-font " (" (count (:data applications)) ")"]]
        (doall (for [[state-key state] application-statuses]
                 [:div {:key state
                        :class-name (str "messages__dropdown-item"
                                         " _" state-key
                                         (if (= state-key filter-state) " _active"))
                        :on-click (>event [:set-applications-filter-state state-key])}
                  state
                  [:span.gray-font
                   (str " (" (<sub [:count-applications-by-state state-key]) ")")]]))]])))

(defn ShowAllApplications [params]
  (rf/dispatch [:get-applications])
  (fn [params]
    (let [applications (<sub [:applications])
          filter-state (<sub [:applications-filter-state])
          applications-count (<sub [:count-applications-by-state filter-state])]
      [ResidentLayout
       [sa/Grid {:class-name "messages__container"}
        [sa/GridRow {}
         [sa/GridColumn {:width 3}
          [ApplicationsDropdown]]
         [sa/GridColumn {:width 13}
          (if (and (some? filter-state) (= applications-count 0))
            [sa/SegmentGroup
             [sa/Segment "No result found"]]
            [sa/SegmentGroup
             (doall (for [application (:data applications)
                          :when (if (nil? filter-state)
                                  true
                                  (= filter-state (:state application)))]
                      [Application application]))])]]]])))

(pages/reg-page :core/resident-messages ShowAllApplications)
(pages/reg-page :core/resident-messages-apply ApplyToShift)
(pages/reg-page :core/resident-messages-discuss DiscussShift)
