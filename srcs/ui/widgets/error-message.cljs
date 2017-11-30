(ns ui.widgets.error-message
  (:require
   [soda-ash.core :as sa]
   [clojure.string :as str]))

(defn get-message [error names-map names-list]
  (let [field (find names-map (key error))
        field-name (or (if field (val field)) (str/capitalize (name (key error))))
        error-text (first (val error))]
    (if (and field-name (not= field-name ""))
      (str field-name ":" " " error-text)
      error-text)))

(defn ErrorMessage [{:keys [visible errors field-names-map]}]
  (let [field-names-list (keys errors)]
    [sa/Message {:error true
                 :visible visible
                 :header "There was some errors with your submission"
                 :list (map (fn [error] (get-message error field-names-map field-names-list)) errors)}]))
