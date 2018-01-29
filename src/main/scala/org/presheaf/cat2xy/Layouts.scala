package org.presheaf.cat2xy

object Layouts {
  def walkDiagonally(width: Int, height: Int) = {
    for {i <- 0 until width + height
         j <- 0 until width + height} yield (i, j)
  }
}