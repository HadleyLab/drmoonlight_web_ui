(ns ui.scheduler.schedule.core
  (:require
   [reagent.core :as reagent]
   [ui.db.misc :refer [<sub >event >events]]
   [ui.db.shift :refer [shift-form-fields]]
   [ui.db.scheduler :refer [shift-types]]
   [ui.db.shift :refer [get-shift-type]]
   [ui.pages :as pages]
   [ui.widgets :refer [BuildForm]]
   [ui.widgets.error-message :refer [ErrorMessage]]
   [ui.widgets.calendar :refer [Calendar CalendarMonthControl]]
   [ui.widgets.shift-info :refer [ShiftInfo ShiftsFilter]]
   [ui.scheduler.layout :refer [SchedulerLayout]]
   [re-frame.core :as rf]
   [soda-ash.core :as sa]))

(def field-names-map {:date-end "Ends"
                      :date-start "Starts"
                      :payment-amount "Payment amount"
                      :payment-per-hour "Payment hourly/pre shift missed value"
                      :detail ""})

(defn EditModal []
  (let [new-shift-form-cursor (<sub [:new-shift-form-cursor])
        editing-shift-pk (<sub [:editing-shift-pk])
        hide-field-errors true
        {status :status errors :errors} (<sub [:shift-info editing-shift-pk])
        {submit-errors :errors} (<sub [:shift-form-response])]
    [sa/Modal {:open true
               :on-close (>event [:close-edit-shift-modal])
               :size :small
               :class-name "shift__modal"}
     [sa/ModalHeader "Edit the shift"]
     [sa/ModalContent
      [sa/ModalDescription
       (if (= status :failure)
         [ErrorMessage {:errors errors}]
         [sa/Form {}
          [BuildForm new-shift-form-cursor shift-form-fields hide-field-errors]
          (when-not (nil? submit-errors)
            [ErrorMessage {:errors submit-errors :field-names-map field-names-map}])])]]
     [sa/ModalActions
      (when-not (= status :failure)
        [sa/Button {:color :blue :on-click (>event [:update-shift editing-shift-pk])} "Update"])]]))

(defn ShiftLabel [params draw-data]
  (let [hovered (reagent/atom false)]
    (fn [params draw-data]
      (let [pk (:pk params)
            state (:state params)
            type (get-shift-type params shift-types)
            speciality (:speciality params)
            speciality-name (:name (<sub [:speciality speciality]))
            {:keys [starts-on-prev-week ends-on-next-week]} draw-data
            opened (<sub [:edit-shift-popup pk])]
        [:div [sa/Popup
               {:trigger (reagent/as-element
                          [:div {:class-name (str
                                              "shift__label _" type
                                              (when starts-on-prev-week " _starts-on-prev-week")
                                              (when ends-on-next-week " _ends-on-next-week")
                                              (when opened " _hovered"))}
                           (<sub [:speciality-name speciality])])
                :open @hovered
                :on-open (fn [] (reset! hovered true)
                           (rf/dispatch [:open-edit-shift-popup pk]))
                :on-close (fn [] (reset! hovered false)
                            (rf/dispatch [:close-edit-shift-popup pk]))
                :position "left center"
                :offset 2
                :hoverable true
                :class-name "shift__popup"}
               [ShiftInfo params]
               [sa/Divider]
               [:div.shift__popup-footer._scheduler
                [sa/Button {:basic true
                            :color :blue
                            :on-click (fn [] (reset! hovered false)
                                        (rf/dispatch [:close-edit-shift-popup pk])
                                        (rf/dispatch [:init-edit-shift-form pk])
                                        (rf/dispatch [:open-edit-shift-modal pk]))} "Edit shift"]
                (if-not (contains? #{"completed" "active" "failed"} state)
                  [:div.shift__remove-shift {:on-click (fn [] (reset! hovered false)
                                                         (rf/dispatch [:delete-shift pk]))} "Remove shift"])]]]))))

(defn CreateModal [new-shift-form-cursor]
  (fn [new-shift-form-cursor]
    (let [hide-field-errors true
          {status :status errors :errors} (<sub [:shift-form-response])]
      [sa/Modal {:trigger (reagent/as-element [sa/Button {:color :blue
                                                          :fluid true
                                                          :on-click (>events [[[:init-new-shift-form]]
                                                                              [[:open-new-shift-modal]]])}
                                               [sa/Icon {:name :plus}] "Create new shift"])
                 :open (<sub [:new-shift-modal])
                 :on-close (>event [:close-new-shift-modal])
                 :size :small
                 :class-name "shift__modal"}
       [sa/ModalHeader "Create a new shift"]
       [sa/ModalContent
        [sa/Form {}
         [BuildForm new-shift-form-cursor shift-form-fields hide-field-errors]
         (when (= status :failure)
           [ErrorMessage {:errors errors :field-names-map field-names-map}])]]
       [sa/ModalActions [sa/Button {:color :blue :on-click (>event [:create-new-shift])} "Done"]]])))

(defn Index [params]
  (rf/dispatch [:load-shifts])
  (let [new-shift-form-cursor (<sub [:new-shift-form-cursor])]
    (fn [params]
      (let [calendar-month (<sub [:calendar-month])
            editing-shift-pk (<sub [:editing-shift-pk])]
        [SchedulerLayout
         [sa/Grid {}
          [sa/GridRow {}
           [sa/GridColumn {:width 3} [CreateModal new-shift-form-cursor]]
           [CalendarMonthControl [sa/GridColumn {:width 13}]]]
          [sa/GridRow {}
           [sa/GridColumn {:width 3}
            [ShiftsFilter shift-types]]
           [sa/GridColumn {:width 13}
            (when-not (nil? editing-shift-pk) [EditModal])
            [Calendar calendar-month ShiftLabel]]]]]))))

(pages/reg-page :core/scheduler-schedule Index)
