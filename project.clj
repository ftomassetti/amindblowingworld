(defproject amindblowingworld "0.1.0-SNAPSHOT"
  :description "ACreativeTeamName's creation"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License" :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
    [org.clojure/clojure "1.5.1"]
    [org.clojure/clojurescript "0.0-2173" :exclusions [org.apache.ant/ant]]
    [rm-hull/monet "0.2.1"]
    [compojure "1.1.6"]
    [hiccup "1.0.4"]
    [com.github.lands/lands-java-lib "0.3-SNAPSHOT"]
    [org.clojure/math.combinatorics "0.0.8"]]
  :plugins [
    [lein-cljsbuild "1.0.2"]
    [lein-ring "0.8.7"]]
  :cljsbuild {
    :builds [{:source-paths ["src-cljs"]
    :compiler {:output-to "resources/public/js/main.js" :optimizations :whitespace :pretty-print true}}]}
  :ring {
    :handler amindblowingworld.routes/app
    :auto-reload? true})
