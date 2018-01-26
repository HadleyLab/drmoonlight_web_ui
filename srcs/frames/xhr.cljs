(ns frames.xhr
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [clojure.string :as str]
            [re-frame.core :as rf]))

(defn to-query [params]
  (->> params
       (mapv (fn [[k v]] (str (name k) "=" v)))
       (str/join "&")))

(defn json-fetch [{uri :uri
                   token :token
                   headers :headers
                   params :params
                   success :success
                   error :error :as opts}]
  (let [headers (merge (or headers {})
                       {"Content-Type" "application/json"}
                       (if (nil? token) {} {"Authorization" (str "Token " token)}))
        fetch-opts (-> (merge {:method "get"} opts)
                       (dissoc :uri :headers :success :error :params)
                       (assoc :headers headers))
        fetch-opts (if (:body opts) (assoc fetch-opts :body (.stringify js/JSON (clj->js (:body opts)))) fetch-opts)]
    (->
     (js/fetch (str (:uri opts) (when params (str "?" (to-query params))))
               (clj->js fetch-opts))
     (.then (fn [resp]
              (if (= (.-status resp) 204)
                (rf/dispatch [(:event success)
                              (merge success
                                     {:request opts
                                      :response resp})])
                (.then (.json resp)
                       (fn [doc]
                         (if (< (.-status resp) 299)
                           (rf/dispatch [(:event success)
                                         (merge success
                                                {:request opts
                                                 :response resp
                                                 :data (js->clj doc :keywordize-keys true)})])
                           (rf/dispatch [(:event error)
                                         (merge success
                                                {:request opts
                                                 :response resp
                                                 :data (js->clj doc :keywordize-keys true)})])))))))
     {})))
(rf/reg-fx :json/fetch json-fetch)

(defn- dispatch-write-with-fx [path data fx]
  (rf/dispatch [:db/write path data])
  (cond
    (nil? fx) nil
    (fn? fx) (rf/dispatch (fx (:data data)))
    :else (rf/dispatch fx)))

(defn json-fetch->path [{path :path
                         uri :uri
                         token :token
                         headers :headers
                         params :params
                         map-result :map-result
                         succeed-fx :succeed-fx
                         failure-fx :failure-fx :as opts}]
  (println (:body opts))
  (let [headers (merge {"Content-Type" "application/json"}
                       (or headers {})
                       (if (nil? token) {} {"Authorization" (str "Token " token)}))
        fetch-opts (-> (merge {:method "get"} opts)
                       (dissoc :uri :headers :success :error :params)
                       (assoc :headers headers))
        fetch-opts (if (:body opts)
                     (assoc fetch-opts :body (.stringify js/JSON (clj->js (:body opts))))
                     fetch-opts)
        status-path (conj path :status)
        map-result (or map-result identity)]
    (rf/dispatch [:db/write status-path :loading])
    (->
     (js/fetch (str (:uri opts) (when params (str "?" (to-query params))))
               (clj->js fetch-opts))
     (.then (fn [resp]
              (if (= (.-status resp) 204)
                (dispatch-write-with-fx status-path :succeed succeed-fx)
                (-> (.json resp)
                    (.then #(js->clj % :keywordize-keys true))
                    (.then (fn [doc]
                             (if (>= (.-status resp) 400)
                               (dispatch-write-with-fx path {:status :failure
                                                             :errors doc} failure-fx)
                               (dispatch-write-with-fx path {:status :succeed
                                                             :data (map-result doc)} succeed-fx)))))))))))
(rf/reg-fx :json/fetch->path json-fetch->path)

(rf/reg-fx
 ::fetch
 (fn [{uri :uri content :content suc :success :as args}]
   (->
    (js/fetch uri)
    (.then (fn [x]
             (cond
               (= :text content) (.text x)
               (= :json content) (.json x)
               :else (assert false (str "Expected content text or json, but " content)))))
    (.then (fn [txt]
             (rf/dispatch [suc {:content txt
                                :args args}]))))))
