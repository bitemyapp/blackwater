(defproject blackwater "0.0.7"
  :description "Pretty logging for SQL queries in JDBC for Clojure"
  :url "http://github.com/bitemyapp/blackwater"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [clj-time "0.6.0"]
                 [robert/hooke "1.3.0"]
                 [org.clojure/java.jdbc "0.3.0-alpha4"]
                 [korma "0.3.0-RC5"]
                 [myguidingstar/clansi "1.3.0"]]
  :profiles {:dev {:dependencies [[org.xerial/sqlite-jdbc "3.7.2"]]}})
