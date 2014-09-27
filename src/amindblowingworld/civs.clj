(ns amindblowingworld.civs
  )

(import '(com.github.lands.World))
(import '(com.github.lands.Biome))

(defn load-world [filename]
  (let [f (java.io.File. filename)]
    (. com.github.lands.PickleSerialization loadWorld f)))

(def world (load-world "worlds/seed_13038.world"))

(defn pop-balancer []
  (println "Balancing population"))

(defn run-every-second [f]
  (future (Thread/sleep 1000)
    (f)
    (run-every-second f)))

(defn init []
  (run-every-second pop-balancer))

(defn biome-matrix [world]
  (let [w (-> world .getDimension .getWidth)
        h (-> world .getDimension .getHeight)
        b (-> world .getBiome)]
    (for [y (range h)]
      (for [x (range w)]
        (.get b x y)))))

