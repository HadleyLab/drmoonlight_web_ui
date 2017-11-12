(ns ui.styles
  (:require [garden.core :as garden]
            [garden.units :as units]))

(defn style [css]
  [:style (garden/css css)])

(def basic-style
  (garden/css []))


