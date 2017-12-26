(ns ui.scheduler.message
  (:require
   [reagent.core :as reagent]
   [ui.db.misc :refer [>event >atom <sub get-url reduce-statuses text-with-br]]
   [ui.db.shift :refer [as-apply-date-time as-hours-interval]]
   [ui.db.application :refer [get-application-status-name]]
   [ui.widgets.applications-dropdown :refer [ApplicationsDropdown]]
   [ui.pages :as pages]
   [ui.routes :refer [href]]
   [ui.scheduler.layout :refer [SchedulerLayout]]
   [ui.widgets.message :refer [Discussion]]
   [re-frame.core :as rf]
   [clojure.string :as str]
   [soda-ash.core :as sa]
   [clojure.string :as string]
   [ui.scheduler.schedule.core]
   [ui.widgets.shift-info :refer [ShortShiftInfo]]))

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
           {first-name :first-name
            last-name :last-name
            specialities :specialities
            :as owner} :owner
           {last-message-text :text
            last-message-owner :owner
            :as last-message} :last-message} params]
      [sa/Segment {:key application-pk
                   :class-name (str "messages__message-wrapper" (if (= state 1) " _new"))
                   :on-click (>event [:goto :scheduler :messages shift-pk :discuss application-pk])}
       [sa/Grid {:class-name "messages__message-grid"}
        [sa/GridColumn {:width 3}
         [:b first-name " " last-name]
         [:div.gray-font (.fromNow (js/moment date-created))]]
        [sa/GridColumn {:width 10}
         [:b [ShortShiftInfo shift]]
         (if last-message-text [:div.messages__message-text
                                (if (= user-id last-message-owner) [:b "You: "])
                                last-message-text])
         [:div.messages__message-desc (str/join ", " (map #(<sub [:speciality-name %]) specialities))]]
        [sa/GridColumn {:width 3}
         [:div {:class-name (str "messages__message-label" " label-" state)}
          (get-application-status-name state)]]]])))

(defn ShowAllApplications [params]
  (rf/dispatch [:get-applications])
  (fn [params]
    (let [applications (<sub [:applications])
          filter-state (<sub [:applications-filter-state])
          applications-count (<sub [:count-applications-by-state filter-state])]
      [SchedulerLayout
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

(defn ApplicationLayout [application-info content]
  (let [{status :status application :data} application-info
        data-is-loading (or (and (nil? application) (= status :loading)) (= status :not-asked))]
    [SchedulerLayout
     [sa/Grid {}
      (when (not data-is-loading)
        (let [{owner :owner} application
              {date-joined :date-joined
               id :id} owner
              specialities (map #(<sub [:speciality-name %]) (:specialities owner))]
          [sa/GridRow {}
           [sa/GridColumn {:width 3}
            [:div.chat__resident-name (str (:first-name owner) " " (:last-name owner))]
            [:p (string/join ", " specialities)]
            [:div.gray-font "signed up " (.format (js/moment date-joined) "DD/MM/YYYY")]
            [:span.chat__link {:on-click (>event [:goto :scheduler :detail id])} "Go to user profile"]]
           [sa/GridColumn {:width 13 :class-name "chat__container"}
            content]]))]]))

(defn DiscussShift [{application-pk :application-pk shift-pk :shift-pk}]
  (rf/dispatch [:get-shift-info shift-pk])
  (rf/dispatch [:get-application-info application-pk])
  (rf/dispatch [:get-application-messages application-pk])
  (fn [{application-pk :application-pk shift-pk :shift-pk}]
    (let [{shift-status :status shift :data} (<sub [:shift-info shift-pk])
          {application-status :status application :data :as application-info} (<sub [:application-info application-pk])
          {messages-status :status messages :data} (<sub [:application-messages application-pk])
          status (reduce-statuses shift-status application-status messages-status)]
      [ApplicationLayout application-info [Discussion shift application messages status]])))

(pages/reg-page :core/scheduler-messages ShowAllApplications)
(pages/reg-page :core/scheduler-messages-discuss DiscussShift)
