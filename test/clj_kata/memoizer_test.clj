(ns clj-kata.memoizer-test
  (:use [clojure.test] 
        [clj-kata.memoizer]))

(defn log2 [n]
  (/ (Math/log n) (Math/log 2)))

(with-test
  (def f (bounded-memoize log2 2))
  (is (= (f 1) 0.0) "first")
  (is (= (f 2) 1.0) "second")
  (is (= (f 1) 0.0))
  (is (= (f 4) 2.0))
  (is (= (f 1) 0.0)))


(defn g
 [x]
 (println "Got" x "from" (Thread/currentThread))
 (Thread/sleep 5000)
 (case x
   3 (g 2)
   2 (g 1)
   1 :done))

(def g (bounded-memoize g 2))

(-> #(do (g 3)
  (println "Done for" (Thread/currentThread)))
  Thread.
  .start)
(-> #(do (g 3)
  (println "Done for" (Thread/currentThread)))
  Thread.
  .start)
