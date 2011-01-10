#!/bin/sh

FILE='src2pdf.war'
DOWNLOAD="download/$FILE"
APPDIR=/var/lib/tomcat6/webapps
APP="$APPDIR/$FILE"

while true; do

  if [ ! -f $APP ] || [ $DOWNLOAD -nt $APP ]; then
    cp $DOWNLOAD $APP
    echo `date` Done updating.
  fi
  sleep 30

done

