#!/bin/sh

SERVER=$1

ant war
echo scp -i src2pdf20101223.pem src2pdf.war $SERVER:/home/ubuntu/download/

scp -i src2pdf20101223.pem src2pdf.war $SERVER:/home/ubuntu/download/

