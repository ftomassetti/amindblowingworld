(ns amindblowingworld.view_index
  (:require
    [hiccup
      [page :refer [html5]]
      [page :refer [include-js include-css]]]))

;(def map-legend []
;  ()
;)

(defn index-page []
  (html5
    [:head
      [:title "AMindBlowingWorld"]
      (include-js "/js/app.js")
      (include-css "/css/app.css")]
    [:body {:onload "initApp();"}
      [:h1 "AMindBlowingWorld"]
      [:div#authDiv "auth here"]
      [:div#appDiv
        [:div#world.column
          [:h3 "World Map"]
          [:img#worldMap {:src "/img/world.png"}]]
        [:div#menu.column
          [:h3 "Menu"]]
        [:div#news.column
          [:h3 "News"]
          [:textarea {:cols "60" :rows "30"}]]
        [:br {:style "clear: right;"}]]
    ]))
