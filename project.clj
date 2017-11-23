(defproject ui "0.1.0-SNAPSHOT"
  :description "drmoonlight web ui"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.9.0-RC1"]
                 [org.clojure/clojurescript "1.9.946" :scope "provided"]

                 [cljsjs/react-with-addons "15.5.4-0"]
                 [reagent "0.7.0" :exclusions [cljsjs/react]]
                 [re-frame "0.10.2" :exclusions [cljsjs/react]]
                 [reagent-utils "0.2.1" :exclusions [cljsjs/react]]
                 [re-frisk "0.5.0" :exclusions [cljsjs/react]]
                 [binaryage/devtools "0.9.7"]
                 [hiccup "1.0.5"]
                 [garden "1.3.3"]
                 [route-map "0.0.5"]
                 [com.andrewmcveigh/cljs-time "0.5.2"]
                 [soda-ash "0.76.0" :exclusions [cljsjs/react]]
                 [cljsjs/react-datepicker "0.55.0-0" :exclusions [cljsjs/react]]]

  :plugins [[lein-environ "1.1.0"]
            [lein-cljsbuild "1.1.7"]
            [lein-ancient "0.6.14"]
            [cider/cider-nrepl "0.15.1"]]

  :jvm-opts ["--add-modules" "java.xml.bind"]
  :min-lein-version "2.5.0"

  :clean-targets ^{:protect false}
  [:target-path
   [:cljsbuild :builds :app :compiler :output-dir]
   [:cljsbuild :builds :app :compiler :output-to]]

  :source-paths ["srcc"]
  :resource-paths ["resources"]

  :figwheel
  {:http-server-root "public"
   :server-port 3000
   :nrepl-port 7003
   :nrepl-middleware ["cemerick.piggieback/wrap-cljs-repl"
                      "cider.nrepl/cider-middleware"]
   :css-dirs ["resources/public/css"]}


  :profiles {:dev {:repl-options {:init-ns ui.repl
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

                   :dependencies [[ring/ring-mock "0.3.1"]
                                  [ring/ring-devel "1.6.2"]
                                  [prone "1.1.4"]
                                  [figwheel-sidecar "0.5.14"]
                                  [org.clojure/tools.nrepl "0.2.13"]
                                  [com.cemerick/piggieback "0.2.2-SNAPSHOT"]
                                  [pjstadig/humane-test-output "0.8.3"]]

                   :source-paths ["srcs" "srcc" "env/dev/clj"]
                   :plugins [[lein-figwheel "0.5.14"]]

                   :env {:dev true}

                   :cljsbuild
                   {:builds
                    {:ui {:source-paths ["srcs" "srcc" "env/dev/cljs"]
                          :compiler
                          {:main "ui.dev"
                           :asset-path "/js/out"
                           :output-to "resources/public/js/ui.js"
                           :output-dir "resources/public/js/out"
                           :source-map true
                           :optimizations :none
                           :pretty-print  true

                           ;; :foreign-libs []

                           ;; :externs []
                           ;; :externs []

                           }}}}}

             :prod {:cljsbuild

                    {:builds
                     {:ui {:source-paths ["srcs" "env/prod/cljs"]
                           :verbose true
                           :compiler
                           {:main "ui.prod"
                            :verbose true
                            ;; :foreign-libs []
                            ;; :externs []
                            :output-to "build/js/ui.js"
                            ;; :output-dir "build/js/out"
                            :optimizations :advanced
                            :pretty-print  false}}}}}})
