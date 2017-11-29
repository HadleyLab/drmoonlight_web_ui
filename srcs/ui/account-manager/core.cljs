(ns ui.account-manager.core
  (:require
   [reagent.core :as reagent]
   [ui.db.misc :refer [>event <sub]]
   [ui.widgets :refer [concatv]]
   [ui.pages :as pages]
   [ui.routes :refer [href]]
   [ui.widgets.resident-profile-detail :refer [ResidentProfileDetail]]
   [re-frame.core :as rf]
   [soda-ash.core :as sa]))

(defn AccountManagerLayout [content]
  (let [route (<sub [:route-map/current-route])]
    [sa/Grid {}
     [sa/GridRow {:class-name "moonlight-header"}
      [sa/GridColumn {:width 1}]
      [sa/GridColumn {:width 12} [sa/Header {} "Dr.Moonlight"]]
      [sa/GridColumn {:width 3} [sa/Button {:on-click (>event [:logout])} "Log out"]]]
     [sa/GridRow {}
      [sa/GridColumn {:width 1}]
      [sa/GridColumn {:width 14} content]
      [sa/GridColumn {:width 1}]]]))

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
                              [sa/GridColumn {:width 3}]
                              [sa/GridColumn {:width 3}
                               ;;TODO use error widget from https://gitlab.bro.engineering/drmoonlight/drmoonlight_web_ui/issues/6#note_6233
                               (when (= status :failure) [:div.error (str errors)])
                               [sa/Button {:color :green :on-click (>event [:approve-resident pk [:goto :account-manager]])} "Approve"]
                               [sa/Button {:color :red :on-click (>event [:reject-resident pk [:goto :account-manager]])} "Reject"]]]]])))

(pages/reg-page :account-manager/index Index)
(pages/reg-page :account-manager/resident-profile-detail WrappedResidentProfileDetail)
