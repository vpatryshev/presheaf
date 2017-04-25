#!/bin/sh
set -euo 
#pipefail


mkdir -p uploads
mkdir -p archive
VERFILE=src/main/resources/buildno.txt
VERSION=`cat $VERFILE`
SERVER="50.57.81.82" 
SERVERHOME="root@$SERVER:/root"
KEYS="src2pdf20101223.pem"
SCP="scp"
WARFILE=$1
WARNAME=${WARFILE##*/}
UP="$SCP uploads/* $SERVERHOME/download"
#echo "Have file $WARFILE, will $UP"
GETLOGS="$SCP $SERVERHOME/tomcat/logs/* logs/"
WEBAPPS="/var/lib/tomcat6/webapps"
echo `date`> "uploads/ready.flag"
cp $WARFILE archive/$WARNAME.$VERSION
cp $WARFILE uploads/presheaf.war
echo "got version `cat src/main/resources/buildno.txt`"
echo "presheaf-06062011-256MlU04JcS1o"
$UP 
#date 
#sleep 600 
#$GETLOGS

