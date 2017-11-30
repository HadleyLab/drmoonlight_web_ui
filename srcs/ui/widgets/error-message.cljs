(ns ui.widgets.error-message
  (:require
   [soda-ash.core :as sa]
   [clojure.string :as str]))

(defn getMessage [error names]
  (let [field-name (find names (key error))
        error-text (first (val error))]
    (if (and field-name (not= (val field-name) ""))
      (str (val field-name) ":" " " error-text)
      error-text)))

(defn ErrorMessage [params]
  (let [visible (:visible params)
        errors (:errors params)
        field-name-map (:field-name-map params)]
    [sa/Message {:error true
                 :visible visible
                 :header "There was some errors with your submission"
                 :list (map (fn [error] (getMessage error field-name-map)) errors)}]))
