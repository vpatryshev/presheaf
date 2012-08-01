# Generates images; pass file name without extension
HOME=.
CACHE=$HOME/diagrams
NAME=$1
TEX=$NAME.tex
EPS=$NAME.eps
DVI=$NAME.dvi
IMG=$NAME.png
cd $CACHE
. ../templates/xy $NAME >$TEX
latex $TEX && dvips -E -o $EPS $DVI && epstopdf -exact $EPS && dvipng -T tight -o $IMG $DVI
rm $EPS $DVI $NAME.log $NAME.aux
