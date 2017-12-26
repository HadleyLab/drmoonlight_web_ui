(ns ui.account-manager.core
  (:require
   [reagent.core :as reagent]
   [ui.db.misc :refer [>event <sub]]
   [ui.widgets :refer [concatv]]
   [ui.pages :as pages]
   [ui.routes :refer [href]]
   [ui.widgets.resident-profile-detail :refer [ResidentProfileDetail]]
   [re-frame.core :as rf]
   [soda-ash.core :as sa]
   [ui.widgets.header-logo :refer [HeaderLogo]]
   [ui.widgets.error-message :refer [ErrorMessage]]))

(defn AccountManagerHeader []
  (let [route (<sub [:route-map/current-route])]
    [sa/GridRow {:class-name "header__wrapper"}
     [sa/Container {:class-name "header__content"}
      [HeaderLogo]
      [sa/ButtonGroup {}
       [sa/Button {:active (= (:match route) :account-manager/index)
                   :on-click (>event [:goto :account-manager])
                   :class-name "header__menu-item"}
        [sa/Icon {:name "list layout" :size :large}]
        "Dashboard"]
       [sa/Button {:active (= (:match route) :core/statistics)
                   :on-click (>event [:goto :statistics])
                   :class-name "header__menu-item"}
        [sa/Icon {:name "line graph" :size :large}]
        "Statistics"]
       [sa/Button {:on-click (>event [:logout])
                   :class-name "header__menu-item"}
        [sa/Icon {:name "log out" :size :large}]
        "Log out"]]]]))

(defn AccountManagerLayout [content]
  (let [route (<sub [:route-map/current-route])]
    [sa/Grid {}
     [AccountManagerHeader]
     [sa/GridRow {}
      [sa/Container content]]]))

(defn Index [params]
  (rf/dispatch [:load-residents-which-waiting-for-approve])
  (fn []
    (let [residents-which-waiting-for-approve (<sub [:get-residents-which-waiting-for-approve])]
      [AccountManagerLayout
       [sa/Grid {}
        [sa/GridRow {}
         [sa/GridColumn {:width 3}]
         [sa/GridColumn {:width 13}
          (if (not= (count (:data residents-which-waiting-for-approve)) 0)
            [sa/SegmentGroup
             (doall (for [resident (:data residents-which-waiting-for-approve)
                          :let [{pk :id
                                 first-name :first-name
                                 last-name :last-name
                                 residency-program :residency-program
                                 specialities :specialities
                                 residency-years :residency-years} resident]]
                      [sa/Segment {:key pk}
                       [sa/Grid
                        [sa/GridColumn {:width 4}
                         [sa/Header (str  first-name " " last-name)]
                         [:p [:strong "Residency years: "] residency-years]]
                        (concatv [sa/GridColumn {:width 8}]
                                 [(concatv [:p [:strong "Specialities: "]]
                                           (mapv (fn [id] [sa/Label
                                                           {:key id}
                                                           (<sub [:speciality-name id])]) specialities))]
                                 [[:p [:strong "Residency program: "]
                                   [sa/Label (<sub [:residency-program-name residency-program])]]])
                        [sa/GridColumn {:width 4}
                         [sa/Button
                          {:on-click (>event [:goto :account-manager :detail pk])}
                          "View full profile"]]]]))]
            [:strong "There is no resident pending approval"])]]]])))

(defn WrappedResidentProfileDetail [{pk :resident-pk :as params}]
  (rf/dispatch [:init-resident-approve-rejects-form])
  (fn []
    (let [{status :status errors :errors} (<sub [:get-resident-approve-reject-result])]
      [AccountManagerLayout [ResidentProfileDetail
                             params
                             [sa/GridRow
                              [sa/GridColumn {:width 4}]
                              [sa/GridColumn {:width 12 :class-name "account-manager__resident-buttons"}
                               (when (= status :failure)
                                 [ErrorMessage {:errors errors}])
                               [sa/Button {:color :green :on-click (>event [:approve-resident pk [:goto :account-manager]])} "Approve"]
                               [sa/Button {:color :red :on-click (>event [:reject-resident pk [:goto :account-manager]])} "Reject"]]]]])))

(pages/reg-page :account-manager/index Index)
(pages/reg-page :account-manager/resident-profile-detail WrappedResidentProfileDetail)
