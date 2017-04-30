# Generates pdf and png from xypic; pass file name without extension
# temporary name is dodoxy.sh; will be renamed back soon

. /home/ubuntu/instance

NAME=$1
TEX=$NAME.tex
EPS=$NAME.eps
DVI=$NAME.dvi
IMG=$NAME.png
cd $CACHE

rm -f $TEX $EPS $DVI $IMG
chmod a+r $NAME

. ../templates/xy $NAME >$TEX
chmod a+r $TEX

/usr/bin/latex $TEX 
rlatex=$?

if [ $rlatex != 0 ]; then 
  echo "latex returned $rlatex"
  exit $rlatex
fi

echo "ok, ok"

/usr/bin/dvips -E -o $EPS $DVI && /usr/bin/epstopdf $EPS && /usr/bin/dvipng -T tight -o $IMG $DVI
r=$?

rm -f $EPS $DVI $NAME.log $NAME.aux
exit $r
