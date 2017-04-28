#!/bin/sh
set -euo 
#pipefail

source instance

mkdir -p uploads
mkdir -p archive
VERFILE=src/main/resources/buildno.txt
VERSION=`cat $VERFILE`
#SERVER="50.57.81.82" 
#SERVERHOME="root@$SERVER:/root"
#KEYS="src2pdf20101223.pem"
WARFILE=$1
WARNAME=${WARFILE##*/}
UP="$SCP uploads/*"
#echo "Have file $WARFILE, will $UP"
GETLOGS="$SCP $HOMETHERE/tomcat/logs/* logs/"
echo `date`> "uploads/ready.flag"
cp $WARFILE archive/$WARNAME.$VERSION
cp $WARFILE uploads/presheaf.war
echo "got version `cat src/main/resources/buildno.txt`"
#echo "presheaf-06062011-256MlU04JcS1o"
$SCP uploads/*.war
$SCP uploads/ready.flag
date 
#sleep 600 
#$GETLOGS

