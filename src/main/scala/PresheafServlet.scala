package org.presheaf

import javax.servlet.http._
import scala.xml._
import java.io._

/**
 * General class for all our (three) servlets
 */

abstract class PresheafServlet extends ScalaHttpServlet {
  def notNull(value: String, default: String) = if (value == null) default else value

  def ref(file: File) = "cache/" + file.getName

  def fileAsAttr(attr: String, file: File) = new UnprefixedAttribute(attr, ref(file), Null)

  def process(req:HttpServletRequest) = {
    val diagram = req.getParameter("in")
//    val context = req.getSession.getServletContext
    val workDir = new File(req.getSession.getServletContext.getRealPath(".."), "cache")
    workDir.mkdirs
    val renderer = new DiagramRenderer(workDir)
    renderer.process(diagram, req.getParameter("opt"))
  }
}