(ns eaml.util)

(defn flat-coll
  "Given a coll of collections, flattens it by one level.
  Example: [[1] [2 3] [] [4 5 6]] will result in [1 2 3 4 5 6]"
  [colls]
  (apply concat colls))

(defn bfs
  "Perform a BFS in the given graph.
  (next-nodes-fn node graph) => takes a node as input and returns all nodes connected to that
  node in the graph."
  [next-nodes-fn initial-node graph]
  (loop [queue [initial-node]
         visited #{}
         result []]
    (if (empty? queue)
      result
      (let [node (first queue)
            queue-tail (subvec queue 1)]
        (if (visited node)
          (recur queue-tail visited result)
          (recur (into queue-tail (next-nodes-fn node graph))
                 (conj visited node)
                 (conj result node)))))))


(defn find-first
  "Iterates over coll and returns the first item such that (func item)
  is truthy. If no item is found then default is returned (defaults to nil)."
  ([func coll default]
   (loop [coll coll]
     (if (empty? coll)
       default
       (if (func (first coll))
         (first coll)
         (recur (rest coll))))))
  ([func coll]
   (find-first func coll nil)))

(defn group-maps
  "Takes a collection of maps and groups their k,v pairs by key"
  [maps]
  (loop [grouped {}
         maps maps]
    (if (empty? maps)
      grouped
      (let [m (first maps)]
        (recur (reduce-kv (fn [reduction k v]
                           (let [current-val (get reduction k)]
                             (if current-val
                               (assoc reduction k (conj current-val v))
                               (assoc reduction k [v]))))
                   grouped m)
               (rest maps))))))

(defn cons*
  "equivalent to calling cons over the values in reverse order.
  Example: (cons* [4 5 6] 1 2 3) == '(1 2 3 4 5 6)"
  [coll & values]
  (if (empty? values)
    coll
    (cons (first values)
          (apply cons* coll (rest values)))))

(defmacro itp
  "Interpolation macro. Similar to ruby interpolation.
  Does not provide any escaping mechanism.
  Syntax: (itp 'Hi #{name}, how are you?')
  Will expand to (str 'Hi ' name ', how are you?')"
  [string]
  (loop [string string
         result []]
    (let [[_ match-sym-anywhere] (re-find #"(.*?)#\{.*?\}" string)
          [regex-match match-symbol] (re-find #"\A#\{(.*?)\}" string)]
      (cond match-symbol
              (recur (.substring string (count regex-match))
                     (conj result (read-string match-symbol)))
            match-sym-anywhere
              (recur (.substring string (count match-sym-anywhere))
                     (conj result match-sym-anywhere))
            :else
              (cons `str (conj result string))))))

(defn case-match
  "Matches string agains the given regex, one by one until a match
  is found. Returns the matched form arg. Returns nil if there are no matches.
  form-pairs => [regex arg]"
  [string & form-pairs]
  (if (empty? form-pairs)
    nil
    (let [[regex form] (take 2 form-pairs)]
      (if (re-find regex string)
        form
        (apply case-match string (rest (rest form-pairs)))))))

(defn merge-lists
  "This function returns a list 'L' of elements s.t. for all a in l1 and for all
  b in l2:
   - f(a) == f(b) then L contains b and does not contain a.
   - f(a) != f(b) then L contains b and a"
  [l1 l2 f]
  (let [mapped (set (map f l2))]
    (loop [result l2
           items l1]
      (if (empty? items)
        result
        (let [item (first items)
              tail (rest items)]
          (if (contains? mapped (f item))
            (recur result tail)
            (recur (conj result item) tail)))))))

(defn singleton?
  [coll]
  (= (count coll) 1))

(defn find-first
  "Return the first x in coll s.t. pred(x) = true"
  [coll pred]
  (first (filter pred coll)))
