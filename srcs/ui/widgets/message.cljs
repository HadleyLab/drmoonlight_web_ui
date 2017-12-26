(ns ui.widgets.message
  (:require
   [ui.db.misc :refer [>event >atom <sub get-url text-with-br]]
   [ui.widgets :refer [concatv]]
   [re-frame.core :as rf]
   [soda-ash.core :as sa]
   [ui.widgets.shift-info :refer [ShortShiftInfo]]))

(defn MessageForm []
  (rf/dispatch-sync [:init-comment-form])
  (let [comment-cursor (<sub [:comment-cursor])]
    (fn [{application-pk :pk
          available-transitions :available-transitions}]
      (let [{status :status errors :errors} (<sub [:comment-form])]
        [:div.chat__form-container
         [sa/Form {:error (= status :failure)}
          [sa/FormInput {:placeholder "Add Comment..."
                         :error (= status :failure)
                         :value @comment-cursor
                         :on-change (>atom comment-cursor)}]
          (when-not (nil? (:text errors)) [:div.error (str (clojure.string/join "," (:text errors)))])
          [sa/ButtonGroup
           (for [transition (conj available-transitions "Send message")]
             [sa/FormButton
              {:color :blue
               :key transition
               :on-click (>event [:add-comment application-pk] transition)}
              transition])]]]))))

(defn Message [message]
  (let [user-id (<sub [:user-id])
        owner-id (:owner message)
        date-created (:date-created message)
        author (if (= user-id owner-id)
                 "You"
                 (<sub [:application-participant (str (:application message)) owner-id]))]
    [sa/Grid {:class-name "chat__message-container"}
     (.log js/console "message" message)
     [sa/GridColumn {:width 3} [:b author ":"]]
     [sa/GridColumn {:width 13} [:div {"dangerouslySetInnerHTML"
                                       #js {:__html (text-with-br (:text message))}}]
      [:p.chat__message-time (.format (js/moment date-created) "h:mm a")]]]))

(defn Discussion [shift application messages status]
  (if (= status :loading)
    [sa/Loader]
    [:div
     [:div.chat__messages-list
      [sa/Header {:textAlign "center" :class-name "chat__messages-header"}
       [ShortShiftInfo shift]]
      (map (fn [m] ^{:key (:pk m)} [Message m]) (reverse messages))]
     [MessageForm application]]))
