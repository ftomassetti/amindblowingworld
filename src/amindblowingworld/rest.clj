(ns amindblowingworld.rest
  (:require [amindblowingworld.world :refer :all]
            [amindblowingworld.history :refer :all]
            [amindblowingworld.civs :refer :all]
            [clojure.data.json :as json]))

(def users (atom []))

(defn add-user-request [name]
  (swap! users conj name)
  (json/write-str "OK"))

(defn users-request []
  (json/write-str @users))

(defn total-pop-request []
  (json/write-str (total-pop)))

(defn- tribe-info [tribe]
  {:id (:id tribe) :name (.name tribe) :settlements (:settlements tribe)})

(defn- settlement-info [s]
  {:id (:id s) :name (.name s) :pos (:pos s) :tribe (:owner s) :pop (:pop s)})

(defn tribes-request []
  (json/write-str (map tribe-info (vals (:tribes @game)))))

(defn- settlements-of-tribe [tribe-id]
  (map get-settlement (:settlements (get-tribe tribe-id))))

(defn tribe-settlements-request [tribe-id]
  (json/write-str (map settlement-info (settlements-of-tribe tribe-id))))

(defn settlements-request []
  (json/write-str (map settlement-info (vals (:settlements @game)))))