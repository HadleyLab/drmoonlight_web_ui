#!/bin/sh -e
rm -rf /etc/nginx/conf.d
mkdir /etc/nginx/conf.d
if [ -e /etc/letsencrypt/live/$1/fullchain.pem ]; then
    ln -s /etc/nginx/available/$1.conf /etc/nginx/conf.d/$1.conf
else
    ln -s /etc/nginx/available/cert_check_only.conf /etc/nginx/conf.d/cert_check_only.conf
fi
nginx -g "daemon off;"
