FROM nginx:alpine
RUN mkdir -p /data/nginx/cache/
WORKDIR /etc/nginx
RUN rm ./conf.d/*
RUN mkdir ./available
ADD ./conf.d/* ./available/
ADD dockerentrypoint.sh ./dockerentrypoint.sh
ADD build ./html
VOLUME ["/etc/letsencrypt", "/data/acme"]
ENTRYPOINT ["./dockerentrypoint.sh"]
