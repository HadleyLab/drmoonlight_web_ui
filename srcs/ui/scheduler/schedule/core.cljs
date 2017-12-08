(ns ui.scheduler.schedule.core
  (:require
   [reagent.core :as reagent]
   [ui.db.misc :refer [<sub >event >events]]
   [ui.db.scheduler-profile :refer [shift-form-fields]]
   [ui.pages :as pages]
   [ui.widgets :refer [BuildForm]]
   [ui.widgets.error-message :refer [ErrorMessage]]
   [ui.widgets.calendar :refer [Calendar CalendarMonthControl]]
   [ui.widgets.shift-info :refer [ShiftInfo]]
   [ui.scheduler.layout :refer [SchedulerLayout]]
   [re-frame.core :as rf]
   [soda-ash.core :as sa]))

(defn EditModal [pk]
  (let [new-shift-form-cursor (<sub [:new-shift-form-cursor])
        {status :status errors :errors} (<sub [:shift-info pk])
        {{detail-error :detail} :errors} (<sub [:shift-form-response])]
    [sa/Modal {:open (<sub [:edit-shift-modal pk])
               :on-close (>event [:close-edit-shift-modal pk])
               :size :small
               :class-name "shift__modal"}
     [sa/ModalHeader "Edit the shift"]
     [sa/ModalContent {:image true}
      [sa/ModalDescription
       (if (= status :failure)
         [ErrorMessage {:errors errors}]
         [sa/Form {}
          [BuildForm new-shift-form-cursor shift-form-fields]
          (when-not (nil? detail-error)
            [ErrorMessage {:errors {"" [detail-error]}}])])]]
     [sa/ModalActions
      (when-not (= status :failure)
        [sa/Button {:color :blue :on-click (>event [:update-shift pk])} "Update"])]]))

(defn ShiftLabel [params]
  (let [pk (:pk params)
        state (:state params)
        speciality (:speciality params)
        speciality-name (:name (<sub [:speciality speciality]))]
    [:div [sa/Popup
           {:trigger (reagent/as-element
                      [:div {:class-name (str "shift__label _" state)}
                       (<sub [:speciality-name speciality])])
            :open (<sub [:edit-shift-popup pk])
            :on-open (>event [:open-edit-shift-popup pk])
            :on-close (>event [:close-edit-shift-popup pk])
            :position "left center"
            :offset 2
            :hoverable true
            :class-name "shift__popup"}
           [ShiftInfo params]
           [sa/Divider]
           [:div.shift__popup-footer._scheduler
            [sa/Button {:basic true
                        :color :blue
                        :on-click (>events [[[:close-edit-shift-popup] pk]
                                            [[:init-edit-shift-form] pk]
                                            [[:open-edit-shift-modal] pk]])} "Edit shift"]
            [:div.shift__remove-shift "Remove shift"]]]
     [EditModal pk]]))

;; TODO: rename to CreateModal to be consistent with EditModal
(defn CreateNewShift [new-shift-form-cursor]
  (rf/dispatch [:init-new-shift-form])
  (fn [new-shift-form-cursor]
    [sa/Modal {:trigger (reagent/as-element [sa/Button {:color :blue
                                                        :fluid true
                                                        :on-click (>event [:open-new-shift-modal])}
                                             [sa/Icon {:name :plus}] "Create new shift"])
               :open (<sub [:new-shift-modal])
               :on-close (>event [:close-new-shift-modal])
               :size :small}
     [sa/ModalHeader "Create a new shift"]
     [sa/ModalContent {:image true}
      [sa/ModalDescription
       [sa/Form {}
        [BuildForm new-shift-form-cursor shift-form-fields]]]]
     [sa/ModalActions [sa/Button {:color :blue :on-click (>event [:create-new-shift])} "Done"]]]))

(def shift-types [{:type "completed" :label "Completed shifts"}
                  {:type "active" :label "Active shifts"}
                  {:type "without_applies" :label "Shifts without applies"}
                  {:type "coverage_completed" :label "Coverage completed"}
                  {:type "require_approval" :label "Require approval"}])

(defn Index [params]
  (rf/dispatch [:load-shifts])
  (let [new-shift-form-cursor (<sub [:new-shift-form-cursor])]
    (fn [params]
      (let [calendar-month (<sub [:calendar-month])
            shifts-count-by-state (<sub [:shifts-count-by-state])
            filter-state (<sub [:shifts-filter-state])
            filtered-shifts (<sub [:shifts-filtered-by-state filter-state])]
        [SchedulerLayout
         [sa/Grid {}
          [sa/GridRow {}
           [sa/GridColumn {:width 3} [CreateNewShift new-shift-form-cursor]]
           [CalendarMonthControl [sa/GridColumn {:width 13}]]]
          [sa/GridRow {}
           [sa/GridColumn {:width 3}
            (for [state shift-types]
              (let [type (:type state)
                    label (:label state)]
                [:div {:key type :class-name "schedule__shifts-menu-item"}
                 [:label {:class-name "schedule__shifts-menu-item-label"
                          :on-click (>event [:set-shifts-filter-state type])}
                  [:span {:class-name (str "schedule__shifts-menu-item-color _" type)}]
                  label " (" (get shifts-count-by-state type 0) ")"]
                 [sa/Checkbox {:checked (= type filter-state)
                               :class-name "schedule__shifts-menu-item-checkbox"
                               :on-click (>event [:set-shifts-filter-state type])}]]))]
           [sa/GridColumn {:width 13}
            [Calendar calendar-month filtered-shifts ShiftLabel]]]]]))))

(pages/reg-page :core/scheduler-schedule Index)
