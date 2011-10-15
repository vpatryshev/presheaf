package org.presheaf

import org.presheaf.HtmlSnippets._
import javax.servlet.http._
import xml.Node
import java.io.File

class DiagramService extends PresheafServlet {
  val xyError = ".*Xy-pic error:(.*)\\\\xyerror.*".r

  val Q = "\""
  def quote(s: String) = Q + s.replaceAll(Q, "").replaceAll("\\\\", "\\\\\\\\") + Q
  def json(s: String): String = quote(s)
  def json(nvp: (String,_)): String = json(nvp._1) + ":" + json(nvp._2.toString)
  def json(map: Map[String, _]): String = map.map(json(_)).mkString("{", ",", "}")
  def json(seq: Iterator[String]): String = seq.map(json).mkString("[", ",\n", "]")
//  def json(seq: Iterator[Map[String, _]]): String = seq.map(json).mkString("[", ",\n", "]")
  def json(seq: Iterable[String]): String = json(seq.iterator)

  def errorLog(logs: Iterable[Node]) = {
    val fullLog = logs.mkString("<br/>").replaceAll("\\\\", "\\\\").replaceAll("\"", "\\\"")
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

  def produce(req:HttpServletRequest, diagram:String): String = {
    //return Map("error" -> "Sorry, this is broken now, working on it - Vlad, 1:05pm (PDT), 10/14/2011")
    val (id, source, img, pdf, logs) : (String, String, File, File, Iterable[Node]) = process(req, diagram, req.getParameter("opt"))
    json(
      if (logs.isEmpty) {
        Map(
          "id"      -> id,
          "source"   -> quote(source),
          "version"  -> version)
      }
      else {
        errorLog(logs)
      }
    )
  }

  override def doGet(req:HttpServletRequest, res:HttpServletResponse) : Unit = {
    res.setContentType("text/html")
    try {

      req.getParameter("op") match {
        case "samples" => res.getWriter.print(DiagramSamples.samples.map(produce(req, _)).mkString("[", ",\n", "]"))

        case "aspng" =>
          res.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY)
          res.setHeader("Location", ref(process(req)._3))

        case "aspdf" =>
          res.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY)
          res.setHeader("Location", ref(process(req)._4))

        case _ =>
         val result = produce(req, req.getParameter("in"))
         res.getWriter.print(result)
      }
    } catch {
      case bd: BadDiagram => {
        println("Diagram service: bad diagram, " + bd.getMessage)
        res.sendError(500, bd.getMessage)
      }
      case e: Throwable   => {
        println("Diagram service: an exception")
        e.printStackTrace
        res.getWriter.print(json(Map("error" -> e.getMessage)))
        res.sendError(500, "Error while processing the diagram: " + e.getMessage)
      }
    }
  }
}