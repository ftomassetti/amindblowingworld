(ns amindblowingworld.rest
  (:require [amindblowingworld.world :refer :all]
            [amindblowingworld.history :refer :all]
            [amindblowingworld.civs :refer :all]
            [clojure.data.json :as json]))

(def users (atom ()))

(defn list-users []
  (map :username @users))

(defn get-user [name]
  (first (filter #(= name (:username %)) @users)))

(defn is-user?
  ([name]
     (is-user? @users name))
  ([db name]
     (some #(= name (:username %)) db)))

(defn add-user-request [name pass]
  (cond
      (and (is-user? name) (= pass (-> name get-user :password))) (json/write-str "OK")
      (and (is-user? name) (not (= pass (-> name get-user :password)))) (json/write-str "User is existing already and the password is wrong. Please choose another name or input the correct password")
      :else (do (swap! users conj {:username name :password pass}) (json/write-str "OK"))
      ))

(defn users-request []
  (json/write-str (list-users)))

(defn total-pop-request []
  (json/write-str (total-pop)))

(defn- tribe-info [tribe]
  {:id (:id tribe) :name (.name tribe) :settlements (:settlements tribe)})

(defn- settlement-info [s]
  {:id (:id s)
   :name (.name s)
   :pos (:pos s)
   :tribe (:owner s)
   :pop (:pop s)
   :tribe-name (:name (get-tribe (:owner s)))
   :biome (.name (get-biome (:pos s)))})

(defn tribes-request []
  (json/write-str (map tribe-info (vals (:tribes @game)))))

(defn- settlements-of-tribe [tribe-id]
  (map get-settlement (:settlements (get-tribe tribe-id))))

(defn tribe-settlements-request [tribe-id]
  (json/write-str (map settlement-info (settlements-of-tribe tribe-id))))

(defn settlements-request []
  (json/write-str (map settlement-info (vals (:settlements @game)))))

(defn- tribe-and-settlement-to-data-list [tas]
  [(:id tas) (:name tas) (:tribe-name tas) (:pop tas) (:biome tas)])
(defn- tribes-and-settlements [data]
  {:data (map tribe-and-settlement-to-data-list data)})

(defn tribes-and-settlements-request []
  (json/write-str (tribes-and-settlements (map settlement-info (vals (:settlements @game))))))

(defn settlement-info-request [settlement-id]
  (let [ s (get-settlement settlement-id)
         desc (if (nil? s)
                ""
                (let [name  (.name s)
                      tribe (.name (get-tribe (.owner s)))
                      pop   (.pop s)]
                  (str "Name: " name "<br/>Tribe: " tribe "<br/>Pop: " pop"<br/>ID: " (.id s))))]
    (println "DESC=" desc)
    (json/write-str desc)))
