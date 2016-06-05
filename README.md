# knex-cljs

Clojurescript wrapper for the [Knex.js](http://knexjs.org/) SQL query builder.

# Design
- queries are pure data

# Next

### Transactions
- make option for `run` that accepts a transaction object
- then perhaps make `runBatch` or something, that automatically nests (? or
    paralellizes?) a set of queries and calls `trx.commit` and `trx.rollback`
    automatically for the user as appropriate.









## Overview

FIXME: Write a paragraph about the library/project and highlight its goals.

## Usage
### Require: 
```Clojure
(ns example.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [knex-cljs.core :as k]
            [knex-cljs.query :as q]
            [cljs.core.async :refer [<!]]))
```

### Connect:
```Clojure
(k/init! "http://localhost:5432")
```

### Query:
Query functions return a channel, which will receive a response of the format:
```Clojure
{:error? false :content [{:id 1 :name "Steve"} {:id 2 :name "Bill"}]}
```
If `:error?` is true, the content key will be absent and the error message will be present as `:msg`.

The arguments to `q/run` are simply the table name and a map of the chainable
functions in Knex. 
```Clojure
(go
  (let [response (<! (q/run "users" {:where {:city "London"}}))]
    (if (:error? response)
      (println (:msg response))

      (for [row (:content response)]
        (println row)))))
```
Notice that queries are just plain old Clojure maps!
The key is the Knex query builder method to call and the values are the arguments.
For "multi-word" method names such as `whereNot`, you may use the keyword `:where-not`.
Now you can build helper functions that create and manipulate queries as you please!

