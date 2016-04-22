(ns knex-cljs.core)

(defonce instance (atom nil))

(def db instance)

(defn knex-lib
  []
  (try
    (js/require "knex")
    (catch js/Object e nil)))

(defn connect!
  [config]
  (if-let [lib (knex-lib)]
    (lib (clj->js config))
    (throw (js/Error. "knex.js library not found"))))

(defn init!
  [config]
  (reset! instance (connect! config)))

(defn parse
  ([x] (parse x {:keywordize-keys false}))
  ([x & opts]
   (let [{:keys [keywordize-keys]} opts
         keyfn (if keywordize-keys keyword str)
         f (fn thisfn [x]
             (cond
               (satisfies? IEncodeClojure x)
               (-js->clj x (apply array-map opts))

               (seq? x)
               (doall (map thisfn x))

               (coll? x)
               (into (empty x) (map thisfn x))

               (array? x)
               (vec (map thisfn x))

               (instance? js/Date x)
               x

               (= (goog/typeOf x) "object")
               (into {} (for [k (js-keys x)]
                          [(keyfn k) (thisfn (aget x k))]))

               :else x))]
     (f x))))
