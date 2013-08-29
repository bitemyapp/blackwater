(ns black.water
  (:require [clojure.java.jdbc :as j]
            [robert.hooke :refer [add-hook]]
            [clojure.java.jdbc.sql :as s]
            [clansi.core :refer :all]
            [clj-time.core :as time]))

(def mysql-db {:subprotocol "sqlite"
               :classname "org.sqlite.JDBC"
               :subname "./test.sql"})

(defn log [sql millis]
  (println (style sql :green) "| took:" (style millis :red) "ms"))

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

(defn decorate-query! []
  (add-hook #'j/query #'query-hook))

(defn decorate-insert! []
  (add-hook #'j/insert! #'insert-hook))

(defn decorate-execute! []
  (add-hook #'j/execute! #'execute-hook))
