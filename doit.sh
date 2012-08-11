# Generates images; pass file name without extension
HOME=/root
CACHE=$HOME/diagrams
NAME=$1
SRC=$NAME.src
TEX=$NAME.tex
EPS=$NAME.eps
DVI=$NAME.dvi
IMG=$NAME.png

grep -q -e "\\\\\\(tikz\\|draw\\|fill\\|filldraw\\|shade\\|path\\|node\\)" $CACHE/$SRC
if [ "$?" -eq "0" ]; then
  $HOME/dotikz.sh $1
else
  $HOME/doxy.sh $1
fi
