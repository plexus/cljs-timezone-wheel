(ns timezone-wheel.core
  (:require [reagent.core :as reagent :refer [atom]]
            [goog.events :as events]
            [goog.events.EventType :as EventType]))

(enable-console-print!)

(defonce app-state (atom {:rotation 0
                          :radius 150
                          :clock-wedges 24
                          :time-slices [{:name "awake-hours"
                                         :start 7
                                         :end 23}
                                        {:name "work-hours"
                                         :start 9
                                         :end 17}
                                        {:name "social-hours"
                                         :start 8
                                         :end 22}]
                          :locations [{:name "montreal"
                                       :hour 5}
                                      {:name "san francisco"
                                       :hour 2}
                                      {:name "berlin"
                                       :hour 10}]}))

(def floor (.-floor js/Math))
(def sin (.-sin js/Math))
(def cos (.-cos js/Math))
(def PI (.-PI js/Math))

(defn wedges->radians [total wedges]
  (let [wedge-size (/ PI total)]
    (+ PI (* wedges -2 wedge-size) (/ 3 total))))

(defn wedge-pos-x [r total wedges]
  (floor (* r (sin (wedges->radians total wedges)))))

(defn wedge-pos-y [r total wedges]
  (floor (* r (cos (wedges->radians total wedges)))))

(defn wheel-slices [radius time-slices clock-wedges]
  [:g
   (for [{:keys [name start end]} time-slices
         :let [r (- radius 10)
               x1 (wedge-pos-x r clock-wedges start)
               y1 (wedge-pos-y r clock-wedges start)
               x2 (wedge-pos-x r clock-wedges end)
               y2 (wedge-pos-y r clock-wedges end)
               large-arc-flag (if (> (- end start) (/ clock-wedges 2)) 1 0)]]
     [:path {:d (str "M " x1 " " y1 " A " r " " r " 0 " large-arc-flag " 1 " x2 " " y2 " L 0 0 Z")
             :class name
             :key name}])])

(defn wheel-numbers [radius clock-wedges]
  [:g
   (for [i (range clock-wedges)
         :let [theta (+ (* i -2 (/ PI clock-wedges)) PI)
               r (- radius 40)
               x (floor (* r (sin theta)))
               y (floor (* r (cos theta)))]]
     [:text {:key i :x x :y y} i])])

(defn wheel [radius time-slices clock-wedges rotation]
  [:g#wheel {:mask "url(#doughnut)" :style {:transform (str "rotateZ(" rotation "rad)")}}
   [wheel-slices radius time-slices clock-wedges]
   [wheel-numbers radius clock-wedges]])

(defn wheel-svg [radius time-slices clock-wedges rotation]
  [:svg {:viewBox "-300 -300 600 600"}
   [:defs
    [:mask#doughnut
     [:circle
      {:cx "0" :cy "0" :r "130" :stroke "white" :stroke-width "100"}]]]
   [wheel radius time-slices clock-wedges rotation]])

(defn wheel-locations [radius locations clock-wedges]
  [:div#locations
   (for [{:keys [hour name]} locations
         :let [r (- radius 15)
               theta (- (* 2 (/ PI clock-wedges) hour) (/ PI 2))
               x (* r (sin theta))
               y (* r (cos theta))
               theta (if (> theta (- PI (/ PI 2)))
                       (+ theta PI)
                       theta)]]
     [:div.location {:key name
                     :style {:top (str x "px")
                             :left (str y "px")
                             :transform (str "rotateZ(" theta "rad)")}}
      name])])

(defn wheel-box [{:keys [radius time-slices clock-wedges locations rotation]}]
  [:div#wheel-box
   [wheel-svg radius time-slices clock-wedges rotation]
   [wheel-locations radius locations clock-wedges]])

(defn render-app []
  (reagent/render [wheel-box @app-state]
                  (js/document.getElementById "app")))

(defn rotate-left [{:keys [rotation clock-wedges] :as state}]
  (update state :rotation #(- % (/ PI clock-wedges))))

(defn rotate-right [{:keys [rotation clock-wedges] :as state}]
  (update state :rotation #(+ % (/ PI clock-wedges))))

(defn event->key [evt]
  (.. evt getBrowserEvent -key))

(events/listen js/document
               EventType/KEYDOWN
               (fn [evt]
                 (case (event->key evt)
                   "ArrowLeft" (swap! app-state rotate-left)
                   "ArrowRight" (swap! app-state rotate-right)
                   nil)
                 (render-app)))

(render-app)
