(ns wikia.common.perfmonitoring.async
  (:require [clojure.core.async :refer [chan
                                        sliding-buffer
                                        go
                                        >!
                                        >!!
                                        put!
                                        thread
                                        timeout
                                        alt!!
                                        <!
                                        <!!]]
            [environ.core :refer [env]]))

(defn enqueue [chan-in event]
  (put! chan-in event))

(defn read-loop! [chan-in chan-out buffer-size buffer-timeout]
  (thread
    (loop [batch []
           to (timeout buffer-timeout)]
      (alt!!
        chan-in ([e]
                  (when-not (nil? e)
                    (if (> buffer-size (count batch))
                      (recur (conj batch e) to)
                      (do
                        (>!! chan-out (conj batch e))
                        (recur [] (timeout buffer-timeout))))))
        to ([_]
             (if (empty? batch)
               (recur batch (timeout buffer-timeout))
               (do
                 (>!! chan-out batch)
                 (recur [] (timeout buffer-timeout)))))))))
