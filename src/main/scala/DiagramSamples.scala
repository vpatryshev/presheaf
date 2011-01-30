package org.presheaf

import org.presheaf.DiagramRenderer._
import org.presheaf.HtmlSnippets._
import javax.servlet.http._
import scala.xml._
import java.io._

class DiagramSamples extends ScalaHttpServlet {

  def fileAsAttr(attr: String, file: File) =
    new UnprefixedAttribute(attr, "cache/" + file.getName, Null)

  def page(title: String) = {
    <html>
      <head>
        <title>{ title }</title>
      </head>
      <body>
        <h1>{ title }</h1>

        <p>{ samples }</p>

        <p>
          <a href="http://ctan.org/tex-archive/macros/generic/diagrams/xypic/xy/doc/xyguide.pdf">This pdf</a> tells in details how to write diagrams in xypic format.
        </p>
        { signature }
     </body>
   </html>
  }

  def process(req:HttpServletRequest) = {
    val diagram = req.getParameter("in")
//    val context = req.getSession.getServletContext
    val workDir = new File(req.getSession.getServletContext.getRealPath(".."), "cache")
    workDir.mkdirs
    val renderer = new DiagramRenderer(workDir)
    renderer.process(diagram, req.getParameter("opt"))
  }

  def doGetXML(req:HttpServletRequest) = {
    configure(req.getSession.getServletContext)
    page("XY Diagram Samples")
  }

  import org.presheaf.HtmlSnippets._

  def samples() = {
    val samples = Array(
      "X \\ar@/_2pc/[rr]_{f;g} \\ar[r]^f &Y \\ar[r]^g &Z\\",
      "X \\ar[r]^f &Y \\ar[r]^g &Z",
      "T: X \\ar@{|->}[r] &Y\\",
      "F: (f: A \\ar[r] &B) \\ar@{|->}[r] & (F[f]: F[A] \\ar[r] &F[B])",
      "X \\ar@/_3pc/[rrr]^{f;g} \\ar[r]^f &T[Y] \\ar[r]^{T[g]} &T[T[Z]] \\ar[r]^{m_Z} &T[Z]",
      " T[T[X]] \\ar[r]^{m_X} \\ar[d]_{T[T[f]]} &T[X]\\ar[d]^{T[f]}\\\\\n T[T[Y]] \\ar[r]^{m_Y}          &T[Y]\\\\",
      " u_X: X \\ar[r] &T[X]\\\\\n m_X: T[T[X]] \\ar[r] &T[X]\\\\",
      " X \\ar@/_1pc/[rrd]^{_{f;g}} \\ar[r]^{_f} &List[Y] \\ar[r]^{_{List[g]}} &List[List[Z]] \\ar[d]_{_{flatten}}\\\\\n &&List[Z]",
      "                                         &Y_1 \\ar@{_(->}[d] \\ar[r]^{g}           &Z \\ar@{_(->}[d] \\\\\n X_1 \\ar@{_(->}[d] \\ar[r]^{f}            &Y   \\ar@{_(->}[d] \\ar[r]^{Opt[g]} &{Opt[Z]}\\\\\n X                 \\ar[r]^{Opt[f]} &{Opt[Y]}"
    )
    <table border="1">
      <tr>
        <th>diagram</th>
        <th>xypic source</th>
      </tr>
      { for (sample <- samples if (sample.length() > 0)) yield oneSample(sample) }
     </table>
    }
 }