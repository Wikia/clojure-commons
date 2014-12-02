(ns wikia.common.perfmonitoring.core
  (:import (java.net DatagramSocket DatagramPacket InetAddress))
  (:require [cheshire.core :as json]
            [environ.core :refer [env]]
            [wikia.common.perfmonitoring.async :as async]))

(declare format-content
         format-series-name
         send-data
         write-listener)

(def host (env :perfmonitoring-host))
(def port (Integer. (env :perfmonitoring-port 5551)))
(def app (.toLowerCase (env :perfmonitoring-app "wikia")))
(def buffer-size (Integer. (env :perfmonitoring-buffer-size 2000)))
(def buffer-timeout (Integer. (env :perfmonitoring-buffer-timeout-ms 10000)))
(def series-name (env :perfmonitoring-series-name "metrics"))
(def config (atom nil))

(defn init []
  (when (env :perfmonitoring-host)
    (swap! config #(or % {:host (InetAddress/getByName host)
                          :port port
                          :socket (DatagramSocket.)
                          :chan-in (async/create-chan buffer-size)
                          :chan-out (async/create-chan buffer-size)}))
    (async/read-loop! (:chan-in @config) (:chan-out @config) buffer-size buffer-timeout)
    (async/write-loop! config write-listener)))

(defn write-listener [points]
  (send-data (json/generate-string (format-content points))))

(defn format-series-name [series-name]
  (let [series-name (if (keyword? series-name)
                      (name series-name)
                      series-name)]
    (keyword (str app "_" (clojure.string/replace (.toLowerCase series-name) "-" "_")))))

(defn format-content [points]
  (reduce (fn [coll next]
            (let [series-name (:series-name next)
                  point (dissoc next :series-name)
                  columns (keys point)
                  vals (vals point)]
              (conj coll {:name series-name
                          :columns columns
                          :points (list vals)})))
          []
          points))

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
    (.send (:socket @config) packet)))

(defn publish
  ([series-name point]
    (when @config
      (async/enqueue (:chan-in @config) (merge {:series-name (format-series-name series-name)} point))))
  ([point]
    (publish series-name point)))

(defmacro series-timing [series-name metric & body]
  `(let [start# (System/currentTimeMillis)
         result# (do ~@body)]
     (publish ~series-name {~metric (- (System/currentTimeMillis) start#)})
     result#))

(defmacro timing [metric & body]
  `(series-timing ~series-name ~metric ~@body))
