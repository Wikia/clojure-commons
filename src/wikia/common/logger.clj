(ns wikia.common.logger
  (:require [clj-logging-config.log4j :as log-config]
            [clojure.tools.logging :as log]
            [clojure.data.json :as json])
  (:use [environ.core])
  (:import (org.apache.log4j.net SyslogAppender)
           (org.apache.log4j FileAppender EnhancedPatternLayout)))

(def appender-skeleton
  (if (= (env :logger-type "file") "syslog")
    (SyslogAppender.
      (EnhancedPatternLayout. (str
                                (env :logger-application "wikia")
                                ": %m%n"))
      (env :logger-syslog-host "127.0.0.1")
      SyslogAppender/LOG_LOCAL0)
    (FileAppender.
      (EnhancedPatternLayout. "%d [%p] - %m%n")
      (env :logger-file-output "logs/wikia-logger.log")
      true)))

(log-config/set-logger! :name (env :logger-application "wikia")
                        :level :debug
                        :out appender-skeleton)

; looks like not all of the 8 syslog levels are available :(
(defn debug
  ([message context]
   (log/debug (json/write-str {"@message" message "@context" context})))
  ([message]
   (log/debug (json/write-str {"@message" message}))))

(defn info
  ([message context]
   (log/info (json/write-str {"@message" message "@context" context})))
  ([message]
   (log/info (json/write-str {"@message" message}))))

(defn warn
  ([message context]
   (log/warn (json/write-str {"@message" message "@context" context})))
  ([message]
   (log/warn (json/write-str {"@message" message}))))

(defn error
  ([message context]
   (log/error (json/write-str {"@message" message "@context" context})))
  ([message]
   (log/error (json/write-str {"@message" message}))))
