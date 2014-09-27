(ns amindblowingworld.civs
  )

(import '(com.github.lands.World))
(import '(com.github.lands.Biome))

(defn load-world [filename]
  (let [f (java.io.File. filename)]
    (. com.github.lands.PickleSerialization loadWorld f)))

(def world (load-world "worlds/seed_13038.world"))

; --------------------------------------
; Records
; --------------------------------------

(defrecord Tribe [id name language settlements])
(defrecord Game [next-id tribes settlements])

; --------------------------------------
; Global state
; --------------------------------------

(def world (atom (load-world "worlds/seed_13038.world")))

(defn create-game []
  (Game. 1 {} {}))

(def game  (atom (create-game)))

; --------------------------------------
; Tribe functions
; --------------------------------------

(defn- create-tribe-in-game [game]
  (let [next-id (.next-id game)
        game (assoc game :next-id (inc next-id))
        language nil
        name nil
        tribe (Tribe. next-id nil nil [])
        game (assoc-in game [:tribes next-id] tribe)]
      (println "Creating tribe [id " next-id "] name: " name)
    game))

(defn create-tribe []
  (swap! game create-tribe-in-game))

; --------------------------------------
; Game functions
; --------------------------------------

(defn total-pop []
  0)

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

