(ns ui.db
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [akiroz.re-frame.storage :refer [reg-co-fx!]]
            [clojure.string :as str]))

(reg-co-fx! :de-moonlight
            {:fx :store
             :cofx :store})

(def <sub (comp deref re-frame.core/subscribe))
(def >evt re-frame.core/dispatch)

(rf/reg-sub-raw :route (fn [db _] (reaction (:route @db))))
(rf/reg-sub-raw :db (fn [db] db))
(rf/reg-sub-raw :cursor (fn [db [k path]] (reaction (reagent/cursor db path))))

(rf/reg-sub-raw :db/by-path
                (fn [db [_ path]]
                  (.log js/console "by-path" path)
                  (reaction (get-in @db path))))

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
(rf/reg-event-db
 :db/write
 (fn [db [_ path val]]
   (insert-by-path db path val)))

(defn get-url [db & rest]
  (apply str (cons (db :base-url) rest)))

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
   #(rf/dispatch (let [value (or (.-value %2) (.-checked %2))]
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

(rf/reg-sub
 :account
 (fn [db _]
   (get-in db [:account])))

(rf/reg-sub
 :user-type
 (fn [db _]
   (get-in db [:account :user-type])))

(rf/reg-sub
 :user-state
 (fn [db _]
   (get-in db [:account :user-info :state])))



(rf/reg-event-fx
 :logout
 [(rf/inject-cofx :store)]
 (fn [{db :db store :store} _]
   {:db (dissoc db :account)
    :store (dissoc store :token)
    :dispatch [:goto "/"]}))

(defn as-options [get-text data]
  (map (fn [[key value]] {:key key :text (get-text value) :value key}) data))

(rf/reg-sub
 :residency-program
 (fn [db _]
   (get-in db [:constants :data :residency-program] [])))

(rf/reg-sub
 :residency-program-as-options
 #(rf/subscribe [:residency-program])
 #(as-options :name %))

(rf/reg-sub
 :speciality
 (fn [db _]
   (get-in db [:constants :data :speciality] [])))

(rf/reg-sub
 :speciality-as-options
 #(rf/subscribe [:speciality])
 #(as-options :name %))
