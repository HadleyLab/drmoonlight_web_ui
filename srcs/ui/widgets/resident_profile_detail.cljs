(ns ui.widgets.resident-profile-detail
  (:require
   [clojure.string :as string]
   [ui.db.misc :refer [>event >atom <sub get-url text-with-br]]
   [ui.db.resident-profile :refer [resident-profile-form-fields]]
   [ui.widgets :refer [concatv]]
   [re-frame.core :as rf]
   [soda-ash.core :as sa]))

(defn DetailRow [data field info]
  (fn [data field info]
    [sa/Grid {:class-name "profile-detail__grid"}
     [sa/GridRow {:class-name "align-items _center"}
      [sa/GridColumn {:width 6}
       (:label info)
       (if (:desc info) [:span.gray-font " (" (:desc info) ")"])]
      [sa/GridColumn {:width 10}
       (case (:type info)
         :radio (map (fn [[key value]]
                       [sa/FormRadio {:disabled true
                                      :label (str value)
                                      :checked (= key (:field data))
                                      :class-name "profile-detail__radio"}])
                     (:items info))
         :multy-select (map
                        (fn [item]
                          [:span.profile-detail__select (str item)])
                        (field data))
         [:b (field data)])]]]))

(defn Details [data field-sets]
  (concatv [:div]
           (->> field-sets
                (mapv (fn [[title fields]]
                        (concatv [:div.form__group {:key title}
                                  [:label {:class-name "form__group-title"} title]]
                                 (->> fields
                                      (mapv (fn [[field info]]
                                              [DetailRow data field info])))))))))

(defn ResidentProfileDetail [{pk :resident-pk} actions]
  (rf/dispatch [:load-resident-profile-detail pk])
  (fn [params]
    (let [{status :status resident :data} (<sub [:get-resident-profile-detail pk])
          residency-program (<sub [:residency-program-name (:residency-program resident)])
          specialities (map #(<sub [:speciality-name %]) (:specialities resident))
          {first-name :first-name
           last-name :last-name
           date-joined :date-joined
           email :email
           residency-years :residency-years} resident]
      [sa/Grid {}
       [sa/GridRow {}
        [sa/GridColumn {:width 4}
         [:div.profile__menu-wrapper
          [sa/Header (str first-name last-name)]
          [:p (string/join ", " specialities)]
          [:p.gray-font "signed up " (.format (js/moment date-joined) "DD/MM/YYYY")]]]
        [sa/GridColumn {:width 12}
         [:div {:class-name "profile-detail__container moonlight-form-inner moonlight-white"}
          [Details (merge
                    resident
                    {:residency-program residency-program
                     :specialities specialities})
           resident-profile-form-fields]
          [:div.form__group._last
           [:label.form__group-title "Account settings"]
           [sa/Grid {:class-name "profile-detail__grid"}
            [sa/GridRow {:class-name "align-items _center"}
             [sa/GridColumn {:width 6} "Email"]
             [sa/GridColumn {:width 10}
              [:b email]]]]]]]]
       actions])))
