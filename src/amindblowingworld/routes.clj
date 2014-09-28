(ns amindblowingworld.routes
  (:use compojure.core
        amindblowingworld.views
        amindblowingworld.rest
        amindblowingworld.useractions
        amindblowingworld.view_index
        [hiccup.middleware :only (wrap-base-url)])
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.response :as response]
            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])
            [friend-oauth2.workflow :as oauth2wf]
            [friend-oauth2.util :as oauth2u]
            [ring.util.response :as resp]
            [ring.util.codec :as codec]
            [cheshire.core :as json]
            [clj-http.client :as http]
            [hiccup.page :as h]
            [hiccup.element :as e]))

(def auth true)

(defn call-github
  [endpoint access-token]
  (-> (format "https://api.github.com%s%s&access_token=%s"
              endpoint
              (when-not (.contains endpoint "?") "?")
              access-token)
      http/get
      :body
      (json/parse-string (fn [^String s] (keyword (.replace s \_ \-))))))

;; This sort of blind memoization is *bad*. Please don't do this in your real apps.
;; Go use an appropriate cache from https://github.com/clojure/core.cache
(def get-github-handle (memoize (comp :login (partial call-github "/user"))))

(def client-config
  {:client-id "60b87c4b4a6e4428e50d"
   :client-secret "096e27ebcf5c127f857b655bc136b050b6244edc"
   ;; :callback {:domain "http://amindblowingworld.clojurecup.com" :path "/oauth-github/github.callback"}
   })

(def uri-config
  {:authentication-uri {:url "https://github.com/login/oauth/authorize"
                        :query {:client_id (:client-id client-config)
                                :response_type "code"
                                :redirect_uri (oauth2u/format-config-uri client-config)
                                :scope ""
                                }}
   :access-token-uri {:url "https://github.com/login/oauth/access_token"
                      :query {:client_id (:client-id client-config)
                              :client_secret (:client-secret client-config)
                              :grant_type "authorization_code"
                              :redirect_uri (oauth2u/format-config-uri client-config)
                              }}
   })

(def config-auth {:roles #{::user}})

(defn render-main-page [request]
  (let [{access-token :access_token} (friend/current-authentication request)
        identity (friend/identity request)]
    (h/html5
     [:head
      [:title "AMindBlowingWorld"]
      (h/include-js "/js/main.js")
      (h/include-js "/js/app.js")]
     [:body (if access-token {:onload "initApp();"} {})
      [:h1 "AMindBlowingWorld App"]
      [:h2 "Clojurecup 2014"]
      [:h3 "Authentication via GitHub using OAuth2"]
      [:p "Current Status (this will change when you log in/out):"]
      (if (and access-token identity)
        [:p "Logged in as GitHub user "
         [:strong (get-github-handle (:current identity))]
         " with GitHub OAuth2 access token " (:current identity)]
        [:p [:a {:href "github.callback"} "Login with GitHub"]])
      [:h3 "App"]
      (if access-token
        [:div#appDiv
         [:div#world [:img#worldView {:src "/img/world.png"}]]
         [:div#menu "Menu"]
         [:div#news "News"]]
        [:div#appDiv
         [:div#world [:img#worldView {:src "/img/world.png"}]]
         [:div#menu "Menu"]
         [:div#news "News"]])
      [:h3 "Logging out"]
      [:p (e/link-to "/logout" "Click here to log out") "."]
      ]
    )))

(defn render-login-page [request]
  (h/html5
   [:head
    [:title "AMindBlowingWorld"]]
   [:body
    [:h1 "AMindBlowingWorld App"]
    [:h2 "Clojurecup 2014"]
    [:h3 "Authentication via GitHub using OAuth2"]
    [:p "Current Status (this will change when you log in/out):"]
    (if-let [identity (friend/identity request)]
      [:p "Logged in as GitHub user "
       [:strong (get-github-handle (:current identity))]
       " with GitHub OAuth2 access token " (:current identity)]
      [:p (e/link-to "/github.callback" "Login with GitHub")]) ;; link to login trigger
    [:p "Debug:"]
    [:p "r " (str request)]
    [:p "id " (str (friend/identity request))]
    [:p "token" (str (friend/current-authentication request))]
    ]))

(defn render-authenticated-page [request]
  (h/html5
   [:head
    [:title "AMindBlowingWorld"]]
   [:body
    [:h1 "AMindBlowingWorld App"]
    [:h2 "Clojurecup 2014"]
    [:h3 "Authentication via GitHub using OAuth2"]
    [:p "Current Status (this will change when you log in/out):"]
    (if-let [identity (friend/identity request)]
      [:p "Logged in as GitHub user "
       [:strong (get-github-handle (:current identity))]
       " with GitHub OAuth2 access token " (:current identity)]
      [:p "Something is wrong..."])
    [:p (e/link-to "/logout" "Logout here") "."]
    [:p "Debug:"]
    [:p "r " (str request)]
    [:p "id " (str (friend/identity request))]
    [:p "token" (str (friend/current-authentication request))]
    ]))

(defroutes main-routes
;auth;  (GET "/" request
;auth;       (render-login-page request))
;auth;  (GET "/authenticated" request
;auth;       ;;(friend/authorize #{::user} (render-authenticated-page request))
;auth;      (render-authenticated-page request)
;auth;       )
  (GET "/" [] (index-page))
  (GET "/map.png" [] (response-biome-map))
  (GET ["/history/since/:event-id", :event-id #"[0-9]+"] [event-id] (history-since (read-string event-id)))
  (GET "/rest/add-user/:name"  [name] (add-user-request name))
  (GET "/rest/users"           []     (users-request))
  (GET "/rest/totalpop" [] (total-pop-request))
  (GET "/rest/settlements" [] (settlements-request))
  (GET "/rest/tribes"      [] (tribes-request))
  (GET ["/rest/tribe/:id/settlements", :id #"[0-9]+"]   [id] (tribe-settlements-request (read-string id)))
  (GET ["/useractions/disaster/:x/:y/:name", :x #"[0-9]+", :y #"[0-9]+"] [x y name] (disaster-request (read-string x) (read-string y) name))
;auth;  (friend/logout (ANY "/logout" request (ring.util.response/redirect "/")))
  (route/resources "/")
  (route/not-found "Page not found"))

(def friend-config
  {:allow-anon? true
   ;;:default-landing-uri "/"
   :login-uri "/github.callback" ;; triggers login
   :unauthorized-handler #(-> (h/html5 [:h2 "You do not have sufficient privileges to access " (:uri %)])
                              resp/response
                              (resp/status 401))
   :workflows [(oauth2wf/workflow
                {:client-config client-config
                 :uri-config uri-config
                 :config-auth config-auth
                 :access-token-parsefn oauth2u/get-access-token-from-params
                 })]})

(def app
  (if auth
    (-> main-routes
        (friend/authenticate friend-config)
        handler/site
        wrap-base-url)
  (-> main-routes
      handler/site
      wrap-base-url)))
