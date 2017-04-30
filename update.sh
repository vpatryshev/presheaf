#!/bin/sh

export HOMEDIR=/home/ubuntu
export JAVA_HOME=/usr/share/java
export FILE='presheaf.war'
export DLDIR="$HOMEDIR/download"
export DOWNLOAD="$DLDIR/$FILE"
export TOMCAT=/opt/tomcat

export UPFILE="DLDIR/update.sh"
export FLAGFILE="$DLDIR/ready.flag"
export CATALINA_BASE=$TOMCAT
export APPDIR="$CATALINA_BASE/webapps"
export ROOTAPP="$APPDIR/ROOT.war"
export ROOTAPPDIR="$APPDIR/ROOT"

while true; do

  if [ -f $FLAGFILE ]; then
    rm $FLAGFILE
    echo Found $DOWNLOAD and $FLAGFILE
    echo "Removing old $ROOTAPP"
    sudo rm -rf $ROOTAPP
    sudo cp $DOWNLOAD $ROOTAPP
    echo "now we have deployed $ROOTAPP ... "
    ls -l $ROOTAPP
    sudo service tomcat stop
    sudo rm -rf $ROOTAPPDIR
    sudo service tomcat start
    sleep 60
    
    sudo cd tomcat/webapps/ROOT
    sudo ln -s catalina.out ../../logs/catalina.out
    sudo chmod a+r,a+w,a+x catalina.out

    echo `date` Done updating.

    if [ -f $UPFILE ]; then
      echo "./update.sh" >callme.sh && chmod u+x callme.sh && mv -f $UPFILE ~
      ./callme.sh
    fi
  fi
  echo "`d