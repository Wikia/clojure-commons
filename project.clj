(defproject wikia/commons "0.1.5-SNAPSHOT"
  :description "Set of common utilities that are useful throughout Wikia's projects"
  :url "https://github.com/Wikia/clojure-commons"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[cheshire "5.3.1"]
                 [clj-logging-config "1.9.7"]
                 [environ "0.5.0"]
                 [org.clojure/clojure "1.6.0"]

                 ; logger
                 [ch.qos.logback/logback-classic "1.1.3"]
                 [net.logstash.logback/logstash-logback-encoder "4.6"]
                 [io.clj/logging "0.8.1"]
                 [org.slf4j/log4j-over-slf4j "1.7.2"]]
  :profiles  {:dev  {:source-paths  ["dev"]
                     :plugins [[lein-midje "3.1.1"]]
                     :dependencies  [[midje "1.6.3"]]}}
  :repl-options {:init-ns user})
