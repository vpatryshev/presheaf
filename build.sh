#!/bin/sh
set -euo 
#pipefail

VERFILE=src/main/resources/buildno.txt
expr `cat $VERFILE` + 1 > $VERFILE

sbt deploy

