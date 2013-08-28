(ns blackwater.core
  (:require [clojure.java.jdbc :as j]
            [lumiere :refer :all]
            [clj-time.core :as time]))

(def mysql-db {:subprotocol "sqlite"
               :classname "org.sqlite.JDBC"
               :subname "./test.sql"})

(defn log [sql millis]
  (println (str (bold (green (str sql)))) "| took: " (str (red (str (millis)))) "ms"))

(defn decorate-query! [f]
  (fn [& args]
    (println args)
    (let [start (time/now)
          result (apply f args)
          end (time/now)
          time-taken (time/in-millis (time/interval start end))]
      
      (println (str (bold (green (first (second args))))) "| took: " (str (red (str time-taken))) "ms")
      result)))

;; (defn decorate-do-prepared! []
;;   (alter-var-root
;;    #'j/do-prepared
;;    decorate-var!))

;; (defn decorate-transaction! []
;;   (alter-var-root
;;    #'j/db-transaction*
;;    decorate-var!))

;; (defn decorate-execute! []
;;   (alter-var-root
;;    #'j/execute!
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
