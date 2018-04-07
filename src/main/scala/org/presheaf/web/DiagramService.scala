package org.presheaf.web

import org.presheaf.{Diagram, DiagramSamples, OS}
import org.presheaf.web.HtmlSnippets._
import javax.servlet.http._

import scala.io.Source
import scala.util.parsing.json.JSON
import java.io.File

class DiagramService extends PresheafServlet {
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

  def produce(req:HttpServletRequest, diagram:String): String = {
    val d = process(req, diagram, req.getParameter("opt"))
    json(
      if (d.log.isEmpty) {
        Map(
          "id"      -> d.id,
          "source"   -> quote(d.source),
          "version"  -> version)
      }
      else {
        errorLog(d.log)
      }
    )
  }
  
  override def doPost(req: HttpServletRequest, res: HttpServletResponse) : Unit = {
    res.setContentType("application/json;charset=UTF-8")
    val content = Source.fromInputStream(req.getInputStream, "UTF-8").getLines.mkString("")
    try {
      JSON.parseFull(content) foreach { 
        case json: Map[String, Any] =>
          println(json)
          json.get("op") match {
            case Some("update") =>
              val value = json.get("value")
              println(s"an update: $value")
              value match {
                case Some(history: Map[_, _]) if history.nonEmpty =>
                  println(s"good update: $history")
                case other =>
                  println(s"bad update value $other, res=$res")
                  res.sendError(505, s"bad update value $other")
              }
            case other =>
              res.sendError(503, s"bad request $json")
          }
        case other => println(s"bad rq $other")
          res.sendError(504, s"bad request $other")
          
      }
      
    } catch {
      case bd: Diagram.Bad =>
        OS.log(s"Diagram post service: it did not work for $content: bd.getMessage")
        res.sendError(501, bd.getMessage)

      case e: Throwable   =>
        OS.log("Diagram post service: an exception")
        e.printStackTrace()
        res.getWriter.print(json(Map("error" -> e.getMessage)))
        res.sendError(502, "Error while processing the diagram: " + e.getMessage)
    }
  }

  override def doGet(req:HttpServletRequest, res:HttpServletResponse) : Unit = {
    res.setContentType("text/html")
    try {

      req.getParameter("op") match {
        case "samples" => res.getWriter.print(DiagramSamples.samples.map(produce(req, _)).mkString("[", ",\n", "]"))

        case "aspng" =>
          res.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY)
          res.setHeader("Location", ref(process(req).img))

        case "aspdf" =>
          res.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY)
          res.setHeader("Location", ref(process(req).pdf))

        case _ =>
         val result = produce(req, req.getParameter("in"))
         res.getWriter.print(result)
      }
    } catch {
      case bd: Diagram.Bad => 
        OS.log("Diagram service: bad diagram, " + bd.getMessage)
        res.sendError(503, bd.getMessage)
      
      case e: Throwable   => 
        OS.log("Diagram service: an exception")
        e.printStackTrace()
        res.getWriter.print(json(Map("error" -> e.getMessage)))
        res.sendError(504, "Error while processing the diagram: " + e.getMessage)
    }
  }
}