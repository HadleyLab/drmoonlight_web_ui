(ns ui.widgets.error-message
  (:require
   [soda-ash.core :as sa]
   [clojure.string :as str]))

(defn getMessage [error names]
  (let [display-field-name (some (fn [name] (= name (key error))) names)
        field-name (str/capitalize (name (key error)))
        error-text (first (val error))]
    (if display-field-name
      (str field-name ":" " " error-text)
      error-text)))

(defn ErrorMessage [params]
  (let [visible (:visible params)
        errors (:errors params)
        field-names-to-display (:field-names-to-display params)]
    [sa/Message {:error true
                 :visible visible
                 :header "There was some errors with your submission"
                 :list (map (fn [error] (getMessage error field-names-to-display)) errors)}]))
