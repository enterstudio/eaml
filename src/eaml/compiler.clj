(ns eaml.compiler
  (:require [eaml.error :refer :all]
            [eaml.util :refer :all]
            [eaml.xml :as xml]
            [eaml.scope :as scope]
            [eaml.transpile.simple-res :refer :all]
            [eaml.transpile.style :refer :all]
            [eaml.parser :as parser]))

(defn- transpile-node
  [scope node]
  (let [type (:node node)]
    (cond (style? type)
            (transpile-style scope node)
          (simple-res? type)
            (transpile-simple-res scope node)
          :else (raise! "Unknown node type: " node))))

(defn- insert-resources-top-level
  [config-map]
  (loop [result-map config-map
         remaining-keys (keys result-map)]
    (if (empty? remaining-keys)
      result-map
      (let [key (first remaining-keys)
            value (get config-map key)]
        (recur (assoc result-map key (cons* value :resources {}))
               (rest remaining-keys))))))

(defn- skip-node?
  [{node-type :node}]
  (= node-type :mixin))

(defn transpile
  "Transpile the given AST into an XmlStruct.
  Returns a map of configuration => XmlStruct where
  configuration is a configuration name e.g. 'tablet'"
  [ast]
  (let [scope (scope/create ast)
        transpile-scoped #(transpile-node scope %)
        ast (filter (complement skip-node?) ast)]
    (->> (map transpile-scoped ast)
         (group-maps)
         (insert-resources-top-level))))

(defn transpile-str
  "Equivalent to transpile but takes an eaml program string as input instead
  of an AST and transpiles it."
  [string]
  (let [ast (parser/parse-str string)]
    (transpile ast)))


