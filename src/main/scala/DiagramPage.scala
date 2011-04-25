package org.presheaf

import org.presheaf.DiagramRenderer._
import org.presheaf.HtmlSnippets._

import javax.servlet.http._
import scala.xml._

class DiagramPage extends PresheafServlet {
  val sample = "X \\ar@/_2pc/[rr]_{f;g} \\ar[r]^f &Y \\ar[r]^g &Z\\"

  def page(title: String, diagram: String, ns: Seq[Node]) = {
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
         <textarea name="in" cols="60" rows="10">{ Text(diagram) }</textarea>
         <input type="submit" value="Submit" />
       </form>
        <p>
          <a href="http://ctan.org/tex-archive/macros/generic/diagrams/xypic/xy/doc/xyguide.pdf">This pdf</a> tells in details how to write diagrams in xypic format.
        </p>

        <a href="samples">Samples</a><br/>

        { signature }
     </body>
   </html>
  }

  override def doGetXML(req:HttpServletRequest) = {
    configure(req.getSession.getServletContext)
    val format = notNull(req.getParameter("format"), "xy")
    val param = notNull(req.getParameter("in"), "")

    if (param.isEmpty) {
      page("Let's try a diagram",
           sample,
           <p>enter something below</p>)
    } else {
      val (diagram, imgRef, pdfRef, logs) = process(req)
      val img = <img/> % fileAsAttr("src", imgRef)
      val pdf = <a>pdf</a> % fileAsAttr("href", pdfRef)
      page("Here's your diagram",
           diagram,
           <p>"<b>{ diagram }</b>"</p>
           <p>{ img }</p>
           <p>{ pdf }</p>
           <p>{ logs }</p>)
    }
  }

}
