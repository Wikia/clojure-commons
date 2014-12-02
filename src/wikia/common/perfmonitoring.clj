(ns wikia.common.perfmonitoring
  (:import (java.net DatagramSocket DatagramPacket InetAddress)
           (java.util Random))
  (:require [cheshire.core :as json]
            [environ.core :refer [env]]))

(def host (env :perfmonitoring-host))
(def port (Integer. (env :perfmonitoring-port 5551)))
(def app (env :perfmonitoring-app "wikia"))
(def config (atom nil))
(def socket-agent (agent nil))
(def series-name (str (.toLowerCase app)
                      "_"
                      (.toLowerCase (env :perfmonitoring-series-name "metrics"))))

(defn init []
  (when (env :perfmonitoring-host)
    (send socket-agent #(or % (DatagramSocket.)))
    (swap! config #(or % {:host (InetAddress/getByName host)
                          :port port}))))

(defn send-packet [socket packet]
  (try
    (doto socket (.send packet))
    (catch Exception e
      socket)))

(defn send-data [content]
  content
  (when-let [packet (try
                      (DatagramPacket.
                        ^"[B" (.getBytes content)
                        ^Integer (count content)
                        ^InetAddress (:host @config)
                        ^Integer (:port @config))
                      (catch Exception e
                        nil))]
    (send socket-agent send-packet packet)))

(defn publish [point]
  (when @config
    (let [columns (keys point)
          point (list (vals point))]
      (send-data (json/generate-string [{:name series-name
                                        :columns columns
                                        :points point}])))))

(defmacro timing [metric & body]
  `(let [start# (System/currentTimeMillis)
         result# (do ~@body)]
     (publish {~metric (- (System/currentTimeMillis) start#)})
     result#))

(init)