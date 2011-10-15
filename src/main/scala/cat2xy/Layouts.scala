package org.presheaf

object Layouts {
  def walkDiagonally(width: Int, height: Int) = {
    for (i <- 0 to width + height - 1;
         j <- 0 to width + height - 1) yield (i, j)
  }
}