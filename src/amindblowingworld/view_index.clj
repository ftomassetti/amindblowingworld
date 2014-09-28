(ns amindblowingworld.view_index
  (:require
    [hiccup
      [page :refer [html5]]
      [page :refer [include-js include-css]]]))

(def landscape-colors [["Settlement" 255 0 0]
                       ["Ocean" 0 0 225]
                       ["Iceland" 255 255 255]
                       ["Tundra" 141 227 0]
                       ["Alpine" 141 227 0]
                       ["Glacier" 255 255 255]
                       ["Grassland" 80 173 88]
                       ["Rock Desert" 105 120 59]
                       ["Sand Desert" 205 227 141]
                       ["Forest" 59 120 64]
                       ["Savanna" 171 161 27]
                       ["Jungle" 5 227 34]])
                       
(defn landscape-color-to-table-row [landscape-name red green blue]
  [:tr [:td {:style (format "background-color: rgb(%d,%d,%d);" red green blue)} "&nbsp;&nbsp;&nbsp;&nbsp;"]
       [:td landscape-name]])

(defn landscape-colors-legend []
  (map #(apply landscape-color-to-table-row %) landscape-colors))


(defn damage-reasons [damages isChecked]
  (map (fn [damage] [:div [:input {:type :radio :name :damage :value damage :checked isChecked} damage] [:br]]) damages))

(defn index-page []
  (html5
    [:head
      [:title "AMindBlowingWorld"]
      (include-js "/js/jquery-1.10.2.js")
      (include-js "/js/jquery-ui.js")
      (include-js "/js/external.js")
      (include-js "/js/app.js")
      (include-css "/css/dark-hive/jquery-ui.css")
      (include-css "/css/app.css")]
    [:body {:onload "initApp();"}
      [:h1 "AMindBlowingWorld"]
      [:div#accordion
        [:h3 "Map and updates"]
        [:div#appDiv
          [:div#world.column
            [:h3 "World Map"]
            [:img#worldMap {:src "/map.png"}]]
          [:div#menu.column
            [:h3 "Landscapes"]
            [:table (landscape-colors-legend)]
            [:h3 "Make damage!"]
            [:form#damageReasons (damage-reasons ["Vulcano"] true) (damage-reasons ["Earthquake" "Hunger" "Tsunami" "Tornado" "Meteor" "Russian invasion" "Crazy Putin"] false)]]
          [:div#news.column
            [:h3 "News"]
            [:select#newsList {:size 30}
              [:option {:value "v0" :selected "true"} "World created"]]]
          [:br {:style "clear: right;"}]]
        [:h3 "Tribes and villages - table"]
        [:div#tableDiv]]
    ]))
