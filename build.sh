#!/bin/sh
set -euo 
#pipefail

mkdir -p uploads
mkdir -p archive
VERFILE=src/main/resources/buildno.txt
expr `cat $VERFILE` + 1 > $VERFILE
VERSION=`cat $VERFILE`
SERVER="50.57.81.82" 
SERVERHOME="root@$SERVER:/root"
KEYS="src2pdf20101223.pem"
SCP="scp"
WARNAME=presheaf.war
WARFILE="build/$WARNAME"
UP="$SCP uploads/* $SERVERHOME/download"
GETLOGS="$SCP $SERVERHOME/tomcat/logs/* logs/"
WEBAPPS="/var/lib/tomcat6/webapps"
echo `date`> "uploads/ready.flag"
ant war 
cp $WARFILE archive/$WARNAME.$VERSION 
#echo "ok1"
cp $WARFILE $WEBAPPS/ROOT.war
#echo "ok2"
#sudo cp -r build/presheaf/WEB-INF $WEBAPPS/ROOT/
#echo "ok3"
#chmod a+r $WEBAPPS/ROOT.war
cp $WARFILE uploads
echo "got version `cat src/main/resources/buildno.txt`"
echo "presheaf-06062011-256MlU04JcS1o"
$UP 
date 
sleep 600 
$GETLOGS

