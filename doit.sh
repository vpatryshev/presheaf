# Generates images; pass file name without extension
. instance
NAME=$1
SRC=$NAME.src
TEX=$NAME.tex
EPS=$NAME.eps
DVI=$NAME.dvi
IMG=$NAME.png

sudo chmod a+r $SRC

grep -q -e "\\\\\\(tikz\\|draw\\|fill\\|filldraw\\|shade\\|path\\|node\\)" $CACHE/$SRC
if [ "$?" -eq "0" ]; then
  $INSTANCE_HOME/dotikz.sh $1
else
  $INSTANCE_HOME/doxy.sh $1
fi
