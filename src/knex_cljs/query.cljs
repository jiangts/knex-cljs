(ns knex-cljs.query
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [clojure.string]
            [cljs.core.async :refer [>! chan close!]]
            [knex-cljs.core :refer [instance parse]]))

(defn camel-case
  "Converts kebab-case to camelCase"
  [s]
  (clojure.string/replace s #"-(\w)" (comp clojure.string/upper-case second)))

(defn handler
  "Processes and writes query result to channel"
  [channel]
  (fn [err results]
    (let [return {:error? (boolean err)}]
      (go (>! channel
              (if err
                (assoc return :msg err)
                (assoc return :content (parse results :keywordize-keys true))))
          (close! channel)))))

;; use .call/.apply since a normal cljs function call emits
;; f.call(null, <args>). Instead, we need
;; f.call(knex, <args>)
(defn build-query
  [knex [k v]]
  (let [f (aget knex (-> k name camel-case))]
    (if (vector? v)
      (.apply f knex (cljs->js v))
      (.call f knex (clj->js v)))))

(defn run
  "Runs given query (a plain cljs map) on table"
  ([table query]
   (run table query nil))
  ([table query trx]
   (let [channel (chan)
         knex-query (reduce build-query (@instance (name table)) query)]
     (cond-> knex-query
       trx (.transacting trx)
       :always (.asCallback (handler channel)))
     channel)))
