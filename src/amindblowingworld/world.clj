(ns amindblowingworld.world)

(import '(com.github.lands.World))
(import '(com.github.lands.Biome))

(defn load-world [filename]
  (let [f (java.io.File. filename)]
    (. com.github.lands.PickleSerialization loadWorld f)))

;(defonce world (atom (load-world "worlds/seed_13038.world")))

(defn biome-matrix [world]
  (let [w (-> world .getDimension .getWidth)
        h (-> world .getDimension .getHeight)
        b (-> world .getBiome)]
    (for [y (range h)]
      (for [x (range w)]
        (.get b x y)))))
