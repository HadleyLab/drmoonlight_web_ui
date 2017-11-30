(ns ui.widgets.message
  (:require
   [ui.db.misc :refer [>event >atom <sub get-url text-with-br]]
   [ui.widgets :refer [concatv]]
   [re-frame.core :as rf]
   [soda-ash.core :as sa]))

(defn MessageForm [{application-pk :pk}]
  ;; available-transitions :available-transitions}]
  ;; TODO this is not working available transitions is not changing.
  ;; So I have to use subscription on line 16.
  ;; I need to know more how reagent render works and why this componet is not rerendering
  (rf/dispatch-sync [:init-comment-form])
  (let [comment-cursor (<sub [:comment-cursor])]
    (fn []
      (let [{status :status errors :errors} (<sub [:comment-form])
            available-transitions (<sub [:available-transitions (str application-pk)])]
        [sa/Segment
         [sa/Form {:error (= status :failure)}
          [sa/FormInput {:placeholder "Add comment ..."
                         :error (= status :failure)
                         :value @comment-cursor
                         :on-change (>atom comment-cursor)}]
          (when-not (nil? (:text errors)) [:div.error (str (clojure.string/join "," (:text errors) ))])
          [sa/ButtonGroup
           (for [transition (conj available-transitions "message")]
             [sa/FormButton
              {:color :blue
               :key transition
               :on-click (>event [:add-comment application-pk] transition)}
              transition])]]]))))

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
                                        #js{:__html (text-with-br (:text message))}}]]]]))

(defn Discussion [shift application messages status]
  (if (= status :loading)
    [sa/Loader]
    (concatv [sa/SegmentGroup]
             (map (fn [m] [Message m]) (reverse messages))
             [[MessageForm application]])))

