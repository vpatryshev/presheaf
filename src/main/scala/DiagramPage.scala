package org.presheaf

import org.presheaf.DiagramRenderer._
import javax.servlet.http._
import scala.xml._
import java.io._

class DiagramPage extends ScalaHttpServlet {
  val sample = "X \\ar@/_2pc/[rr]_{f;g} \\ar[r]^f &Y \\ar[r]^g &Z\\"
  val version = "1.000f"

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
         <textarea name="in" cols="60" rows="10">{ Text(sample) }</textarea>
         <input type="submit" value="Submit" />
       </form>
        <p>
          <a href="http://ctan.org/tex-archive/macros/generic/diagrams/xypic/xy/doc/xyguide.pdf">This pdf</a> tells in details how to write diagrams in xypic format.
        </p>

        <h4>Samples:</h4>
        <p>{ samples }</p>

        <font size="-2"> v.{ version } Copyright (c) Vlad Patryshev</font>
        <br/>Questions: <a href="mail:vpatryshev@gmail.com">ask me</a>
     </body>
   </html>
  }

  def process(req:HttpServletRequest) = {
    val diagram = req.getParameter("in")
    val context = req.getSession.getServletContext
    val workDir = new File(context.getRealPath("cache"))
    workDir.mkdirs
    val renderer = new DiagramRenderer(workDir)
    renderer.process(diagram)
  }

  def doGetXML(req:HttpServletRequest) = {
    configure(req.getSession.getServletContext)
    val format = notNull(req.getParameter("format"), "xy")
    val param = notNull(req.getParameter("in"), "")

    if (param.isEmpty) {
      page("Let's try a diagram",
           <p>enter something below</p>)
    } else {
      val (diagram, imgRef, pdfRef, logs) = process(req)
      val img = <img/> % fileAsAttr("src", imgRef)
      val pdf = <a>pdf</a> % fileAsAttr("href", pdfRef)
      page("Here's your diagram",
           <p>"<b>{ diagram }</b>"</p>
           <p>{ img }</p>
           <p>{ pdf }</p>
           <p>{ logs }</p>)
    }
  }

  def img(src: String) = <img/> % new UnprefixedAttribute("src", src, Null)
  def oneSample(xy: String) =
    <tr>
      <td>{ xy }</td>
      <td>{ img("dws?in=" + DiagramRenderer.encode(xy)) }</td>
    </tr>

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
        <th>xypic source</th>
        <th>diagram</th>
      </tr>
      { for (sample <- samples if (sample.length() > 0)) yield oneSample(sample) }
     </table>
    }
 }
