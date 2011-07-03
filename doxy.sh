# Generates images; pass file name without extension
HOME=/root
CACHE=$HOME/diagrams
NAME=$1
TEX=$NAME.tex
EPS=$NAME.eps
DVI=$NAME.dvi
IMG=$NAME.png
cd $CACHE
latex $TEX && dvips -E -o $EPS $DVI && epstopdf $EPS && dvipng -T tight -o $IMG $DVI
rm $EPS $DVI $NAME.log $NAME.aux
#ln -s $NAME.tex cache/$NAME.tex
#ln -s $NAME.pdf cache/$NAME.pdf
#ln -s $NAME.png cache/$NAME.png

