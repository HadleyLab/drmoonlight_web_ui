(ns ui.widgets.message
  (:require
   [ui.db.misc :refer [>event >atom <sub get-url text-with-br]]
   [ui.widgets :refer [concatv]]
   [ui.db.misc :refer [>event >atom dispatch-set!]]
   [re-frame.core :as rf]
   [soda-ash.core :as sa]
   [clojure.string :as string]
   [ui.widgets.shift-info :refer [get-short-shift-info]]
   [ui.widgets.error-message :refer [ErrorMessage]]))

(defn MessageForm []
  (rf/dispatch-sync [:init-comment-form])
  (let [comment-cursor (<sub [:comment-cursor])
        attachment-cursor (<sub [:attachment-cursor])]
    (fn [{application-pk :pk
          available-transitions :available-transitions}]
      (let [{status :status errors :errors} (<sub [:comment-form])
            attached-filename (if (nil? @attachment-cursor) nil (.-name @attachment-cursor))]
        [:div.chat__form-container
         [sa/Form {:error (= status :failure)}
          (when (= status :failure)
            [ErrorMessage {:errors errors}])
          [sa/FormInput {:placeholder "Add Comment..."
                         :error (= status :failure)
                         :value @comment-cursor
                         :on-change (>atom comment-cursor)}]
          (if-not (nil? attached-filename) [:div.attached-file-label "Attached file: " attached-filename])
          ; TODO not works
          ; [:div.attached-file-label
          ;   (if (not (nil? attached-filename)) (str "Attached file: " attached-filename) " ")]
          [sa/Input {:type :file
                     :id "id_attachment_input"
                     :style {:display "none"}
                     :value @attachment-cursor
                     :on-change (fn [e]
                                  (-> e
                                      .-target
                                      .-files
                                      (aget 0)
                                      (->> (dispatch-set! attachment-cursor))))}]
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
            "Send message"]
           [sa/Button
            {:basic true
             :color "blue"
             :floated "right"
             :class-name "attachment_button"
             :on-click #(.click (.getElementById js/document "id_attachment_input"))}
            [sa/Icon {:name "file outline" :size :large}]]]]]))))

(defn Message [message]
  (let [user-id (<sub [:user-id])
        owner-id (:owner message)
        date-created (:date-created message)
        avatar (:owner-avatar message)
        attachment (:attachment message)
        thumbnail (:thumbnail message)
        author (if (= user-id owner-id)
                 "You"
                 (<sub [:application-participant (str (:application message)) owner-id]))]
    [sa/Grid {:class-name "chat__message-container"}
     [sa/GridColumn {:width 3}
      [:b author ":"]
      [:img.avatar.avatar-small.chat__message-avatar {:src avatar}]]
     [sa/GridColumn {:width 13} [:div {"dangerouslySetInnerHTML"
                                       #js {:__html (text-with-br (:text message))}}]
      (if-not (nil? attachment) [:a {:href attachment
                                     :target "_blank"}
                                  [:img.attachment-image {
                                    :src (if (nil? thumbnail) "/doc_icon.svg" thumbnail)}]])
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
