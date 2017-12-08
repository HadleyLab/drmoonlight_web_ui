(ns ui.widgets.error-message
  (:require
   [soda-ash.core :as sa]
   [clojure.string :as str]))

(defn get-message [[error-key error-value] names-map]
  (let [field-name (names-map error-key (str/capitalize (name error-key)))
        error-text (if (vector? error-value) (str/join ", " error-value) error-value)]
    (if (= field-name "")
      error-text
      (str field-name ": " error-text))))

(defn ErrorMessage [{:keys [errors field-names-map]}]
  (let [field-names-map (or field-names-map {})]
    [sa/Message {:negative true
                 :header "There was some errors with your submission"
                 :list (map #(get-message % field-names-map) errors)}]))
