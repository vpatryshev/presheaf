package org.presheaf.web

import org.presheaf._
import org.presheaf.web._

import java.io._
import BuildInfo._

/**
 * Presheaf operations
 */

trait PresheafOps {
  def ref(file: File) = "cache/" + file.getName
  val xyError = ".*Xy-pic error:(.*)\\\\xyerror.*".r

  val Q = "\""
  def quote(s: String): String = Q + s.replaceAll(Q, "").replaceAll("\\\\", "\\\\\\\\").replaceAll("\n", "\\\\n") + Q
  def json(s: String): String = quote(s)
  def json(nvp: (String,_)): String = json(nvp._1) + ":" + json(nvp._2.toString)
  def json(map: Map[String, _]): String = map.map(json).mkString("{", ",", "}")
  def json(seq: Iterator[String]): String = seq.map(json).mkString("[", ",\n", "]")
  def json(seq: Iterable[String]): String = json(seq.iterator)

  def errorLog(log: Iterable[String]): Map[String, String] = {
    val fullLog = log.mkString("\n").replaceAll("\\\\", "\\\\").replaceAll("\"", "\\\"")
    OS.log(fullLog)
    fullLog match {
      case xyError(msg) =>
        Map(
          "error"     -> msg,
          "version"  -> version)
      case _ =>
        Map(
          "error"     -> fullLog,
          "version"  -> version)

    }
  }

  def process(dir: File, diagram: String) : Diagram = {
    require (diagram != null && diagram.nonEmpty, "no diagram to render")
    OS.log("Rendering diagram \"" + diagram + "\"")
    DiagramRenderer(dir).process(diagram)
  }
}