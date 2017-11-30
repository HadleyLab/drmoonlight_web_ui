(ns ui.widgets.error-message
  (:require
   [soda-ash.core :as sa]
   [clojure.string :as str]))

(defn get-message [[error-key error-value] names-map]
  (let [field-name (names-map error-key (str/capitalize (name error-key)))
        error-text (str/join ", " error-value)]
    (if (= field-name "")
      error-text
      (str field-name ": " error-text))))

(defn ErrorMessage [{:keys [errors field-names-map]}]
  [sa/Message {:error true
               :header "There was some errors with your submission"
               :list (map #(get-message % field-names-map) errors)}])
