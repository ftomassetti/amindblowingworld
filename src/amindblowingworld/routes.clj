(ns amindblowingworld.routes
  (:use compojure.core
        amindblowingworld.views
        amindblowingworld.rest
        amindblowingworld.useractions
        ;; amindblowingworld.view_index
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

(def auth false)

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
   :callback {:domain "http://amindblowingworld.clojurecup.com" :path "/"}
   })

(def uri-config
  {:authentication-uri {:url "https://github.com/login/oauth/authorize"
                        :query {:client_id (:client-id client-config)
                                :response_type "code"
                                :redirect_uri (oauth2u/format-config-uri client-config)
                                :scope ""}}
   :access-token-uri {:url "https://github.com/login/oauth/access_token"
                      :query {:client_id (:client-id client-config)
                              :client_secret (:client-secret client-config)
                              :grant_type "authorization_code"
                              :redirect_uri (oauth2u/format-config-uri client-config)}}})

(def config-auth {:roles #{::user}})

(defroutes main-routes
  (GET "/" [req] (h/html5
                  [:head
                   [:title "AMindBlowingWorld"]
                   (h/include-js "/js/main.js")
                   (h/include-js "/js/app.js")]
                  (if auth
                    [:body (if (friend/identity req)
                             {:onload "initApp();"}
                             {})
                     [:h1 "AMindBlowingWorld App"]
                     [:h2 "Clojurecup 2014"]
                     [:h3 "Authentication via GitHub using OAuth2"]
                     [:p "Current Status (this will change when you log in/out):"]
                     (if-let [identity (friend/identity req)]
                       [:p "Logged in as GitHub user "
                        [:strong (get-github-handle (:current identity))]
                        " with GitHub OAuth2 access token " (:current identity)]
                       [:p [:a {:href "github.callback"} "Login with GitHub"]])
                     [:h3 "App"]
                     (if (friend/identity req)
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
                    [:body {:onload "initApp();"}
                     [:h1 "AMindBlowingWorld App"]
                     [:h2 "Clojurecup 2014"]
                     [:h3 "Authentication via GitHub using OAuth2"]
                     [:p "Current Status (this will change when you log in/out):"]
                     [:p "development code "]
                     [:h3 "App"]
                     [:div#appDiv
                      [:div#world [:img#worldView {:src "/img/world.png"}]]
                      [:div#menu "Menu"]
                      [:div#news "News"]]
                     [:h3 "Logging out"]
                     [:p "Click here to log out" "."]
                     ]
                    )))

         ;; [:ul [:li (e/link-to (misc/context-uri req "role-user") "Requires the `user` role")]
        ;;  ;[:li (e/link-to (misc/context-uri req "role-admin") "Requires the `admin` role")]
        ;;  [:li (e/link-to (misc/context-uri req "requires-authentication")
        ;;         "Requires any authentication, no specific role requirement")]]
        ;; [:h3 "Logging out"]
        ;; [:p (e/link-to (misc/context-uri req "logout") "Click here to log out") "."])))
  (GET "/logout" req
    (friend/logout* (resp/redirect (str (:context req) "/"))))
  ;; (GET "/requires-authentication" req
  ;;   (friend/authenticated "Thanks for authenticating!"))
  ;; (GET "/role-user" req
  ;;   (friend/authorize #{::users/user} "You're a user!"))
  ;; #_(GET "/role-admin" req
  ;;   (friend/authorize #{::users/admin} "You're an admin!"))
  (GET "/map.png" [] (response-biome-map))
  (GET ["/history/since/:event-id", :event-id #"[0-9]+"] [event-id] (history-since (read-string event-id)))
  (GET "/rest/totalpop" [] (total-pop-request))
  (GET "/rest/settlements" [] (settlements-request))
  (GET "/rest/tribes"      [] (tribes-request))
  (GET ["/useractions/disaster/:x/:y", :x #"[0-9]+", :y #"[0-9]+"] [x y] (disaster-request (read-string x) (read-string y)))
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (if auth
    (-> main-routes
        (friend/authenticate
         {:allow-anon? true
          :default-landing-uri "/"
          :login-uri "/github.callback"
          :unauthorized-handler #(-> (h/html5 [:h2 "You do not have sufficient privileges to access " (:uri %)])
                                     resp/response
                                     (resp/status 401))
          :workflows [(oauth2wf/workflow
                       {:client-config client-config
                        :uri-config uri-config
                        :config-auth config-auth
                        ;;:access-token-parsefn oauth2u/get-access-token-from-params
                        :access-token-parsefn #(-> % :body codec/form-decode (get "access_token"))
                        })]})
        handler/site
        wrap-base-url)
  (-> main-routes
      handler/site
      wrap-base-url)))
