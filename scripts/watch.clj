(require '[cljs.build.api :as b])

(b/watch "src"
  {:main 'knex-cljs.core
   :output-to "out/knex_cljs.js"
   :output-dir "out"})
