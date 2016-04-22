(ns massive-cljs.query
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [massive-cljs.query])
  (:require [cljs.core.async :refer [>! chan close!]]
            [massive-cljs.core :refer [instance parse]]))

(defn handler
  "processes results of query (called from db-fn macroexpansion)"
  [channel]
  (fn [err results]
    (let [return {:error? (boolean err)}]
      (go (>! channel
              (if err
                (assoc return :msg err)
                (assoc return :content (parse results :keywordize-keys true))))
          (close! channel)))))

(defn run
  "Execute raw SQL"
  ([query]
   (run query []))
  ([query params]
   (let [channel (chan)]
     (.run @instance
           query
           (clj->js params)
           (handler channel))
     channel)))
