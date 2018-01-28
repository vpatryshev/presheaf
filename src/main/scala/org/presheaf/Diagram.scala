package org.presheaf

import java.io.File

import scala.xml.Node

case class Diagram(
                    id: String,
                    source: String,
                    img: File,
                    pdf: File,
                    log: Iterable[Node] = Nil) {}

object Diagram {
  def bad(explanation: String): Nothing = throw new Bad(explanation)

  class Bad(explanation: String) extends RuntimeException(explanation) {}
}