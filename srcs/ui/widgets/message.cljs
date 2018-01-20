(ns ui.widgets.message
  (:require
   [ui.db.misc :refer [>event >atom <sub get-url text-with-br]]
   [ui.widgets :refer [concatv]]
   [re-frame.core :as rf]
   [soda-ash.core :as sa]
   [clojure.string :as string]
   [ui.widgets.shift-info :refer [get-short-shift-info]]
   [ui.widgets.error-message :refer [ErrorMessage]]))

(defn MessageForm []
  (rf/dispatch-sync [:init-comment-form])
  (let [comment-cursor (<sub [:comment-cursor])]
    (fn [{application-pk :pk
          available-transitions :available-transitions}]
      (let [{status :status errors :errors} (<sub [:comment-form])]
        [:div.chat__form-container
         [sa/Form {:error (= status :failure)}
          (when (= status :failure)
            [ErrorMessage {:errors errors}])
          [sa/FormInput {:placeholder "Add Comment..."
                         :error (= status :failure)
                         :value @comment-cursor
                         :on-change (>atom comment-cursor)}]
          [:div.chat__buttons
           (for [transition available-transitions]
             [sa/Button
              {:color (if (= transition "reject") "red" "green")
               :basic true
               :key transition
               :class-name "chat__button"
               :on-click (>event [:add-comment application-pk] transition)}
              (string/capitalize transition)])
           [sa/Button
            {:color :blue
             :floated "right"
             :on-click (>event [:add-comment application-pk] "message")}
            "Send message"]]]]))))

(defn Message [message]
  (let [user-id (<sub [:user-id])
        owner-id (:owner message)
        date-created (:date-created message)
        author (if (= user-id owner-id)
                 "You"
                 (<sub [:application-participant (str (:application message)) owner-id]))]
    [sa/Grid {:class-name "chat__message-container"}
     [sa/GridColumn {:width 3} [:b author ":"]]
     [sa/GridColumn {:width 13} [:div {"dangerouslySetInnerHTML"
                                       #js {:__html (text-with-br (:text message))}}]
      [:p.chat__message-time (.format (js/moment date-created) "h:mm a")]]]))


(defn group-messages [messages]
  (group-by (fn [{date-created :date-created}]
              (.format (js/moment date-created) "YYYY-MM-DD"))
            messages))

(defn Discussion [shift application messages status]
  (if (= status :loading)
    [sa/Loader]
    [:div
     [:div.chat__messages-list
      [sa/Header {:textAlign "center" :class-name "chat__messages-header"}
       (get-short-shift-info shift)]
      [:div
       (map (fn [[key groped-messages]]
              ^{:key key}
              [:div.chat__messages-group
                           [:div.chat__messages-date
                            (.format (js/moment key) "D MMM")]
                           (map (fn [m] ^{:key (:pk m)} [Message m]) groped-messages)])
            (group-messages (reverse messages)))]]
     [MessageForm application]]))
