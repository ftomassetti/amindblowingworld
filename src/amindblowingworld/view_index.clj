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
          [:img#worldMap {:src "/map.png"}]]
        [:div#menu.column
          [:h3 "Menu"]
          [:textarea {:cols "30" :rows "30"}]]
        [:div#news.column
          [:h3 "News"]
          [:select#newsList {:size 30}
            [:option {:value "v0" :selected "true"} "World created"]]]
        [:br {:style "clear: right;"}]]
    ]))
