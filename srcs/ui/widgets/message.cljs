(ns ui.widgets.message
  (:require
   [ui.db :refer [>event >atom <sub get-url text-with-br]]
   [ui.widgets :refer [concatv]]
   [re-frame.core :as rf]
   [soda-ash.core :as sa]))

(defn MessageForm [application-pk]
  (rf/dispatch-sync [:init-comment-form])
  (let [comment-cursor (<sub [:comment-cursor])]
    (fn []
      (let [{status :status errors :errors} (<sub [:comment-form])]
        [sa/Segment
         [sa/Form {:error (= status :failure)}
          [sa/FormInput {:placeholder "Add comment ..."
                         :error (= status :failure)
                         :value @comment-cursor
                         :on-change (>atom comment-cursor)}]
          (when-not (nil? (:message errors)) [:div.error (str (:message errors))])
          [sa/FormButton {:color :blue
                          :on-click (>event [:add-comment application-pk])} "Send Message"]]]))))

(defn Message [message]
  (let [user-id (<sub [:user-id])
        owner-id (:owner message)
        author (if (= user-id owner-id)
                 "You"
                 (<sub [:application-participant (str (:application message)) owner-id]))]
    [sa/Segment
     [sa/Grid
      [sa/GridColumn {:width 2} [:p author ":"]]
      [sa/GridColumn {:width 12} [:div {"dangerouslySetInnerHTML"
                                        #js{:__html (text-with-br (:message message))}}]]]]))

(defn Discussion [shift application messages status]
  (if (= status :loading)
    [sa/Loader]
    (concatv [sa/SegmentGroup]
             (map (fn [m] [Message m]) (reverse messages))
             [[MessageForm (:pk application)]])))

