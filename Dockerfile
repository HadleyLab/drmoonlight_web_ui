FROM clojure:lein-alpine
RUN mkdir -p /app
WORKDIR /app
ADD project.clj ./
RUN lein deps
ADD . ./
RUN lein with-profile prod cljsbuild once ui
RUN cat resources/public/index.html |  sed "s/CACHE_ID/`cat build/js/ui.js | md5sum`/" > build/index.html
