package org.presheaf

import java.io.File

class Diagram(
              val id: String,
              val source: String,
              val img: File,
              val pdf: File,
              val log: Iterable[String] = Nil) {
  override def toString: String =
    s"Diagram($id, $source, $img, $pdf, ${log.mkString("\n")})"
}

object Diagram {
  
    
  def bad(explanation: String): Nothing = throw new Bad(explanation)

  class Bad(explanation: String) extends RuntimeException(explanation) {}
}