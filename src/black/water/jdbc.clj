(ns black.water.jdbc
  (:require [clojure.java.jdbc :as j]
            [robert.hooke :refer [add-hook]]
            [java-jdbc.sql :as s]
            [clansi.core :refer :all]
            [black.water.log :refer [log-sql]]
            [clj-time.core :as time]))

(def extract-transaction? #'j/extract-transaction?)

(defn generic-logger
  [accessor f args]
  (let [start (time/now)
        result (apply f args)
        end (time/now)
        time-taken (time/in-millis (time/interval start end))]
    (log-sql (accessor args) time-taken)
    result))

(defn query-hook
  "Hook for c.j.j query, destructures sql from arguments
   and times the run-time, sending the results to the log fn."
  [f & args]
  (generic-logger #(first (second %)) f args))

(defn execute-hook
  "Hook for c.j.j execute!"
  [f & args]
  (generic-logger #(clojure.string/join " | " (second %)) f args))

(defn db-do-commands-hook
  "Hook for c.j.j db-do-commands"
  [f & args]
  (generic-logger last f args))

(defn insert-hook
  "Hook for c.j.j insert!"
  [f & args]
  (let [start (time/now)
        [db table & options] args
        [transaction? maps-or-cols-and-values-etc] (extract-transaction? options)
        stmts (apply s/insert table maps-or-cols-and-values-etc)
        result (apply f args)
        end (time/now)
        time-taken (time/in-millis (time/interval start end))]
    (log-sql (clojure.string/join " | " (first stmts)) time-taken)
    result))

(defn decorate-query!
  "decorate c.j.j/query, wraps the fn var."
  []
  (add-hook #'j/query #'query-hook))

(defn decorate-insert!
  "decorate c.j.j/insert!, wraps the fn var."
  []
  (add-hook #'j/insert! #'insert-hook))

(defn decorate-execute!
  "decorate c.j.j/execute!, wraps the fn var."
  []
  (add-hook #'j/execute! #'execute-hook))

(defn decorate-db-do-commands!
  "decorate c.j.j/execute!, wraps the fn var."
  []
  (add-hook #'j/db-do-commands #'db-do-commands-hook))

(defn decorate-cjj!
  "Hooks into clojure.java.jdbc to log queries, inserts, and execute"
  []
  (decorate-db-do-commands!)
  (decorate-query!)
  (decorate-insert!)
  (decorate-execute!))
