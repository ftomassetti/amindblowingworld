(defproject amindblowingworld "0.1.0-SNAPSHOT"
  :description "ACreativeTeamName's creation"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License" :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
    [org.clojure/clojure "1.6.0"]
    [org.clojure/clojurescript "0.0-2197" :exclusions [org.apache.ant/ant]]
    [compojure "1.1.6"]
    [hiccup "1.0.4"]]
  :plugins [
    [lein-cljsbuild "1.0.3"]
    [lein-ring "0.8.7"]]
  :cljsbuild {
    :builds [{:source-paths ["src-cljs"]
    :compiler {:output-to "resources/public/js/main.js" :optimizations :whitespace :pretty-print true}}]}
  :ring {:handler amindblowingworld.routes/app})
