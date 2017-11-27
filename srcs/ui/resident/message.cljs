(ns ui.resident.message
  (:require
   [reagent.core :as reagent]
   [ui.db :refer [>event >atom <sub get-url reduce-statuses text-with-br]]
   [ui.db.shift :refer [as-apply-date-time as-hours-interval]]
   [ui.pages :as pages]
   [ui.routes :refer [href]]
   [ui.widgets :refer [concatv]]
   [ui.resident.layout :refer [ResidentLayout ResidentProfileLayout]]
   [re-frame.core :as rf]
   [clojure.string :as str]
   [soda-ash.core :as sa]
   [ui.resident.schedule.core]))

(defn MessageForm [application-pk]
  (rf/dispatch-sync [:init-comment-form])
  (let [comment-cursor (<sub [:comment-cursor])]
    (fn []
      (let [{status :status errors :errors} (<sub [:comment-form])]
        [sa/Segment
         [sa/Form {:error (= status :failure)}
          [sa/FormInput {:placeholder "Add comment ..."
                         :error (= status :failure)
                         :value @comment-cursor
                         :on-change (>atom comment-cursor)}]
          (when-not (nil? (:message errors)) [:div.error (str (:message errors))])
          [sa/FormButton {:color :blue
                          :on-click (>event [:add-comment application-pk])} "Send Message"]]]))))

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
  (let [{status :status shift :data} (<sub [:shift-info shift-pk])]
    [ResidentLayout
     [sa/Grid {}
      [sa/GridRow {}
       [sa/GridColumn {:width 3}
        [sa/Segment
         [sa/Dimmer {:active (and (nil? shift) (= status :loading))} [sa/Loader]]
         (when (= status :succeed)
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

(defn Message [message]
  (let [user-id (<sub [:user-id])
        owner-id (:owner message)
        author (if (= user-id owner-id)
                 "You"
                 (<sub [:application-owner (str (:application message))]))]
    [sa/Segment
     [sa/Grid
      [sa/GridColumn {:width 2} [:p author ":"]]
      [sa/GridColumn {:width 12} [:div {"dangerouslySetInnerHTML"
                                        #js{:__html (text-with-br (:message message))}}]]]]))

(defn Discussion [shift application messages status]
  (if (= status :loading)
    [sa/Loader]
    (concatv [sa/SegmentGroup]
             (map (fn [m] [Message m]) (reverse messages))
             [[MessageForm (:pk application)]])))

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

(def application-statuses
  {1 "New"
   2 "Approved"
   3 "Rejected"
   4 "Postponed"
   5 "Confirmed"
   6 "Cancelled"
   7 "Failed"
   8 "Completed"})

(defn ShowAllApplications [params]
  (rf/dispatch [:get-applications])
  (fn [params]
    (let [applications (<sub [:applications])
          filter-state (<sub [:applications-filter-state])]
      [ResidentLayout
       [sa/Grid {}
        [sa/GridRow {}
         [sa/GridColumn {:width 3}
          (doall (for [[state-key state] application-statuses]
                   [:div {:key state}
                    [sa/Label {:active (= state-key filter-state)
                               :on-click (>event [:set-applications-filter-state state-key])}
                     state
                     [sa/LabelDetail (<sub [:count-applications-by-state state-key])]]
                    [:br]
                    [:br]]))]
         [sa/GridColumn {:width 13}
          [sa/SegmentGroup
           (for [application (:data applications)
                 :let [{{speciality :speciality
                         start :date-start
                         finish :date-end
                         pk :pk
                         payment-amount :payment-amount
                         payment-per-hour :payment-per-hour
                         description :description
                         :as shift} :shift} application]
                 :when (if (nil? filter-state)
                         true
                         (= filter-state (:state application)))]
             [sa/Segment {:key (:pk application)}
              [sa/Grid
               [sa/GridColumn {:width 4}
                [sa/Header (:name (<sub [:speciality speciality]))]
                [:p [:strong "Starts: "] (as-apply-date-time start)]
                [:p [:strong "Ends: "] (as-apply-date-time finish)]
                [:p [:strong "Total: "] (as-hours-interval start finish) " hours"]
                [:p [:strong "Payment amount: "] (str "$" payment-amount " per " (if payment-per-hour "hour" "shift"))]]
               [sa/GridColumn {:width 9} description]
               [sa/GridColumn {:width 3}
                [sa/Button
                 {:on-click (>event [:goto :resident :messages pk :discuss (:pk application)])}
                 "Go to Chat"]]]])]]]]])))

(pages/reg-page :core/resident-messages ShowAllApplications)
(pages/reg-page :core/resident-messages-apply ApplyToShift)
(pages/reg-page :core/resident-messages-discuss DiscussShift)
