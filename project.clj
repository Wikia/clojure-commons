(defproject wikia/commons "0.1.2-SNAPSHOT"
  :description "Set of common utilities that are useful throughout Wikia's projects"
  :url "https://github.com/Wikia/clojure-commons"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[cheshire "5.3.1"]
                 [clj-logging-config "1.9.7"]
                 [environ "0.5.0"]
                 [org.clojure/clojure "1.6.0"]

                 ; logger
                 [log4j/log4j "1.2.16" :exclusions [javax.mail/mail javax.jms/jms com.sun.jdmk/jmxtools com.sun.jmx/jmxri]]
                 [org.clojure/tools.logging "0.2.3"]
                 [org.slf4j/slf4j-log4j12 "1.6.4"]]
  :profiles  {:dev  {:source-paths  ["dev"]
                     :plugins [[lein-midje "3.1.1"]]
                     :dependencies  [[midje "1.6.3"]]}}
  :repl-options {:init-ns user})
