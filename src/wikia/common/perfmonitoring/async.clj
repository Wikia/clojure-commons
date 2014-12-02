(ns wikia.common.perfmonitoring.async
  (:require [clojure.core.async :refer [>!!
                                        <!!
                                        put!
                                        thread
                                        timeout
                                        alt!!]]
            [environ.core :refer [env]]))

(defn enqueue [chan-in event]
  (put! chan-in event))

(defn write-loop! [config callback]
  (thread
    (loop []
      (when-let [points (<!! (:chan-out @config))]
        (callback points)
        (recur)))))

(defn read-loop! [chan-in chan-out buffer-size buffer-timeout]
  (thread
    (loop [batch []
           timeout-chan (timeout buffer-timeout)]
      (alt!!
        chan-in ([e]
                  (when-not (nil? e)
                    (if (> buffer-size (count batch))
                      (recur (conj batch e) timeout-chan)
                      (do
                        (>!! chan-out (conj batch e))
                        (recur [] (timeout buffer-timeout))))))
        timeout-chan ([_]
             (if (empty? batch)
               (recur batch (timeout buffer-timeout))
               (do
                 (>!! chan-out batch)
                 (recur [] (timeout buffer-timeout)))))))))
