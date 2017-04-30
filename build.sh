#!/bin/sh
set -euo 
#pipefail

VERFILE=src/main/resources/buildno.txt
expr `head -1 $VERFILE` + 1 > $VERFILE
date >> $VERFILE

sbt deploy

