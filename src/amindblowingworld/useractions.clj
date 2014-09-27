(ns amindblowingworld.useractions
  (:require [amindblowingworld.world :refer :all]
            [amindblowingworld.history :refer :all]
            [amindblowingworld.civs :refer :all]
            [clojure.data.json :as json]))

(defn disaster-request [x y name]
  (let [pos {:x x :y y}]
    (disaster pos 20 name 0.5)
    (json/write-str "Ok")))
