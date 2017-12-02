(ns ui.scheduler.schedule.core
  (:require
   [reagent.core :as reagent]
   [ui.db.misc :refer [dispatch-set! <sub >event get-url]]
   [ui.db.scheduler-profile :refer [shift-form-fields]]
   [ui.pages :as pages]
   [ui.routes :refer [href]]
   [ui.widgets :refer [concatv BuildForm]]
   [ui.widgets.error-message :refer [ErrorMessage]]
   [ui.widgets.calendar :refer [Calendar]]
   [ui.scheduler.layout :refer [SchedulerLayout]]
   [re-frame.core :as rf]
   [clojure.string :as str]
   [soda-ash.core :as sa]
   [cljs-time.core :as dt]
   [cljs-time.format :as format]))

(defn ShiftLabel [{speciality :speciality pk :pk}]
  (let [new-shift-form-cursor (<sub [:new-shift-form-cursor])
        {status :status errors :errors} (<sub [:shift-info pk])
        {{detail-error :detail} :errors} (<sub [:shift-form-response])]
    [sa/Modal {:trigger (reagent/as-element
                         [:div
                          [sa/Label {:color :blue
                                     :on-click (>event [:open-edit-shift-modal pk])}
                           (<sub [:speciality-name speciality])] [:br] [:br]])
               :dimmer :blurring
               :open (<sub [:edit-shift-modal pk])
               :on-open (>event [:init-edit-shift-form pk])
               :on-close (>event [:close-edit-shift-modal pk])
               :size :small}
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

(defn CreateNewShift [new-shift-form-cursor]
  (rf/dispatch [:init-new-shift-form])
  (fn [new-shift-form-cursor]
    [sa/Modal {:trigger (reagent/as-element [sa/Button {:color :blue
                                                        :on-click (>event [:open-new-shift-modal])}
                                             [sa/Icon {:name :plus}] "Create new shift"])
               :dimmer :blurring
               :open (<sub [:new-shift-modal])
               :on-close (>event [:close-new-shift-modal])
               :size :small}
     [sa/ModalHeader "Create a new shift"]
     [sa/ModalContent {:image true}
      [sa/ModalDescription
       [sa/Form {}
        [BuildForm new-shift-form-cursor shift-form-fields]]]]
     [sa/ModalActions [sa/Button {:color :blue :on-click (>event [:create-new-shift])} "Done"]]]))

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
           [sa/GridColumn {:width 3}]
           [sa/GridColumn {:width 4}
            [sa/Button {:icon "angle left"
                        :on-click (>event [:set-calendar-month (dt/minus calendar-month (dt/months 1))])}]
            [:span (format/unparse (format/formatter "MMMM YYYY") calendar-month)]
            [sa/Button {:icon "angle right"
                        :on-click (>event [:set-calendar-month (dt/plus calendar-month (dt/months 1))])}]
            [sa/GridColumn {:width 6}]]]
          [sa/GridRow {}
           [sa/GridColumn {:width 3}
            (for [state ["completed" "active" "without_applies" "coverage_completed" "require_approval"]]
              [:div {:key state}
               [sa/Label {:active (= state filter-state)
                          :on-click (>event [:set-shifts-filter-state state])}
                state
                [sa/LabelDetail (get shifts-count-by-state state 0)]]
               [:br]
               [:br]])]
           [sa/GridColumn {:width 13}
            [Calendar calendar-month filtered-shifts ShiftLabel]]]]]))))

(pages/reg-page :core/scheduler-schedule Index)
