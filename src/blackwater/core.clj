(ns blackwater.core
  (:require [clojure.java.jdbc :as j]
            [clojure.java.jdbc.sql :as s]
            [lumiere :refer :all]
            [clj-time.core :as time]))

(def mysql-db {:subprotocol "sqlite"
               :classname "org.sqlite.JDBC"
               :subname "./test.sql"})

(defn log [sql millis]
  (println (str (bold (green (str sql)))) "| took:" (str (red (str millis))) "ms"))

(def extract-transaction? #'j/extract-transaction?)

(defn query! [f]
  (fn [& args]
    ;; (println "query! args: " args)
    (let [start (time/now)
          result (apply f args)
          end (time/now)
          time-taken (time/in-millis (time/interval start end))]
      (log (first (second args)) time-taken)
      result)))

(defn execute! [f]
  (fn [& args]
    ;; (println "query! args: " args)
    (let [start (time/now)
          result (apply f args)
          end (time/now)
          time-taken (time/in-millis (time/interval start end))]
      (log (clojure.string/join " | " (second args)) time-taken)
      result)))

(defn insert! [f]
  (fn [& args]
    ;; (println "insert! args: " args)
    (let [start (time/now)
          [db table & options] args
          [transaction? maps-or-cols-and-values-etc] (extract-transaction? options)
          stmts (apply s/insert table maps-or-cols-and-values-etc)
          result (apply f args)
          end (time/now)
          time-taken (time/in-millis (time/interval start end))]
      ;; (println "insert! destruct: " transaction? maps-or-cols-and-values-etc stmts)
      (log (clojure.string/join " | " (first stmts)) time-taken)
      result)))

(defn decorate-query! []
  (alter-var-root
   #'j/query
   query!))

(defn decorate-insert! []
  (alter-var-root
   #'j/insert!
   insert!))

(defn decorate-execute! []
  (alter-var-root
   #'j/execute!
   execute!))

;; (defn decorate-do-prepared! []
;;   (alter-var-root
;;    #'j/do-prepared
;;    decorate-var!))

;; (defn decorate-transaction! []
;;   (alter-var-root
;;    #'j/db-transaction*
;;    decorate-var!))

;; (defn decorate-insert! []
;;   (alter-var-root
;;    #'j/insert!
;;    decorate-var!))

;; (defn decorate-query! []
;;   (alter-var-root
;;    #'j/query
;;    decorate-var!))

(def all-decs
  [; decorate-do-prepared! decorate-transaction! decorate-execute!
   decorate-insert!
   decorate-query!])
