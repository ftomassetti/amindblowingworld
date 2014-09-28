(ns amindblowingworld.civs
  (:require [amindblowingworld.world :refer :all]
            [amindblowingworld.history :refer :all])
  (:import [com.github.lands World Biome]
           [java.io ByteArrayOutputStream ByteArrayInputStream]
           [java.awt Color RenderingHints]
           [java.awt.image BufferedImage]
           [javax.imageio ImageIO]))

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

(defn ^World get-world [] world)

(defn create-game []
  (Game. 1 {} {}))

(def game (atom (create-game)))

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

(defn update-settlement [ settlement]
  (let [id (:id settlement)]
    (swap! game assoc-in [:settlements id] settlement)))

(defn update-tribe [tribe]
  (let [id (:id tribe)]
    (swap! game assoc-in [:tribes id] tribe)))

(defn get-biome [pos]
  (let [x (:x pos)
        y (:y pos)
        b (-> (get-world) .getBiome)
        biome (.get b x y)]
    biome))

(defn population-supported [pos]
  (let [x (:x pos)
        y (:y pos)
        b (-> (get-world) .getBiome)
        biome (.get b x y)]
    (case (.name biome)
      "OCEAN"        0
      "ICELAND"      100
      "TUNDRA"       200
      "ALPINE"       200
      "GLACIER"      0
      "GRASSLAND"    5000
      "ROCK_DESERT"  500
      "SAND_DESERT"  300
      "FOREST"       3000
      "SAVANNA"      1500
      "JUNGLE"       2500)))

(defn altitude-color-factor [altitude]
  (let [f (/ (Math/log (+ 1.0 altitude)) 15.0)]
    f))

(defn mix-values [a b f]
  (+ (* f (float a)) (* (- 1.0 f) (float b))))

(defn mix-colors [^Color a ^Color b f]
  (let [red (int (mix-values (.getRed a) (.getRed b) f))
        green (int (mix-values (.getGreen a) (.getGreen b) f))
        blue (int (mix-values (.getBlue a) (.getBlue b) f))]
    (Color. red green blue)))

(defn- adapt-color-to-elevation-real [color x y]
  (let [elevation (.get (.getElevation (get-world)) x y)]
    (mix-colors (Color. 255 255 255) color (altitude-color-factor elevation))))

(def adapt-color-to-elevation (memoize adapt-color-to-elevation-real))


; --------------------------------------
; Tribe functions
; --------------------------------------

(defn settlement-at [pos]
  (let [settlements (vals (:settlements @game))]
    (reduce (fn [acc s]
              (if (= pos (:pos s))
                (:id s)
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

(def fastness 10000)

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
             dead (int (* strength (:pop s)))
             new-pop (- (:pop s) dead)]
        (update-settlement-pop s-id new-pop)
        (record-event (str dead " died in " (:name s) " because of " name) (:pos s))))
    settlements))

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

(defn free-random-land-near [center distance]
  (let [cx (:x center)
        cy (:y center)
        all-cells-around (for [dx (range (* -1 distance) distance) dy (range (* -1 distance) distance)]
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
  (let [new-pop (int new-pop)
        s (get-settlement id-settlement)
        pos (> new-pop (:pop s))
        s (assoc s :pop new-pop)]
    (if pos
      (record-event (str "Population of " (:name s) " growing to " (:pop s)) (:pos s))
      (record-event (str "Population of " (:name s) " shrinking to " (:pop s)) (:pos s)))
    (update-settlement s)
    (when (= (:pop s) 0)
      ;(update-biome-map)
      (record-event (str "Village " (:name s) " is now a ghost town") (:pos s)))))

(defn ghost-town? [settlement]
  (= 0 (:pop settlement)))

(declare update-settlement-fun)

(defn rand-between [min max]
  (let [diff (- max min)]
    (+ min (rand-int diff))))

(defn spawn-new-village-from [id-settlement]
  (try
    (let [old-village   (get-settlement id-settlement)
        _               (assert (not (nil? id-settlement)) (str "Unable to find settlement " id-settlement " in game " @game))
        tribe-id        (:owner old-village)
        _               (assert (not (nil? tribe-id)) (str "Old village has no owner: " old-village))
        tribe           (get-tribe tribe-id)
        _               (assert (not (nil? tribe)) (str "No tribe found with id " tribe-id " in " @game))
        language        (:language tribe)
        new-village-name (.name language)
        new-pos (free-random-land-near (:pos old-village) 10)
        _               (assert (not (nil? new-pos)))
        pop-new-village (rand-between (/ (:pop old-village) 5) (/ (:pop old-village) 3))
        pop-old-village (- (:pop old-village) pop-new-village)
        settlement (Settlement. (get-next-id) new-village-name pop-new-village (:id tribe) new-pos)
        id-new-settlement (:id settlement)
        new-settlements-list (conj (:settlements tribe) (:id settlement))
        _ (update-tribe (assoc tribe :settlements new-settlements-list))
        _ (swap! game assoc-in [:settlements (:id settlement)] settlement)]
      (record-event (str "Village " new-village-name " is born from " (:name old-village)) new-pos)
      (update-settlement-pop id-settlement pop-old-village)
      (run-randomly (update-settlement-fun id-new-settlement) (* fastness 3) (* fastness 10)))
    (catch AssertionError e (println "assertion failed: " (.getMessage e)))))

(defn events-for [id-settlement]
  (let [s (get-settlement id-settlement)
        _ (assert (not (nil? s)) (str "Settlement with id " id-settlement " not found"))
        events [:growing :shrinking :stable]
        pop (:pop s)
        too-much-pop (> pop (population-supported (:pos s)))]
    (if too-much-pop [:shrinking :stable :shrinking :stable :growing]
      [:growing :shrinking :stable])))

(defn close-to-population-supported [s]
  (let [ps (population-supported (:pos s))
        pop (:pop s)]
    (> pop (* 0.8 ps))))

(defn update-settlement-fun [id-settlement]
  (fn []
    (let [s (get-settlement id-settlement)
          events (events-for id-settlement)
          event (rand-nth events)]
      (when (and s (not (ghost-town? s)))
        (when (= event :growing)
          (let [perc (+ 1.0 (/ (rand) 5.0))
                new-pop (int (* (:pop s) perc))]
            (update-settlement-pop id-settlement new-pop)))
        (when (= event :shrinking)
          (let [perc (- 1.00 (/ (rand) 7.5))
                new-pop (int (* (:pop s) perc))]
            (update-settlement-pop id-settlement new-pop)))
        (let [s (get-settlement id-settlement)]
          (when (and (< (:pop s) 50) (chance 0.35))
            (update-settlement-pop id-settlement 0))
          (when (and (close-to-population-supported s) (chance 0.15))
            (spawn-new-village-from id-settlement)
            ;(update-biome-map)
            ))))))

(defn get-next-id []
  (let [next-id (:next-id @game)]
    (swap! game assoc :next-id (inc next-id))
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
        (run-randomly (update-tribe-fun id-tribe) (* 3 fastness) (* 10 fastness))
        (run-randomly (update-settlement-fun id-settlement) (* fastness 3) (* fastness 10))
        (record-event (str "Creating tribe " name-tribe) pos)
        (record-event (str "Creating village " name-settlement) pos)
        ;(update-biome-map)
        )
    (catch AssertionError e (println "Create tribe: " (.getMessage e)))))

; --------------------------------------
; Game functions
; --------------------------------------

(defn total-pop []
  (int (reduce + (map :pop (vals (:settlements @game))))))

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
  (future (Thread/sleep (* n 1 fastness))
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
