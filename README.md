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
  (:require [massive-cljs.core :as massive]
            [massive-cljs.query :as query]
            [cljs.core.async :refer [<!]]))
```

### Connect:
```Clojure
(massive/init! "http://localhost:5432")
```

### Query:
Query functions return a channel, which will receive a response of the format:
```Clojure
{:error? false :content [{:id 1 :name "Steve"} {:id 2 :name "Bill"}]}
```
If `:error?` is true, the content key will be absent and the error message will be present as `:msg`.

In general `query/db-fn` mirrors the syntax of Massive's database functions:
```Clojure
(go
  (let [response (<! (query/db-fn :users :find {:city "London"}))]
    (if (:error? response)
      (println (:msg response))

      (for [row (:content response)]
        (println row)))))
```

```Clojure
(query/db-fn :users :save {:email "new@example.com" :city "Paris"}) ;Insert a new user
(query/db-fn :users :save {:id 4 :city "New York"}) ;Update an existing user by including the PK as a parameter
(query/db-fn :users :find-one {:email "email@example.com"}) ;Returns a single result in :content, rather than a list
(query/db-fn :my-special-function [arg1 arg2]) ;Looks for db/mySpecialFunction.sql in project root
```

Raw SQL can be executed directly using `query/run`, with a list of parameters as the optional second argument:
```Clojure
(query/run "SELECT * FROM users WHERE id > $1" [min-id])
```

