#!/bin/sh

export HOMEDIR=/home/ubuntu
export JAVA_HOME=/usr/share/java
export FILE='presheaf.war'
export DLDIR="$HOMEDIR/download"
export DOWNLOAD="$DLDIR/$FILE"

export UPFILE="DLDIR/update.sh"
export FLAGFILE="$DLDIR/ready.flag"
export CATALINA_BASE=/var/lib/tomcat6
export APPDIR="$CATALINA_BASE/webapps"
export ROOTAPP="$APPDIR/ROOT.war"
export ROOTAPPDIR="$APPDIR/ROOT"

while true; do

  if [ -f $FLAGFILE ]; then
    rm $FLAGFILE
    echo Found $DOWNLOAD and $FLAGFILE
    echo "Removing old $ROOTAPP"
    rm $ROOTAPP
    cp $DOWNLOAD $ROOTAPP
    echo "now we have deployed $ROOTAPP ... "
    ls -l $ROOTAPP
    /usr/share/tomcat6/bin/shutdown.sh
    rm -rf $ROOTAPPDIR
    /usr/share/tomcat6/bin/startup.sh
    sleep 60
    cd tomcat/webapps/ROOT
    ln -s catalina.out ../../logs/catalina.out
    echo `date` Done updating.

    if [ -f $UPFILE ]; then
      echo "./update.sh" >callme.sh && chmod u+x callme.sh && mv -f $UPFILE ~
      ./callme.sh
    fi
  fi
  echo "Waiting for a file..."
  sleep 31
done

