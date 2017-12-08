(ns ui.db.scheduler-profile
  (:require
   [re-frame.core :as rf]))

(def schema
  {::scheduler-profile
   {:profile-form {}}})

(rf/reg-sub
 ::scheduler-profile
 (fn [db path]
   (get-in db path)))
