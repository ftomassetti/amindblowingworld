(ns amindblowingworld.useractions
  (:require [amindblowingworld.world :refer :all]
            [amindblowingworld.history :refer :all]
            [amindblowingworld.civs :refer :all]
            [clojure.data.json :as json]))

; Return false if no villages are nearby
(defn disaster-request [x y name]
  (let [pos {:x x :y y}]
    (json/write-str (disaster pos (rand-between 30 60) name (rand)))))
