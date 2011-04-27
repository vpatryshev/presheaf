package org.presheaf

import org.presheaf.HtmlSnippets._
import javax.servlet.http._
import xml.Node
import java.io.File

class DiagramService extends PresheafServlet {

  def page(diagram: String, imgRef: String, pdfRef: String, logs: Seq[Node]) = {
    <xml>
      <diagram>
        <source>{  diagram }</source>
        <image>{   imgRef  }</image>
        <pdf>{     pdfRef  }</pdf>
        <logs>{    logs    }</logs>
        <version>{ version }</version>
      </diagram>
    </xml>
  }

  val Q = "\""
  def quote(s: String) = Q + s.replaceAll(Q, "") + Q
  def attr(nvp: (String,Object)) = nvp._1 + ":" + quote(nvp._2.toString)
  def json(map: Map[String, Object]) = map.map(attr _).mkString("{", ",", "}")

  override def doGet(req:HttpServletRequest, res:HttpServletResponse) : Unit = {
    res.setContentType("text/html")
    try {
      val (diagram, img, pdf, logs) : (String, File, File, Iterable[Node]) = process(req)
      req.getParameter("out") match {
        case "png" =>
          res.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY)
          res.setHeader("Location", ref(img))

        case "pdf" =>
          res.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY)
          res.setHeader("Location", ref(pdf))
        case _ =>
          res.getWriter.print(json(
            Map("source"  -> diagram,
                "image"   -> ref(img),
                "pdf"     -> ref(pdf),
                "logs"    -> (logs map (_.toString)),
                "version" -> version)))}
    } catch {
      case bd: BadDiagram => res.sendError(500, bd.getMessage)
      case e: Throwable   => res.sendError(500, "Error while processing the diagram: " + e.getMessage)
    }
  }
}