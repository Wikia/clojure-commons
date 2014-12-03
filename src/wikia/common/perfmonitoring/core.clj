(ns wikia.common.perfmonitoring.core
  (:import (java.net DatagramSocket DatagramPacket InetAddress))
  (:require [cheshire.core :as json]
            [environ.core :refer [env]]))

(declare format-content
         format-series-name
         send-data
         write-listener)

(def host (env :perfmonitoring-host))
(def port (Integer. (env :perfmonitoring-port 5551)))
(def app (.toLowerCase (env :perfmonitoring-app "wikia")))
(def series-name (env :perfmonitoring-series-name :metrics))
(def config (atom nil))

(defn init []
  (when (env :perfmonitoring-host)
    (swap! config #(or % {:host (InetAddress/getByName host)
                          :port port
                          :socket (DatagramSocket.)}))))

(defn format-series-name [series-name]
  (let [series-name (if (keyword? series-name)
                      (name series-name)
                      series-name)]
    (keyword (str app "_" (clojure.string/replace (.toLowerCase series-name) "-" "_")))))

(defn format-content [point]
  (let [series-name (:series-name point)
        point (dissoc point :series-name)
        columns (keys point)
        vals (vals point)]
    [{:name series-name
      :columns columns
      :points (list vals)}]))

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
      (let [point (merge {:series-name (format-series-name series-name)} point)]
        (future (send-data (json/generate-string (format-content point)))))))
  ([point]
    (publish series-name point)))

(defmacro series-timing [series-name metric & body]
  `(let [start# (System/currentTimeMillis)
         result# (do ~@body)]
     (publish ~series-name {~metric (- (System/currentTimeMillis) start#)})
     result#))

(defmacro timing [metric & body]
  `(series-timing ~series-name ~metric ~@body))
