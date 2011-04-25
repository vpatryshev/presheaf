#!/bin/sh
mkdir -p uploads
VERFILE=src/main/resources/buildno.txt
expr `cat $VERFILE` + 1 > $VERFILE
VERSION=`cat $VERFILE`
SERVER="ec2-50-17-178-228.compute-1.amazonaws.com" 
SERVERHOME="ubuntu@$SERVER:/home/ubuntu"
KEYS="src2pdf20101223.pem"
SCP="scp -i $KEYS"
WARNAME=presheaf.war
WARFILE="build/$WARNAME"
UP="$SCP uploads/* $SERVERHOME/download"
GETLOGS="$SCP $SERVERHOME/tomcat/logs/* logs/"
echo `date`> "uploads/ready.flag"
ant war && cp $WARFILE archive/$WARNAME.$VERSION && cp $WARFILE uploads && $UP && cp $WARFILE /usr/share/tomcat/apache-tomcat-6.0.29/webapps/ && date && sleep 600 && $GETLOGS

