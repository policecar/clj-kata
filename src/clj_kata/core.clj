(ns clj-kata.core)

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

(defn qsort [L]
  (if (empty? L) 
      '()
      (let [[pivot & L2] L]
           (lazy-cat (qsort (for [y L2 :when (<  y pivot)] y))
                     (list pivot)
                     (qsort (for [y L2 :when (>= y pivot)] y))))))

(defn exp 
	[x n]
	(reduce * (repeat n x)))

(defn log2 [n]
	(/ (Math/log n) (Math/log 2)))

(defn ceil
	[n] 
	(+ n (- 1.0 (mod n 1))))
