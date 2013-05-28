(ns clj-kata.memoizer
  (:require [clojure.core.cache :as cache]))

; adapted from http://kotka.de/blog/2010/03/memoize_done_right.html

;; A bounded version of 'memoize' for functions of a single argument which caches
;; the last k values computed. Currently uses a 'first-in-first-out' cache.

(defn bounded-memoize 
  "Return a bounded memoized version of fn 'f' 
   that caches the last 'k' computed values"
  [f k]
  (assert (and (fn? f) (integer? k)))

  (let [fifocache (atom (cache/fifo-cache-factory {} :threshold k))
        hit-or-miss (fn [C item]
                      (if (cache/has? C item)   ; if item in cache
                        (cache/hit C item)      ; return cache
                        (cache/miss C item (delay (apply f item)))))] 

        (fn [& args]
          (let [mem (swap! fifocache hit-or-miss args)] ; update cache 
                @(cache/lookup mem args)))))        ; get desired value (from cache)
                                                    ; deref since it's in a Delay
