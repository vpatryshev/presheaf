package org.presheaf

import javax.servlet.http._
import scala.xml._
import java.io._
import collection.mutable.ListBuffer

/**
 * General class for all our (three) servlets
 */

abstract class PresheafServlet extends HttpServlet {
  def notNull(value: String, default: String) = if (value == null) default else value

  def ref(file: File) = "cache/" + file.getName

  def fileAsAttr(attr: String, file: File) = new UnprefixedAttribute(attr, ref(file), Null)

  def process(req:HttpServletRequest) : (String, File, File, Iterable[Node]) = {
    val diagram = req.getParameter("in")
//    val context = req.getSession.getServletContext
    val workDir = new File(req.getSession.getServletContext.getRealPath(".."), "cache")
    System.out.println("processing \"" + diagram + "\", will store in " + workDir.getAbsolutePath)
    workDir.mkdirs
    val renderer = new DiagramRenderer(workDir)
    renderer.process(diagram, req.getParameter("opt"))
  }

  override def doGet(req : HttpServletRequest, res : HttpServletResponse) : Unit = {
    res.setContentType("text/html")
    val out:PrintWriter = res.getWriter()
    out.print(doGetXML(req).toString)
  }

  def doGetXML(req: HttpServletRequest) : Seq[Node] = error("oops")
}