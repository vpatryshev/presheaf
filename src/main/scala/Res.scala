package org.presheaf

object Res {
  def stream(resource: String) = getClass.getResourceAsStream(resource)
  def read(resource: String) = scala.io.Source.fromInputStream(stream(resource))
}