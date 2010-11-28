package org.pullback

import actors.Futures
import actors.Futures._
import java.io.{InputStream, BufferedOutputStream, File, FileOutputStream, PrintWriter}
import javax.servlet.http._
import scala.xml._

class PullbackService extends HttpServlet {
  def notNull(value: String, default: String) = if (value == null) default else value

  def ref(file: File) = "cache/" + file.getName

  def fileAsAttr(attr: String, file: File) =
    new UnprefixedAttribute(attr, ref(file), Null)

  def page(ns: Seq[Node]) = {
    <html>
      <head>
        <title>Formatting Your Diagram AAS ver.2</title>
      </head>
      <body>
        { ns }
     </body>
   </html>
  }

  def process(req:HttpServletRequest) = {
    val diagram = notNull(req.getParameter("diagram"), "")
    val out = notNull(req.getParameter("out"), "")
    val workDir = new File(req.getSession().getServletContext().getRealPath("/"), "cache")
    workDir.mkdirs
    val renderer = new DiagramRenderer(workDir)
    renderer.process(diagram)
  }

  def doGetXML(req:HttpServletRequest) = {

    val format = notNull(req.getParameter("format"), "xy")

    val (diagram, imgRef, pdfRef, logs) = process(req)
    val img = <img/> % fileAsAttr("src", imgRef)
    val pdfHref = <a>pdf</a> % fileAsAttr("href", pdfRef)

      page(<p>{ img }</p>
           <p>{ pdfHref }</p>
           <p> { logs }</p>)
  }

  final override def doGet(req:HttpServletRequest, res:HttpServletResponse) : Unit = {
    res.setContentType("text/html")
    res.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY)
    val out = if ("pdf" == req.getParameter("out")) process(req)._3 else process(req)._2
    res.setHeader("Location", ref(out))
  }

//  override def init():Unit = {
//    super.init
//    println("=====================PullbackService plain init=====================")
//  }
//  override def init(sc:javax.servlet.ServletConfig):Unit = {
//    super.init(sc)
//    println("=====================PullbackService alt init=====================")
//  }
  override def service(req:javax.servlet.http.HttpServletRequest,
              resp:javax.servlet.http.HttpServletResponse) =
                super.service(req, resp):Unit ;

//  override def service(req:javax.servlet.ServletRequest,
//              resp:javax.servlet.ServletResponse):Unit =
//                super.service(req,resp);
 }