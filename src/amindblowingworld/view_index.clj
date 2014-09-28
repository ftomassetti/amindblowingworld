(ns amindblowingworld.view_index
  (:require
    [hiccup
      [page :refer [html5]]
      [element :refer [link-to]]
      [page :refer [include-js include-css]]]
      [amindblowingworld.rest :as rest]))

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

(defn table-headers[]
  [:tr [:th "ID"] [:th "Settlement"] [:th "Tribe"] [:th "Population"] [:th "Biome"]])

;; (def login-form
;;   [:div {:class "row"}
;;    [:div {:class "columns small-12"}
;;     [:h3 "Login..."]
;;     [:div {:class "row"}
;;      [:form {:method "POST" :action "login" :class "columns small-4"}
;;       [:div "Username" [:input {:type "text" :name "username"}]]
;;       [:div "Password" [:input {:type "password" :name "password"}]]
;;       [:div [:input {:type "submit" :class "button" :value "Login"}]]]]
;;     [:h3 "...or sign up"]
;;     [:div {:class "row"}
;;      [:form {:method "POST" :action "signup" :class "columns small-4"}
;;       [:div "Username" [:input {:type "text" :name "username"}]]
;;       [:div "Password" [:input {:type "password" :name "password"}]]
;;       [:div [:input {:type "submit" :class "button" :value "Sign up"}]]]]
;;     ]])

(defn index-page []
  (html5
   [:head
    [:title "AMindBlowingWorld"]
    (include-js "/js/jquery-1.10.2.js")
    (include-js "/js/jquery-ui.js")
    (include-js "/js/jquery.dataTables.min.js")
    (include-js "/js/external.js")
    (include-js "/js/app.js")
    (include-css "/css/dark-hive/jquery-ui.css")
    (include-css "/css/jquery.dataTables.css")
    (include-css "/css/app.css")]
   [:body {:onload "initApp();"}
    [:div#appViewport
     [:div#header
      [:h1 "A-Mind-Blowing-World"]
      [:h2 "The miracle of life, civilizations rising and... the possibility to destroy everything one-click away!"]]
     [:div#accordion
      [:h3 "Map and updates"]
      [:div#appDiv
       [:div#world.column
        [:h3 "World Map"]
        [:img#worldMap {:src "/img/ancient.png"}]]
       [:div#menu.column
        ;;[:h3 "Landscapes"]
        ;;[:table (landscape-colors-legend)]
        [:h3 "Make damage!"]
        [:form#damageReasons (damage-reasons ["Vulcano"] true) (damage-reasons ["Earthquake" "Hunger" "Tsunami" "Tornado" "Meteor" "Russian invasion" "Crazy Putin"] false)]]
       [:div#news.column
        [:h3 "News"]
        [:select#newsList {:size 30}
         [:option {:value "v0" :selected "true"} "World created"]]]
       [:div#users.column
        [:h3 "Registered users"]
        [:div#usersList "Nobody is regitered yet!"]
        [:br]
        [:div "Username" [:input#login {:type "text"}]]
        [:div "Password" [:input#passwrd {:type "password"}]]
        [:input#registerUser {:type "button" :value "Login and damage!"}]
        [:div#worldpop
         [:span.worldpopLabel "Total population: "] [:span.worldpopValue "0"]]]
       [:br {:style "clear: right;"}]]
      [:h3 "Tribes and settlements"]
      [:div#tableDiv
       [:table#tribesAndSettlements {:class "display" :cellspacing "0" :width "100%"}
        [:thead (table-headers)]
        [:tfoot (table-headers)]]]
      [:h3 "About A-Mind-Blowing-World"]
      [:div#aboutDiv
       [:div.longText
        [:p "This application was built while paricipating in the "
         (link-to "http://clojurecup.com" "ClojureCup 2014") "."]
        [:p "While growing up we were fascinated by game like "
         (link-to "http://en.wikipedia.org/wiki/Populous" "Populous")
         ". As children we encountered these first examples of simulating civilizations evolutions in computer games.
            They were heroic attempts done in on the poor hardware available at that time.
            This is a small tribute to the kind of creation which captured our fantasy, a long time ago."]
        [:p "The game is settled in a content generated world. We used "
         (link-to "http://github.com/ftomassetti/lands" "Lands") " to produce that world.
            On top of it we simulated the evolution of different tribes. These are a few characteristics of that simulation:"]
        [:ul
         [:li "Each tribe can own many villages; they spawn in the same area"]
         [:li "Each tribe has its own language: you will notice that the name of the tribe and the name of its villages sound similar"]
         [:li "The size of villages depends on the environment (e.g., villages tend to grow bigger in Forest rather than in sand desert)."]]
        [:p "While these elements are not very evident in the game, we like to think that they contribute a bit to the atmosphere of the game itself."]
        [:p "In this game the player has only the possibility to destroy, but destruction plays a fundamental role in the mysterious circle of life..."]
        [:p "Finally, you will notice that two disasters have rather peculiar names. A member of our team comes
                from Ukraine and we would like to use this occasion to share our hope for peace in his country."]
        [:p "Maybe we could invite Putin to play this game and cause destruction here, instead of hurting real people."]
        ]]
    ]]]))
