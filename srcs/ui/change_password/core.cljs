(ns ui.change-password.core
  (:require
   [ui.db.misc :refer [>event <sub]]
   [ui.db.account :refer [change-password-form-fields]]
   [ui.widgets :refer [BuildForm]]
   [re-frame.core :as rf]
   [soda-ash.core :as sa]))

(defn ChangePasswordForm []
  (rf/dispatch [:init-change-password-form])
  (let [cursor (<sub [:change-password-form-cursor])]
    (fn []
      (let [fields (:fields @cursor)
            new-password (:new-password fields)
            new-password-confirm (:new-password-confirm fields)
            {status :status errors :errors} (<sub [:change-password-form-response])]
        [:div.profile__content
         [sa/Form {:class-name "moonlight-form-inner"}
          [BuildForm cursor change-password-form-fields]
          [:div.form__group
           (if (= status :succeed)
             [sa/Message {:color "green" :compact true :header "Your password has been successfully changed!"}])
           [sa/FormButton {:color :blue
                           :disabled (or
                                      (= new-password "")
                                      (nil? new-password)
                                      (not= new-password new-password-confirm))
                           :loading (= status :loading)
                           :class-name "profile__button"
                           :on-click (>event [:update-user-password])} "Save changes"]]]]))))
