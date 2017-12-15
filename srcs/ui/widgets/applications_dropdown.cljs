(ns ui.widgets.applications-dropdown
  (:require
   [ui.db.misc :refer [>event <sub]]
   [ui.db.application :refer [application-statuses get-application-status-name]]
   [soda-ash.core :as sa]))

(defn ApplicationsDropdown [params]
  (fn [params]
    (let [applications (<sub [:applications])
          filter-state (<sub [:applications-filter-state])]
      [sa/Dropdown {:button true
                    :fluid true
                    :floating true
                    :text (get-application-status-name filter-state)
                    :class-name (str "messages__dropdown"
                                     (if-not (nil? filter-state) (str " _" filter-state)))}
       [sa/DropdownMenu
        [:div {:class-name (str "messages__dropdown-item"
                                (if (nil? filter-state) " _active"))
               :on-click (>event [:set-applications-filter-state nil])}
         "All messages" [:span.gray-font " (" (count (:data applications)) ")"]]
        (doall (for [[state-key state] application-statuses]
                 [:div {:key state
                        :class-name (str "messages__dropdown-item"
                                         " _" state-key
                                         (if (= state-key filter-state) " _active"))
                        :on-click (>event [:set-applications-filter-state state-key])}
                  state
                  [:span.gray-font
                   (str " (" (<sub [:count-applications-by-state state-key]) ")")]]))]])))
