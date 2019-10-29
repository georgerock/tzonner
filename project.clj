 (defproject tzonner "0.1.0-SNAPSHOT"
   :description "TimeZonnes CRUD"
   :dependencies [[org.clojure/clojure "1.10.0"]
                  [metosin/compojure-api "2.0.0-alpha30"]
                  [ring-cors "0.1.13"]]
   :ring {:handler tzonner.handler/app}
   :uberjar-name "server.jar"
   :profiles {:dev {:dependencies [[javax.servlet/javax.servlet-api "3.1.0"]]
                   :plugins [[lein-ring "0.12.5"]]}})
