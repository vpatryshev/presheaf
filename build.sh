#!/bin/sh

set -euo 
#pipefail
printf '\33c\e[3J'
VERFILE=src/main/resources/buildno.txt
expr `head -1 $VERFILE` + 1 > $VERFILE
date >> $VERFILE

sbt clean deploy

