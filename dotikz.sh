# Generates images; pass file name without extension
HOME=.
CACHE=$HOME/diagrams
NAME=$1
SRC=$NAME.src
TEX=$NAME.tex
PNG=$NAME.png
PDF=$NAME.pdf
cd $CACHE
. ../templates/tikz $NAME > $TEX
pdflatex --jobname=$NAME $TEX && convert -density 300 $PDF $PNG

rm $EPS $DVI $NAME.log $NAME.aux >/dev/null 2>&1
