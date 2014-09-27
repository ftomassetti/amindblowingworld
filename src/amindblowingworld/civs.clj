(ns amindblowingworld.civs
  (:require [amindblowingworld.world :refer :all]
            [amindblowingworld.history :refer :all]))

(import '(com.github.lands.World))
(import '(com.github.lands.Biome))

(import java.io.ByteArrayOutputStream)
(import java.io.ByteArrayInputStream)
(import java.awt.image.BufferedImage)
(import java.awt.RenderingHints)
(import java.awt.Color)
(import javax.imageio.ImageIO)

(def world (load-world "worlds/seed_13038.world"))

(defn generate-language []
  (com.github.langgen.SamplesBasedLanguageFactory/getRandomLanguage))

; --------------------------------------
; Records
; --------------------------------------

(defrecord Tribe [id name language settlements])
(defrecord Settlement [id name pop owner pos])
(defrecord Game [next-id tribes settlements])

; --------------------------------------
; Global state
; --------------------------------------

;(def world (atom (load-world "worlds/seed_13038.world")))

(def world (load-world "worlds/seed_13038.world"))

(defn get-world [] world)

(defn create-game []
  (Game. 1 {} {}))

(def game  (atom (create-game)))

(def ^:dynamic saved-biome-map nil)

(declare settlement-at)
(declare run-randomly)
(declare get-settlement)
(declare get-tribe)

(defn get-settlement [id]
  (get-in @game [:settlements id]))

(defn get-tribe [id]
  (get-in @game [:tribe id]))

(defn update-settlement [settlement]
  (let [id (.id settlement)]
    (swap! game assoc-in [:settlements id] settlement)))

(defn update-tribe [tribe]
  (let [id (.id tribe)]
    (swap! game assoc-in [:tribes id] tribe)))

(defn calc-biome-map [world]
  (let [ w (-> world .getDimension .getWidth)
         h (-> world .getDimension .getHeight)
         scale-factor 1
         img (BufferedImage. (* scale-factor w) (* scale-factor h) (BufferedImage/TYPE_INT_ARGB))
         g (.createGraphics img)
         b (-> world .getBiome)]
    (doseq [y (range h)]
      (doseq [x (range w)]
        (if (settlement-at {:x x :y y})
          (.setColor g (Color. 255 0 0))
          (let [pos {:x x :y y}
                biome (.get b x y)]
            (case (.name biome)
              "OCEAN"        (.setColor g (Color. 0 0 255))
              "ICELAND"      (.setColor g (Color. 255 255 255))
              "TUNDRA"       (.setColor g (Color. 141 227 218))
              "ALPINE"       (.setColor g (Color. 141 227 218))
              "GLACIER"      (.setColor g (Color. 255 255 255))
              "GRASSLAND"    (.setColor g (Color. 80 173 88))
              "ROCK_DESERT"  (.setColor g (Color. 105 120 59))
              "SAND_DESERT"  (.setColor g (Color. 205 227 141))
              "FOREST"       (.setColor g (Color. 59 120 64))
              "SAVANNA"      (.setColor g (Color. 171 161 27))
              "JUNGLE"       (.setColor g (Color. 5 227 34))
              (.setColor g (Color. 255 0 0)))))
        (let [pixel-x (* x scale-factor)
              pixel-y (* y scale-factor)]
          (.fillRect g pixel-x pixel-y scale-factor scale-factor))))
    (.dispose g)
    img))

(defn update-biome-map []
  (time (let [img (calc-biome-map (get-world))]
    (def saved-biome-map img))))

; --------------------------------------
; Tribe functions
; --------------------------------------

(defn settlement-at [pos]
  (let [settlements (vals (.settlements @game))]
    (reduce (fn [acc s]
              (if (= pos (.pos s))
                (.id s)
                acc)) nil settlements)))

(defn land? [pos]
  (let [ b (-> (get-world) .getBiome)
         biome (.get b (:x pos) (:y pos))]
    (not (= (.name biome) "OCEAN"))))

(defn random-pos []
  (let [w (-> (get-world) .getDimension .getWidth)
        h (-> (get-world) .getDimension .getHeight)]
    {:x (rand-int w) :y (rand-int h)}))

; TODO check if there is a village there
(defn free-random-land []
  (let [rp (random-pos)]
    (if (land? rp)
      rp
      (free-random-land))))

(defn chance [p]
  (< (rand) p))

(defn update-tribe-fun [id-tribe]
  (fn []
    ;(println "Updating " id-tribe)
    ))

(defn update-settlement-pop [id-settlement new-pop]
  (let [s (get-settlement id-settlement)
        pos (> new-pop (:pop s))
        s (assoc s :pop new-pop)]
    (if pos
      (record-event (str "Population of " (.name s) " growing to " (.pop s)) nil)
      (record-event (str "Population of " (.name s) " shrinking to " (.pop s)) nil))
    (update-settlement s)))

(defn ghost-town? [settlement]
  (= 0 (.pop settlement)))

(defn update-settlement-fun [id-settlement]
  (fn []
    (let [s     (get-settlement id-settlement)
          event (rand-nth [:growing :shrinking :stable])]
      (when (and s (not (ghost-town? s)))
        (when (= event :growing)
          (let [perc (+ 1.0 (/ (rand) 5.0))
                new-pop (int (* (.pop s) perc))]
            (update-settlement-pop id-settlement new-pop)))
        (when (= event :shrinking)
          (let [perc (- 1.00 (/ (rand) 8.0))
                new-pop (int (* (.pop s) perc))]
            (update-settlement-pop id-settlement new-pop)))
        (let [s (get-settlement id-settlement)]
          (when (and (< (.pop s) 70) (chance 0.35))
            (update-settlement-pop id-settlement 0)
            (update-biome-map)
            (record-event (str "Village " (.name s) " is now a ghost town") nil))
          (when (and (> (.pop s) 500) (chance 0.1))
            (println "Spawning a new village")
            (update-biome-map)
            ))))))

(def fastness 1000)

(defn- create-tribe-in-game [game]
  (let [id-tribe (.next-id game)
        game (assoc game :next-id (inc id-tribe))
        id-settlement (.next-id game)
        game (assoc game :next-id (inc id-settlement))
        language (generate-language)
        name-tribe (.name language)
        name-settlement (.name language)
        pos (free-random-land)
        settlement (Settlement. id-settlement name-settlement 100 id-tribe pos)
        tribe (Tribe. id-tribe name-tribe language [id-settlement])
        game (assoc-in game [:tribes id-tribe] tribe)
        game (assoc-in game [:settlements id-settlement] settlement)]
      (run-randomly (update-tribe-fun id-tribe) 3000 10000)
      (run-randomly (update-settlement-fun id-settlement) (* fastness 3) (* fastness 10))
      (record-event (str "Creating tribe " name-tribe) pos)
      (record-event (str "Creating village " name-settlement) pos)
      (update-biome-map)
    game))

(defn create-tribe []
  (swap! game create-tribe-in-game))

; --------------------------------------
; Game functions
; --------------------------------------

(defn total-pop []
  (reduce + (map :pop (vals (.settlements @game)))))

(defn pop-balancer []
  (let [pop (total-pop)]
    (println "Balancing population, total pop: " pop)
    (if (< pop 1000)
      (create-tribe)
      (println "...nothing to do"))))

(defn run-every-second [f]
  (future (Thread/sleep 1000)
    (f)
    (run-every-second f)))

(defn run-every-n-seconds [f n]
  (future (Thread/sleep (* n 1000))
    (f)
    (run-every-n-seconds f n)))

(defn run-randomly [f min max]
  (future (Thread/sleep (+ min (rand-int (- max min))))
    (f)
    (run-randomly f min max)))

(defn init []
  (run-every-n-seconds pop-balancer 10))


