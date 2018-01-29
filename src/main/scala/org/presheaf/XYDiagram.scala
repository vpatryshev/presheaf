package org.presheaf

/**
 * Incorporates xy-diagram-related functionality.
 */

class XYDiagram(source: String) {

}

object XYDiagram {

  val prefix =
"""\documentclass[12pt,notitlepage]{article}
\""" +
"""usepackage[all]{xy}

\begin{document}

\thispagestyle{empty}

\[\xymatrix{
"""

  val suffix = "\n}\\]\\end{document}\n"

  def buildTex(s: String) = prefix + s + suffix
  
}