(ns ^:figwheel-no-load ui.prod
  (:require [ui.core :as core]))

(enable-console-print!)

(core/init! "https://mdmoonlight.com")
