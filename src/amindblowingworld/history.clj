(ns amindblowingworld.history
  (:require [amindblowingworld.world :refer :all]))

(defonce events (atom []))

(defn record-event [msg pos]
  (println "Event: " msg " at: " pos)
  (swap! events conj {:msg msg :pos pos :timestamp (.getTime (java.util.Date.))}))