package org.presheaf.web

import javax.servlet.http._
import org.presheaf._
import org.presheaf.web._

import java.io._
import Diagram._

/**
 * General class for all our (three) servlets
 */

abstract class PresheafServlet extends HttpServlet with PresheafOps {


  def process(req:HttpServletRequest, diagram: String) : Diagram = {
    require(diagram != null && diagram.nonEmpty, "no diagram to render")
    OS.log("Rendering diagram \"" + diagram + "\"")
    val dir = wd(req)
    process(dir, diagram)
  }

  def wd(req:HttpServletRequest) = {
    val here = new File(req.getSession.getServletContext.getRealPath("x")).getParentFile
    if (!here.exists) bad("Server error, here directory missing " + here.getAbsolutePath)
    val workDir = new File(here.getParentFile, "cache")
    if (!workDir.exists) bad("Server error, work directory missing " + workDir.getAbsolutePath)
    if (!workDir.isDirectory) bad("Server error, check work directory " + workDir.getAbsolutePath)
    workDir
  }

  override def doGet(req : HttpServletRequest, res : HttpServletResponse) : Unit = {
    sys.error("oops")
  }
}