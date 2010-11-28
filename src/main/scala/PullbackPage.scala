package org.pullback

import org.pullback.DiagramRenderer._
import javax.servlet.http._
import scala.xml._
import java.io._

class PullbackPage extends ScalaHttpServlet {
  def notNull(value: String, default: String) = if (value == null) default else value

  def fileAsAttr(attr: String, file: File) =
    new UnprefixedAttribute(attr, "cache/" + file.getName, Null)

  def page(title: String, ns: Seq[Node]) = {
    <html>
      <head>
        <title>{ title }</title>
      </head>
      <body>
        <h1>{ title }</h1>
        { ns }
       <form>
         <select name="format">
           <option value="xy">xypic</option>
           <option value="graph">graph</option>
           <option value="guess">ascii art</option>
         </select>
         <textarea name="diagram" width="40" height="10">{ Text("X \\ar@/_2pc/[rr]_{f;g} \\ar[r]^f &Y \\ar[r]^g &Z\\") }</textarea>
         <input type="submit" value="Submit" />
       </form>
     </body>
   </html>
  }

  def process(req:HttpServletRequest) = {
    val diagram = notNull(req.getParameter("diagram"), "")
    if (diagram.isEmpty) (diagram, null, null, null)
    else {
      val context = req.getSession.getServletContext
//      val workDir = new File(req.getSession().getServletContext().getRealPath("/"), "cache")
      val workDir = new File(context.getRealPath("cache"))
      val archive = new File(context.getRealPath("WEB-INF/classes/org/pullback/apps.tgz"))
      workDir.mkdirs
      val renderer = new DiagramRenderer(workDir)
      renderer.process(diagram)
    }
  }

  def doGetXML(req:HttpServletRequest) = {
    ensureAppPresence(req.getSession.getServletContext)

    val format = notNull(req.getParameter("format"), "xy")
    val (diagram, imgRef, pdfRef, logs) = process(req)

    if (diagram.isEmpty) {
      page("Let's try a diagram",
           <p>enter something below</p>)
    } else {
      val img = <img/> % fileAsAttr("src", imgRef)
      val pdfHref = <a>pdf</a> % fileAsAttr("href", pdfRef)

      page("Here's your diagram",
           <p>d "<b>{ diagram }</b>"</p>
           <p>{ img }</p>
           <p>{ pdfRef }</p>
           <p> { logs }</p>)
    }
  }
 }
