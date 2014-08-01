(defproject wikia/commons "0.1.0-SNAPSHOT"
  :description "Set of common utilities that are useful throughout Wikia's projects"
  :url "https://github.com/Wikia/clojure-commons"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [environ "0.5.0"]
                 [org.clojure/data.json "0.2.5"]

                 ; logger
                 [log4j/log4j "1.2.16" :exclusions [javax.mail/mail javax.jms/jms com.sun.jdmk/jmxtools com.sun.jmx/jmxri]]
                 [org.slf4j/slf4j-log4j12 "1.6.4"]
                 [org.clojure/tools.logging "0.2.3"]
                 [ clj-logging-config "1.9.7"]])
