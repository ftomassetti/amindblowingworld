(ns amindblowingworld.views
  (:require
    [hiccup
      [page :refer [html5]]
      [page :refer [include-js]]]))

(defn index-page []
  (html5
    [:head
      [:title "AMindBlowingWorld"]
      (include-js "/js/main.js")]
    [:body
      [:h1 "AMindBlowingWorld"]
      [:div#authDiv "auth here"]
      [:div#appDiv "app here" [:div#world]]
    ]))
