(ns amindblowingworld.ui
  (:require [monet.canvas :as canvas]))


(defn draw-initial-world []
  (let [
         canvas-dom (.getElementById js/document "world")
         monet-canvas (canvas/init canvas-dom "2d")]

    (canvas/add-entity monet-canvas :background
                       (canvas/entity {:x 0 :y 0 :w 600 :h 600} ; val
                                      nil                       ; update function
                                      (fn [ctx val]             ; draw function
                                        (-> ctx
                                            (canvas/fill-style "#191d21")
                                            (canvas/fill-rect val)))))))

(defn doc-ready-handler []
  (let[ ready-state (. js/document -readyState)]
    (if (= "complete" ready-state)
      (do
        (js/alert "Hello AMindBlowingWorld!")
        (draw-initial-world)))))

(defn on-doc-ready []
  (aset  js/document "onreadystatechange" doc-ready-handler ))

(on-doc-ready)
