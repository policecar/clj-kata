(ns project-euler.core
  (:use [clojure.set :as set]))


(defn multiples-of-3-and-5
  [n]
  (assert (integer? n))
  
  (reduce + (set/union (set (range 0 n 3)) (set (range 0 n 5)))))
