(ns ui.widgets.header-logo
  (:require
   [ui.db.misc :refer [>event]]
   [soda-ash.core :as sa]))

(defn HeaderLogo []
  [sa/Header {:class-name "header__logo" :on-click (>event [:goto "/"])} "Dr. Moonlight"])
