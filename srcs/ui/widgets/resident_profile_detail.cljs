(ns ui.widgets.resident-profile-detail
  (:require
   [clojure.string :as string]
   [ui.db.misc :refer [>event >atom <sub get-url text-with-br]]
   [ui.widgets :refer [concatv]]
   [re-frame.core :as rf]
   [soda-ash.core :as sa]))

(defn ResidentProfileDetail [{pk :resident-pk} actions]
  (rf/dispatch [:load-resident-profile-detail pk])
  (fn [params]
    (let [{status :status resident :data} (<sub [:get-resident-profile-detail pk])
          {first-name :first-name
           last-name :last-name
           residency-program :residency-program
           specialities :specialities
           residency-years :residency-years} resident]
      [sa/Grid {}
       [sa/GridRow {}
        [sa/GridColumn {:width 3}
         [sa/Header (str first-name last-name)]
         [:p (string/join ", " (map #(<sub [:speciality-name %]) specialities))]]
        [sa/GridColumn {:width 13}
         [:div {:class-name "moonlight-form-inner"}
          [:p [:strong "First Name: "] first-name]
          [:p [:strong "Last Name: "] last-name]]]]
       actions])))
