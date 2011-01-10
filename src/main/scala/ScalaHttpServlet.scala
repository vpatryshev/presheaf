package org.presheaf
import javax.servlet.http._

abstract class ScalaHttpServlet extends HttpServlet {

  val contentType = "text/html"

  def doGetXML(req:HttpServletRequest):scala.xml.Node

  final override def doGet(req:HttpServletRequest, res:HttpServletResponse) {
    res.setContentType("text/html")
    res.getWriter.print(doGetXML(req).toString)
  }

}
