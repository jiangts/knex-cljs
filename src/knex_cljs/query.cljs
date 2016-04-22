(ns knex-cljs.query
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [clojure.string]
            [cljs.core.async :refer [>! chan close!]]
            [knex-cljs.core :refer [instance parse]]))

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

(defn camel-case
  "Converts kebab-case to camelCase"
  [s]
  (clojure.string/replace s #"-(\w)" (comp clojure.string/upper-case second)))

(defn build-query
  [knex [k v]]
  (let [f (aget knex (-> k name camel-case))]
    (f (clj->js v))))

(defn run
  "Runs given query (a plain cljs map) on table"
  ([table query]
   (run table query nil))
  ([table query trx]
   (let [channel (chan)
         knex-query (reduce build-query (@instance table) query)]
     (when trx
       (.transacting knex-query trx))
     (.asCallback knex-query (handler channel))
     channel)))
