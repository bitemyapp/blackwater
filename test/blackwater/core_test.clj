(ns blackwater.core-test
  (:require [clojure.test :refer :all]
            [clojure.java.jdbc :as j]
            [clojure.java.jdbc.sql :as s]
            [clansi.core :as clansi]
            [korma.core :refer :all]
            [korma.db :refer :all]
            [black.water.korma :refer [decorate-korma!]]
            [black.water.jdbc :refer [decorate-cjj!]]))

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

(defn run-multiline-query []
  (j/query sqlite-db
           [(str "SELECT *
                  FROM test_table")]))

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

(defn run-korma-insert []
  (insert test-table
          (values {:str "I have been inserted!" :num 99})))

(defn run-korma-update []
  (update test-table
          (set-fields {:str "I have been updated!"
                       :num 9090})
          (where {:id 2})))

(defn run-korma-insert-and-delete []
  (insert test-table
          (values {:str "I have been inserted!" :num 99 :id 9001}))
  (delete test-table
          (where {:id 9001})))


(defn count-newlines [string]
  (count (clojure.string/split string #"\n")))

(deftest test-blackwater-cjj
  (decorate-cjj!)
  (binding [clansi/*use-ansi* false]
    (testing "c.j.j query generates log line"
      (let [query-out (with-out-str (run-query))]

        (is (.contains
             query-out
             "SELECT * FROM test_table"))

        (is (= (count-newlines query-out) 1))))

    (testing "c.j.j multiline-query generates single log line"
      (let [query-out (with-out-str (run-multiline-query))]

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

(deftest test-blackwater-korma
  (decorate-korma!)
  (binding [clansi/*use-ansi* false]

    (testing "korma query generates log line"
      (let [out (with-out-str (run-korma-query))]

        (is (.contains
             out
             "SELECT \"test_table\".\"id\" AS \"str\" FROM \"test_table\""))))

    (testing "korma insert generates log line"
      (let [out (with-out-str (run-korma-insert))]

        (is (.contains
             out
             "INSERT INTO \"test_table\" (\"str\", \"num\") VALUES (?, ?)"))))

    (testing "korma update generates log line"
      (let [out (with-out-str (run-korma-update))]

        (is (.contains
             out
             "UPDATE \"test_table\" SET \"num\" = ?, \"str\" = ? WHERE (\"test_table\".\"id\" = ?)"))))

    (testing "korma insert and delete generates log line"
      (let [out (with-out-str (run-korma-insert-and-delete))]

        (is (.contains
             out
             "INSERT INTO \"test_table\" (\"str\", \"num\", \"id\") VALUES (?, ?, ?)"))

        (is (.contains
             out
             "DELETE FROM \"test_table\" WHERE (\"test_table\".\"id\" = ?)"))

        (is (= (count-newlines out) 2))))))
