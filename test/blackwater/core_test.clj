(ns blackwater.core-test
  (:require [clojure.test :refer :all]
            [clojure.java.jdbc :as j]
            [clojure.java.jdbc.sql :as s]
            [clansi.core :as clansi]
            [korma.core :refer :all]
            [korma.db :refer :all]
            [black.water :refer :all]))

(def sqlite-db {:subprotocol "sqlite"
               :classname "org.sqlite.JDBC"
               :subname "./test.sql"})

(defdb korma-db sqlite-db)

(defentity test-table
  (pk :id)
  (table :test_table))

(defn run-query []
  (j/query sqlite-db
           (s/select * :test_table)))

(defn run-insert []
  (j/insert! sqlite-db :test_table
             {:str "I have returned!" :num 30}))

(defn run-update []
  (j/update! sqlite-db :test_table
            {:str "I have been updated!" :num 3000}
            (s/where {:id 2})))

(defn run-insert-and-delete []
  (j/insert! sqlite-db :test_table
             {:str "I have returned!" :num 10 :id 9001})
  (j/delete! sqlite-db :test_table
             (s/where {:id 9001})))

(defn run-korma-query []
  (select test-table
          (fields [:id :str :num])))

(defn count-newlines [string]
  (count (clojure.string/split string #"\n")))

(deftest test-blackwater
  (decorate-query!)
  (decorate-insert!)
  (decorate-execute!)
  (binding [clansi/*use-ansi* false]
    (testing "c.j.j query generates log line"
      (let [query-out (with-out-str (run-query))]

        (is (.contains
             query-out
             "SELECT * FROM test_table"))

        (is (= (count-newlines query-out) 1))))

    (testing "c.j.j insert generates log line"
      (let [insert-out (with-out-str (run-insert))]

        (is (.contains
             insert-out
             "INSERT INTO test_table ( str, num ) VALUES ( ?, ? )"))

        (is (= (count-newlines insert-out) 1))))

    (testing "c.j.j update generates log line"
      (let [update-out (with-out-str (run-update))]

        (is (.contains
             update-out
             "UPDATE test_table SET str = ?,num = ? WHERE id = ?"))

        (is (= (count-newlines update-out) 1))))

    (testing "c.j.j insert and delete generates log line"
      (let [i-and-d-out (with-out-str (run-insert-and-delete))]

      (is (.contains
           i-and-d-out
           "INSERT INTO test_table ( str, num, id ) VALUES ( ?, ?, ? )")

          (.contains
           i-and-d-out
           "DELETE FROM test_table WHERE id = ?"))

      (is (= (count-newlines i-and-d-out) 2))))))
