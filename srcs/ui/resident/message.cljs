(ns ui.resident.message
  (:require
   [reagent.core :as reagent]
   [ui.db.misc :refer [>event >atom <sub get-url reduce-statuses text-with-br]]
   [ui.db.shift :refer [as-apply-date-time as-hours-interval]]
   [ui.db.application :refer [get-application-status-name]]
   [ui.widgets.applications-dropdown :refer [ApplicationsDropdown]]
   [ui.pages :as pages]
   [ui.routes :refer [href]]
   [ui.widgets :refer [concatv]]
   [ui.widgets.message :refer [Discussion]]
   [ui.resident.layout :refer [ResidentLayout ResidentProfileLayout]]
   [re-frame.core :as rf]
   [clojure.string :as str]
   [soda-ash.core :as sa]
   [ui.widgets.shift-info :refer [get-short-shift-info]]
   [ui.widgets.error-message :refer [ErrorMessage]]))

(defn ApplyForm [shift]
  (rf/dispatch-sync [:init-apply-form (str "I would like to apply for the "
                                           (get-short-shift-info shift))])
  (let [apply-for-shift-result (<sub [:apply-for-shift (:pk shift)])
        comment-cursor (<sub [:comment-cursor])
        status (:status apply-for-shift-result)]
    [:div.chat__apply-for-shift
     [sa/Header {:textAlign "center" :class-name "chat__messages-header"}
      (get-short-shift-info shift)]
     [sa/Form
      [sa/FormInput {:placeholder "Add Comment..."
                     :error (= status :failure)
                     :value @comment-cursor
                     :on-change (>atom comment-cursor)}]
      (when (= status :failure)
        [ErrorMessage {:errors (:errors apply-for-shift-result)}])
      [sa/FormButton {:color :green :on-click (>event [:apply-for-shift (:pk shift)])} "Apply"]]]))

(defn ShiftLayout [shift-pk content]
  (let [{status :status shift :data} (<sub [:shift-info shift-pk])
        data-is-loading (or (and (nil? shift) (= status :loading)) (= status :not-asked))]
    [ResidentLayout
     [sa/Grid {}
      (when (not data-is-loading)
        [sa/GridRow {}
         [sa/GridColumn {:width 3}
          [:span.chat__link {:on-click (>event [:goto :resident :messages])} "Back to all messages"]]
         [sa/GridColumn {:width 13 :class-name "chat__container"}
          content]])]]))

(defn ApplyToShift [{shift-pk :shift-pk}]
  (rf/dispatch [:get-shift-info shift-pk])
  (fn [{shift-pk :shift-pk}]
    (let [{status :status shift :data} (<sub [:shift-info shift-pk])]
      [ShiftLayout shift-pk
       (when (= status :succeed)
         [ApplyForm shift])])))

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
           {last-message-text :text
            last-message-owner :owner
            :as last-message} :last-message} params]
      [sa/Segment {:key application-pk
                   :class-name "messages__message-wrapper"
                   :on-click (>event [:goto :resident :messages shift-pk :discuss application-pk])}
       [sa/Grid {:class-name "messages__message-grid"}
        [sa/GridColumn {:width 3}
         [:div.gray-font (.fromNow (js/moment date-created))]]
        [sa/GridColumn {:width 10}
         [:b (get-short-shift-info shift)]
         (if last-message-text [:div.messages__message-text
                                (if (= user-id last-message-owner) [:b "You: "])
                                last-message-text])]
        [sa/GridColumn {:width 3}
         [:div {:class-name (str "messages__message-label" " label-" state)}
          (get-application-status-name state)]]]])))

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
          [sa/SegmentGroup {:class-name "messages__applications-list"}
           (if (and (some? filter-state) (= applications-count 0))
             [sa/Segment "No result found"]
             (doall (for [application (:data applications)
                          :when (if (nil? filter-state)
                                  true
                                  (= filter-state (:state application)))]
                      ^{:key (:pk application)} [Application application])))]]]]])))

(pages/reg-page :core/resident-messages ShowAllApplications)
(pages/reg-page :core/resident-messages-apply ApplyToShift)
(pages/reg-page :core/resident-messages-discuss DiscussShift)
