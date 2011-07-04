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

  def process(req:HttpServletRequest) : (String, String, File, File, Iterable[Node]) = {
    val diagram = req.getParameter("in")
//    val context = req.getSession.getServletContext
    val here = new File(req.getSession.getServletContext.getRealPath("x")).getParentFile
    if (!here.exists) throw new BadDiagram("Server error, here directory missing " + here.getAbsolutePath)
    val workDir = new File(here.getParentFile, "cache")
    if (!workDir.exists) throw new BadDiagram("Server error, work directory missing " + workDir.getAbsolutePath)
    if (!workDir.isDirectory) throw new BadDiagram("Server error, check work directory " + workDir.getAbsolutePath)
    val renderer = new DiagramRenderer(workDir)
    renderer.process(diagram, req.getParameter("opt"))
  }

  override def doGet(req : HttpServletRequest, res : HttpServletResponse) : Unit = {
    res.setContentType("text/html")
    val out:PrintWriter = res.getWriter()
    out.print(doGetXML(req).toString)
  }

  def doGetXML(req: HttpServletRequest) : Seq[Node] = sys.error("oops")
}