# Generates pdf and png from xypic; pass file name without extension
# temporary name is dodoxy.sh; will be renamed back soon
. /home/ubuntu/instance

NAME=$1
TEX=$NAME.tex
EPS=$NAME.eps
DVI=$NAME.dvi
IMG=$NAME.png
cd $CACHE
. ../templates/xy $NAME >$TEX
latex $TEX && dvips -E -o $EPS $DVI && epstopdf $EPS && dvipng -T tight -o $IMG $DVI
rm $EPS $DVI $NAME.log $NAME.aux
