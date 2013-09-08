(ns black.water
  (:require [clojure.java.jdbc :as j]
            [robert.hooke :refer [add-hook]]
            [clojure.java.jdbc.sql :as s]
            [korma.db :as kdb]
            [korma.core :as kc]
            [clansi.core :refer :all]
            [clj-time.core :as time]))

;; Color global variables, rebind as thou wilt.
(def sql-color :green)
(def time-color :red)

(defn sanitize [sql]
  (let [single-line (apply str (replace {\newline " "} sql))]
    (clojure.string/replace single-line #" {2,}" " ")))

(defn log
  "Given the sql string and the milliseconds it took to execute, print
   a (possibly) colorized readout of the string and the millis."
  [sql millis]
  (let [clean-sql (sanitize sql)]
    (println (style clean-sql sql-color) "| took:" (style millis time-color) "ms")))

;; extract-transaction? wrapper from the c.j.j function.
(def extract-transaction? #'j/extract-transaction?)

(defn query-hook
  "Hook for c.j.j query, destructures sql from arguments
   and times the run-time, sending the results to the log fn."
  [f & args]
  (let [start (time/now)
        result (apply f args)
        end (time/now)
        time-taken (time/in-millis (time/interval start end))]
    (log (first (second args)) time-taken)
    result))

(defn execute-hook
  "Hook for c.j.j execute!"
  [f & args]
  (let [start (time/now)
        result (apply f args)
        end (time/now)
        time-taken (time/in-millis (time/interval start end))]
    (log (clojure.string/join " | " (second args)) time-taken)
    result))

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
    (log (clojure.string/join " | " (first stmts)) time-taken)
    result))

(defn korma-hook
  "Hook for korma, mercifully the library has a universal, singular
   function where all queries eventually end up. <3"
  [f & args]
  (let [start (time/now)
        result (apply f args)
        end (time/now)
        time-taken (time/in-millis (time/interval start end))]
    (log (:sql-str (first args)) time-taken)
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
