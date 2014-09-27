(ns amindblowingworld.ui)

(defn worldmap-dom []
  (.getElementById js/document "worldView"))

(defn update-world-map []
  ((worldmap-dom) "src" (str "/map.png?rand=" (js/Math.random))))

(defn init-ui []
    (js/setInterval update-world-map 1000))

(defn doc-ready-handler []
  (let[ ready-state (. js/document -readyState)]
    (if (= "complete" ready-state)
      (do
        (js/alert "Welcome to AMindBlowingWorld!")
        (init-ui)))))

(defn on-doc-ready []
  (aset  js/document "onreadystatechange" doc-ready-handler ))

;(on-doc-ready)
