(ns black.water.korma
  (:require [clojure.java.jdbc :as j]
            [robert.hooke :refer [add-hook]]
            [korma.db :as kdb]
            [korma.core :as kc]
            [clansi.core :refer :all]
            [black.water.jdbc :refer [generic-logger]]
            [black.water.log :refer [log-sql]]
            [clj-time.core :as time]))

(defn korma-hook
  "Hook for korma, mercifully the library has a universal, singular
   function where all queries eventually end up. <3"
  [f & args]
  (generic-logger #(:sql-str (first %)) f args))

(defn decorate-korma!
  "Hooks into Korma to log SQL that gets executed."
  []
  (add-hook #'kdb/exec-sql #'korma-hook))
