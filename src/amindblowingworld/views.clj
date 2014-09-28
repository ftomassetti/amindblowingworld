(ns amindblowingworld.views
  (:require
    [amindblowingworld.world :refer :all]
    [amindblowingworld.civs :refer :all]
    [amindblowingworld.history :refer :all]
    [clojure.data.json :as json]))

; ----------------------------------------------------
; Images
; ----------------------------------------------------

(import java.io.ByteArrayOutputStream)
(import java.io.ByteArrayInputStream)
(import java.awt.image.BufferedImage)
(import java.awt.RenderingHints)
(import java.awt.Color)
(import javax.imageio.ImageIO)

(defn image-bytes [image format]
  (let [baos (ByteArrayOutputStream.)
        _ (ImageIO/write image format, baos )
        _ (.flush baos)
        bytes (.toByteArray baos)
        _ (.close baos)]
    bytes))

(defn response-png-image-from-bytes [bytes]
  {
    :status 200
    :headers {"Content-Type" "image/png"}
    :body (ByteArrayInputStream. bytes)
    })

(defn biome-map []
  (when (nil? saved-biome-map)
    (update-biome-map))
  saved-biome-map)

(defn response-biome-map []
  (let [bm (biome-map)
        bytes (image-bytes bm "png")]
    (response-png-image-from-bytes bytes)))

(defn- get-history-since [event-id]
  (if (nil? @events)
    [0 []]
    [(.length @events) (if (>= event-id (.length @events))
                         []
                         (let [events-to-return (subvec @events event-id)]
                           (if (> (.length events) 20) (take-last 20 events) events)))]))

(defn history-since [event-id]
  (json/write-str (get-history-since event-id)))

