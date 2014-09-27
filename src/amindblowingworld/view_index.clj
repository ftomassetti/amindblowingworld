(ns amindblowingworld.view_index
  (:require
    [hiccup
      [page :refer [html5]]
      [page :refer [include-js]]]))

(defn index-page []
  (html5
    [:head
      [:title "AMindBlowingWorld"]
      (include-js "/js/main.js")
      (include-js "/js/app.js")]
    [:body {:onload "initApp();"}
      [:h1 "AMindBlowingWorld"]
      [:div#authDiv "auth here"]
      [:div#appDiv
        [:div#world [:img#worldView {:src "/img/world.png"}]]
        [:div#menu "Menu"]
        [:div#news "News"]]
    ]))
