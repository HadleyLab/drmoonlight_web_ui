(ns ui.db.misc)

(defn get-url [db & rest]
  (apply str (cons (db :base-url) rest)))
