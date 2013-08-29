(ns black.water
  (:require [clojure.java.jdbc :as j]
            [robert.hooke :refer [add-hook]]
            [clojure.java.jdbc.sql :as s]
            [korma.db :as kdb]
            [korma.core :as kc]
            [clansi.core :refer :all]
            [clj-time.core :as time]))

(def mysql-db {:subprotocol "sqlite"
               :classname "org.sqlite.JDBC"
               :subname "./test.sql"})

(def *sql-color* :green)
(def *time-color* :red)

(defn log [sql millis]
  (println (style sql *sql-color*) "| took:" (style millis *time-color*) "ms"))

(def extract-transaction? #'j/extract-transaction?)

(defn query-hook [f & args]
  (let [start (time/now)
        result (apply f args)
        end (time/now)
        time-taken (time/in-millis (time/interval start end))]
    (log (first (second args)) time-taken)
    result))

(defn execute-hook [f & args]
  (let [start (time/now)
        result (apply f args)
        end (time/now)
        time-taken (time/in-millis (time/interval start end))]
    (log (clojure.string/join " | " (second args)) time-taken)
    result))

(defn insert-hook [f & args]
  (let [start (time/now)
        [db table & options] args
        [transaction? maps-or-cols-and-values-etc] (extract-transaction? options)
        stmts (apply s/insert table maps-or-cols-and-values-etc)
        result (apply f args)
        end (time/now)
        time-taken (time/in-millis (time/interval start end))]
    (log (clojure.string/join " | " (first stmts)) time-taken)
    result))

(defn korma-hook [f & args]
  (let [start (time/now)
        result (apply f args)
        end (time/now)
        time-taken (time/in-millis (time/interval start end))]
    (log (:sql-str (first args)) time-taken)
    result))

(defn decorate-query! []
  (add-hook #'j/query #'query-hook))

(defn decorate-insert! []
  (add-hook #'j/insert! #'insert-hook))

(defn decorate-execute! []
  (add-hook #'j/execute! #'execute-hook))

(defn decorate-cjj!
  "Hooks into clojure.java.jdbc to log queries, inserts, and execute"
  []
  (decorate-query!)
  (decorate-insert!)
  (decorate-execute!))

(defn decorate-korma!
  "Hooks into Korma to log SQL that gets executed."
  []
  (add-hook #'kdb/exec-sql #'korma-hook))
