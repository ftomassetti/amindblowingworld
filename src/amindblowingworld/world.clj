(ns amindblowingworld.world
  [:import [com.github.lands World Biome PickleSerialization]])

(defn load-world [filename]
  (let [f (clojure.java.io/as-file filename)]
    (. PickleSerialization loadWorld f)))

;(defonce world (atom (load-world "worlds/seed_13038.world")))

(defn biome-matrix [^World world]
  (let [d (.getDimension world)
        w (.getWidth d)
        h (.getHeight d)
        b (.getBiome world)]
    (for [y (range h)]
      (for [x (range w)]
        (.get b x y)))))
