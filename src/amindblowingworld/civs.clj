(ns amindblowingworld.civs
  (:require [amindblowingworld.world :refer :all]
            [amindblowingworld.history :refer :all]))

(import '(com.github.lands.World))
(import '(com.github.lands.Biome))

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
      (record-event (str "Creating tribe " name-tribe) pos)
      (record-event (str "Creating village " name-settlement) pos)
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

(defn run-every-second [f]
  (future (Thread/sleep 1000)
    (f)
    (run-every-second f)))

(defn init []
  (run-every-second pop-balancer))


