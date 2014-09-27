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
(declare get-next-id)
(declare update-settlement-pop)

(defn get-settlement [id]
  (get-in @game [:settlements id]))

(defn get-tribe [id]
  (get-in @game [:tribes id]))

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

;(defn update-biome-map [] )

; --------------------------------------
; Tribe functions
; --------------------------------------

(defn settlement-at [pos]
  (let [settlements (vals (.settlements @game))]
    (reduce (fn [acc s]
              (if (= pos (.pos s))
                (.id s)
                acc)) nil settlements)))

(defn free-cell? [pos]
  (nil? (settlement-at pos)))

(defn land? [pos]
  (let [x (:x pos) y (:y pos)
        w (-> (get-world) .getDimension .getWidth)
        h (-> (get-world) .getDimension .getHeight)]
    (if (or (< x 0) (< y 0) (>= x w) (>= y h))
      false
      (let [ b (-> (get-world) .getBiome)
             biome (.get b (:x pos) (:y pos))]
        (not (= (.name biome) "OCEAN"))))))

(def fastness 1000)

(defn settlements-around [pos radius]
  (let [cx (:x pos)
        cy (:y pos)
        all-cells-around (for [dx (range -5 5) dy (range -5 5)]
                           {:x (+ cx dx) :y (+ cy dy)})
        land-cells-around (filter land? all-cells-around)
        settlements        (map settlement-at land-cells-around)
        settlements       (filter (fn [s] (not (nil? s))) settlements)]
    settlements))

(defn disaster [pos radius name strength]
  (let [settlements (settlements-around pos radius)]
    (doseq [s-id settlements]
      (let [ s (get-settlement s-id)
             dead (int (* strength (.pop s)))
             new-pop (- (.pop s) dead)]
        (update-settlement-pop s-id new-pop)
        (record-event (str dead " died in " (.name s) " because of " name) (.pos s))))))

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

(defn free-random-land-near [center]
  (let [cx (:x center)
        cy (:y center)
        all-cells-around (for [dx (range -5 5) dy (range -5 5)]
                            {:x (+ cx dx) :y (+ cy dy)})
        land-cells-around (filter land? all-cells-around)
        free-land-cells-around (filter free-cell? land-cells-around)]
    (if (empty? free-land-cells-around)
      nil
      (rand-nth free-land-cells-around))))

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

(declare update-settlement-fun)

(defn spawn-new-village-from [id-settlement]
  (try
    (let [old-village   (get-settlement id-settlement)
        _               (assert (not (nil? id-settlement)) (str "Unable to find settlement " id-settlement " in game " @game))
        tribe-id        (.owner old-village)
        _               (assert (not (nil? tribe-id)) (str "Old village has no owner: " old-village))
        tribe           (get-tribe tribe-id)
        _               (assert (not (nil? tribe)) (str "No tribe found with id " tribe-id " in " @game))
        language        (.language tribe)
        new-village-name (.name language)
        new-pos (free-random-land-near (.pos old-village))
        _               (assert (not (nil? new-pos)))
        pop-new-village 100
        pop-old-village (- (.pop old-village) pop-new-village)
        settlement (Settlement. (get-next-id) new-village-name pop-new-village (.id tribe) new-pos)
        id-new-settlement (.id settlement)
        new-settlements-list (conj (.settlements tribe) (.id settlement))
        _ (update-tribe (assoc tribe :settlements new-settlements-list))
        _ (swap! game assoc-in [:settlements (.id settlement)] settlement)]
      (record-event (str "Village " new-village-name " is born from " (.name old-village)) nil)
      (update-settlement-pop id-settlement pop-old-village)
      (run-randomly (update-settlement-fun id-new-settlement) (* fastness 3) (* fastness 10)))
    (catch AssertionError e (println "assertion failed: " (.getMessage e)))))

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
          (let [perc (- 1.00 (/ (rand) 7.5))
                new-pop (int (* (.pop s) perc))]
            (update-settlement-pop id-settlement new-pop)))
        (let [s (get-settlement id-settlement)]
          (when (and (< (.pop s) 70) (chance 0.35))
            (update-settlement-pop id-settlement 0)
            (update-biome-map)
            (record-event (str "Village " (.name s) " is now a ghost town") nil))
          (when (and (> (.pop s) 500) (chance 0.15))
            (spawn-new-village-from id-settlement)
            (update-biome-map)
            ))))))

(defn get-next-id []
  (let [next-id (.next-id @game)
        _ (swap! game assoc :next-id (inc next-id))]
    next-id))

(defn create-tribe []
  (try
    (let [id-tribe        (get-next-id)
          id-settlement   (get-next-id)
          language        (generate-language)
          name-tribe      (.name language)
          name-settlement (.name language)
          pos             (free-random-land)
          settlement      (Settlement. id-settlement name-settlement 100 id-tribe pos)
          tribe           (Tribe. id-tribe name-tribe language [id-settlement])
          _               (update-tribe tribe)
          _               (update-settlement settlement)]
          _               (assert (= settlement (get-settlement id-settlement)))
          _               (assert (= tribe (get-tribe id-tribe)))
        (run-randomly (update-tribe-fun id-tribe) 3000 10000)
        (run-randomly (update-settlement-fun id-settlement) (* fastness 3) (* fastness 10))
        (record-event (str "Creating tribe " name-tribe) pos)
        (record-event (str "Creating village " name-settlement) pos)
        (update-biome-map))
    (catch AssertionError e (println "Create tribe: " (.getMessage e)))))

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
  (.start (Thread.
    (fn []
      (while true (do
                    (Thread/sleep (+ min (rand-int (- max min))))
                    (f)))))))

(defn init []
  (run-every-n-seconds pop-balancer 10))


