(ns ui.db.misc
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [clojure.string :refer [replace escape]]))

(defn reduce-statuses [& statuses]
  (cond
    (some #(= :loading %) statuses) :loading
    (some #(= :failure %) statuses) :failure
    (every? #(= :succeed %) statuses) :succeed
    (some #(= :not-asked %) statuses) :not-asked))

(defn reduce-errors [& errors]
  (first (remove nil? errors)))

(defn text-with-br [text]
  (replace (escape
            text
            {\< "&lt;"
             \> "&gt;"
             \& "&amp;"})
           #"\n"
           "<br/>"))

(defn get-url [db & rest]
  (apply str (cons (db :base-url) rest)))

(def <sub (comp deref re-frame.core/subscribe))
(def >evt re-frame.core/dispatch)

(defn insert-by-path [m [k & ks :as path] value]
  (if ks
    (if (int? k)
      (assoc (or m []) k (insert-by-path (get m k) ks value))
      (assoc (or m {}) k (insert-by-path (get m k) ks value)))
    (if (int? k)
      (assoc (or m []) k value)
      (assoc (or m {}) k value))))

(defn cursor->path [cursor]
  (cond
    (= reagent.ratom/RAtom (type cursor)) []
    (= reagent.ratom/RCursor (type cursor)) (let [path (.-path cursor)
                                                  ratom (.-ratom cursor)]
                                              (into [] (concat (cursor->path ratom) path)))))

(defn- negligible?
  [x]
  (if (seqable? x) (empty? x) (not x)))

(defn >event
  "Return a function suitable for an on-* handler, to deliver the value
  into into a re-frame event. See also >atom.
  The first argument is a re-frame event vector, into which the value
  will be conjed as the final element.
  It is followed by two optional arguments: a default value that will
  be used when the value is empty, and a 'coercer' function to convert
  the value into a suitable form for the event.
  Note that the default value is _not_ passed through the coercer."
  ;; [TODO] The line about the default value is obviously wrong. Check users and fix!
  ([event]
   (>event event ""))
  ([event default]
   (>event event default identity))
  ([event default coercer]
   #(rf/dispatch (let [value (if (nil? %2) nil (or (.-value %2) (.-checked %2)))]
                   (conj event
                         (coercer (if (negligible? value)
                                    default
                                    value)))))))

(defn >events
  "Utility function to dispatch multiple events from an on-* hander.
  The syntax is a bit opaque, because we have to wrap both the event
  parameters and the parameters to >event (default and coercer).

  So, a usage looks like:

    (na/>events [[[:update-age :in-minutes] 42]
                 [[:set-color] :cyan nearest-color]])
  "
  [events]
  (fn [dom-event param-map]
    (run! (fn [event]
            ((apply >event event) dom-event param-map))
          events)))

(defn dispatch-set! [atom value]
  (rf/dispatch [:db/write (cursor->path atom) value]))

(defn >atom
  "Return a function suitable for an on-* handler, to deliver the value
  into into an atom. This would typically be used to store a result into
  a local reagent atom. See also >event.
  The first argument is an atom, which the value will be reset! into.
  It is followed by two optional arguments: a default value that will
  be used when the value is empty, and a 'coercer' function to convert
  the value into a suitable form for the event.
  Note that the default value is _not_ passed through the coercer."
  ;; [TODO] Default wrong here too
  ([atom]
   (>atom atom ""))
  ([atom default]
   (>atom atom default identity))
  ([atom default coercer]
   #(->> (or (.-value %2) (.-checked %2))
         js->clj
         coercer
         (dispatch-set! atom))))

(defn fields->schema [fields & [defaults]]
  (into {} (map (fn [[key _]] [key (get defaults key "")]) (mapcat second fields))))

(defn setup-form-initial-values [initial-values]
  (fn [data]
    (into {} (map (fn [[key value]] [key (initial-values key value)]) data))))

(defn format-date-time [date]
  (let [formatted-date (.format date "MM/DD/YYYY")
        hour (.format date "h")
        minute (.format date "mm")
        am-pm (.format date "a")]
    (str formatted-date " at " hour ":" minute " " am-pm)))

(defn get-timezone-str []
  (.guess (.-tz js/moment)))
