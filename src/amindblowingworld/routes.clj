(ns amindblowingworld.routes
  (:use compojure.core
        amindblowingworld.views
        amindblowingworld.view_index
        [hiccup.middleware :only (wrap-base-url)])
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.response :as response]))

(defroutes main-routes
  (GET "/" [] (index-page))
  (GET "/map.png" [] (response-biome-map))
  (GET ["/history/since/:event-id", :event-id #"[0-9]+"] [event-id] (history-since (read-string event-id)))
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (-> (handler/site main-routes)
      (wrap-base-url)))
