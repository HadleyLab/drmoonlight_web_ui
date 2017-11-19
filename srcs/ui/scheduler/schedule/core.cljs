(ns ui.scheduler.schedule.core
  (:require
   [reagent.core :as reagent]
   [ui.pages :as pages]
   [ui.routes :refer [href]]
   [ui.widgets :refer [concatv build-form fields->schema]]
   [ui.widgets.calendar :refer [calendar]]
   [ui.scheduler.layout :refer [scheduler-layout]]
   [re-frame.core :as rf]
   [clojure.string :as str]
   [soda-ash.core :as sa]
   [sodium.core :as na]
   [cljs-time.core :as dt]
   [cljs-time.format :as format]))

(def root-path :scheduler-schedule-page)

(def shift-form-fields
  {"" {:dateStart "Starts"
       :dateend "Ends"}
   "Requid staff" {:speciality "Speciality name"
                   :paymentAmount "Payment amount, $"
                   :description {:type :textarea :label "Description"}
                   :residencyProgram "Residency Program"
                   :residencyYearsRequired "Residency yearsrequired"}})

(defn schema []
  {:selected (dt/date-time (dt/year (dt/now)) (dt/month (dt/now)) 1)
   :shift-form
   {:fields (fields->schema shift-form-fields)
    :response {:status :not-asked}}
   :shifts {:status :not-asked}})

(defn shift-label [{title :title start :start finish :finish total :total pk :pk}]
  [:div
   [sa/Label {:color :blue} title] [:br] [:br]])

(defn create-new-shift [new-shift-form-cursor]
  [sa/Modal {:trigger (reagent/as-element [sa/Button {:color :blue} [sa/Icon {:name :plus}] "Create new shift"])
             :dimmer :blurring
             :size :small}
   [sa/ModalHeader "Create a new shift"]
   [sa/ModalContent {:image true}
    [sa/ModalDescription
     [na/form {:class-name "moonlight-inner-form"}
      [build-form new-shift-form-cursor shift-form-fields]]]]
   [sa/ModalActions [na/button {:content "Done" :color :blue}]]])

(defn index [params]
  (rf/dispatch-sync [::init-scheduler-shedule-page])
  (let [selected-cursor @(rf/subscribe [:cursor [root-path :selected]])
        shifts-cursor @(rf/subscribe [:cursor [root-path :shifts]])
        new-shift-form-cursor @(rf/subscribe [:cursor [root-path :shift-form]])]
    (fn [params]
      [scheduler-layout
       [na/grid {}
        [na/grid-row {}
         [na/grid-column {:width 3} [create-new-shift new-shift-form-cursor]]
         [na/grid-column {:width 3}]
         [na/grid-column {:width 4}
          [na/button {:icon "angle left"
                      :on-click #(reset! selected-cursor (dt/minus @selected-cursor (dt/months 1)))}]
          [:span (format/unparse (format/formatter "MMMM YYYY") @selected-cursor)]
          [na/button {:icon "angle right"
                      :on-click #(reset! selected-cursor (dt/plus @selected-cursor (dt/months 1)))}]
          [na/grid-column {:width 6}]]]
        [na/grid-row {}
         [na/grid-column {:width 3}]
         [na/grid-column {:width 13}
          [calendar @selected-cursor @shifts-cursor shift-label]]]]])))

(rf/reg-event-fx
 ::init-scheduler-shedule-page
 (fn [{db :db} [_]]
   {:db (if (= (root-path db) nil)
          (assoc-in db [root-path] (schema))
          db)
    :dispatch [::load-shifts]}))

(rf/reg-event-db
 ::load-shifts
 (fn [db [_]]
   (assoc-in db [root-path :shifts]
             {:status :succeed
              :data {"2017-11-30" [{:pk 1 :date (dt/date-time 2017 11 30) :title "doctor" :start "8pm" :finish "11pm" :total "6 hours"}
                                   {:pk 2 :date (dt/date-time 2017 11 30) :title "doctor" :start "8pm" :finish "11pm" :total "6 hours"}
                                   {:pk 3 :date (dt/date-time 2017 11 30) :title "doctor" :start "8pm" :finish "11pm" :total "6 hours"}]
                     "2017-12-01" [{:pk 4 :date (dt/date-time 2017 12 1) :title "doctor" :start "8pm" :finish "11pm" :total "6 hours"}]
                     "2017-12-02" [{:pk 5 :date (dt/date-time 2017 12 1) :title "doctor" :start "8pm" :finish "11pm" :total "6 hours"}]
                     "2017-12-03" [{:pk 6 :date (dt/date-time 2017 12 1) :title "doctor" :start "8pm" :finish "11pm" :total "6 hours"}]
                     "2017-12-04" [{:pk 7 :date (dt/date-time 2017 12 1) :title "doctor" :start "8pm" :finish "11pm" :total "6 hours"}]
                     "2017-12-05" [{:pk 8 :date (dt/date-time 2017 12 1) :title "doctor" :start "8pm" :finish "11pm" :total "6 hours"}]
                     "2017-12-06" [{:pk 9 :date (dt/date-time 2017 12 6) :title "doctor" :start "8pm" :finish "11pm" :total "6 hours"}
                                   {:pk 10 :date (dt/date-time 2017 12 6) :title "doctor" :start "8pm" :finish "11pm" :total "6 hours"}
                                   {:pk 11 :date (dt/date-time 2017 12 6) :title "doctor" :start "8pm" :finish "11pm" :total "6 hours"}]}})))

(pages/reg-page :core/scheduler-schedule index)
