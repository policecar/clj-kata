(ns clj-kata.plumbing)
;; source: https://github.com/Prismatic/plumbing/blob/master/README.md

;; Example usage of Prismatic's plumbing library

(use 'plumbing.core)

;; define a Graph /plumbing equivalent of 'stats'
(def stats-graph
  "A graph specifying the same computation as 'stats'"
  {:n  (fnk [xs]   (count xs))
   :m  (fnk [xs n] (/ (sum identity xs) n))
   :m2 (fnk [xs n] (/ (sum #(* % %) xs) n))
   :v  (fnk [m m2] (- m2 (* m m)))})


;; "compile" the stats-graph into a single function ...
(require '[plumbing.graph :as graph])
(def stats-eager (graph/eager-compile stats-graph))

(= {:n 4
    :m 3
    :m2 (/ 25 2)
    :v (/ 7 2)}
   (stats-eager {:xs [1 2 3 6]}))

;; ... with validity checking
;; Missing :xs key exception
; (thrown? Throwable (stats-eager {:ys [1 2 3]}))


;; (easily) extend stats-graph
(def extended-stats  
  (graph/eager-compile 
    (assoc stats-graph
      :sd (fnk [^double v] (Math/sqrt v)))))

(= {:n 4
    :m 3
    :m2 (/ 25 2)
    :v (/ 7 2)
    :sd (Math/sqrt 3.5)}
   (extended-stats {:xs [1 2 3 6]}))


;; "compile" the graph with different execution strategies, e.g. lazy evaluation ...
(def lazy-stats (graph/lazy-compile stats-graph))

(def output (lazy-stats {:xs [1 2 3 6]}))
;; Nothing has actually been computed yet
(= (/ 25 2) (:m2 output))
;; Now :n, :m, and :m2 have been computed, but :v is still behind a delay        

;; ... or parallel evaluation
(def par-stats (graph/par-compile stats-graph))

(def output (par-stats {:xs [1 2 3 6]}))
;; Nodes are being computed in futures, with :m and :m2 going in parallel after :n
(= (/ 7 2) (:v output))


;; inquire input and output schemata of stats-graph
(require '[plumbing.fnk.pfnk :as pfnk])

;; stats-graph takes a map with one required key, :xs
(= {:xs true}
   (pfnk/input-schema stats-graph))

;; stats-graph outputs a map with four keys, :n, :m, :m2, and :v
(= {:n true :m true :m2 true :v true}
   (pfnk/output-schema stats-graph))


;; wrap stats-graph in a higher-order function, e.g. to profile sub-functions
(def profiled-stats (graph/eager-compile (graph/profiled ::profile-data stats-graph)))

;;; times in milliseconds for each step:
(= {:n 1.001, :m 0.728, :m2 0.996, :v 0.069}
   @(::profile-data (profiled-stats {:xs (range 10000)})))


;; introducing fnk and defnk, macros for keyword functions
(use 'plumbing.core)
(defnk simple-fnk [a b c] 
  (+ a b c))

(= 6 (simple-fnk {:a 1 :b 2 :c 3}))
;; Below throws: Key :c not found in (:a :b)
; (thrown? Throwable (simple-fnk {:a 1 :b 2}))

;; ... with optional keys and a potential default value
(defnk simple-opt-fnk [a b {c 1}] 
  (+ a b c))

(= 4 (simple-opt-fnk {:a 1 :b 2}))

;; ... with nested map bindings
(defnk simple-nested-fnk [a [:b b1] c] 
  (+ a b1 c))

(= 6 (simple-nested-fnk {:a 1 :b {:b1 2} :c 3}))   
;; Below throws: Expected a map at key-path [:b], got type class java.lang.Long
; (thrown? Throwable (simple-nested-fnk {:a 1 :b 1 :c 3})) 

(defnk simple-nested-fnk2 [a [:b b1 [:c {d 3}]]] 
  (+ a b1 d))

(= 4 (simple-nested-fnk2 {:a 1 :b {:b1 2 :c {:d 1}}}))   
(= 5 (simple-nested-fnk2 {:a 1 :b {:b1 1 :c {}}}))


;; more plumbing goodies, e.g. 'for' for maps
(use 'plumbing.core)
(= (for-map [i (range 3) 
             j (range 3) 
             :let [s (+ i j)]
             :when (< s 3)] 
      [i j] 
      s)
   {[0 0] 0, [0 1] 1, [0 2] 2, [1 0] 1, [1 1] 2, [2 0] 2})

;; IllegalArgumentException Key :c not found in {:a 1, :b 2} 
; (thrown? Exception (safe-get {:a 1 :b 2} :c))

;; return k -> (f v) for [k, v] in map
(= (map-vals inc {:a 0 :b 0})
   {:a 1 :b 1})


;; introducing 'penguin' operators!
(use 'plumbing.core)
(= (let [add-b? false]
     (-> {:a 1}
         (merge {:c 2})
         (?> add-b? assoc :b 2)))
   {:a 1 :c 2})

(= (let [inc-all? true]
     (->> (range 10)
          (filter even?)
          (?>> inc-all? map inc)))
    [1 3 5 7 9])