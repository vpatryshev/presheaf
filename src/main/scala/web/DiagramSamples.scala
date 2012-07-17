package org.presheaf

import DiagramRenderer._
import HtmlSnippets._
import javax.servlet.http._

class DiagramSamples extends PresheafServlet {

  def page(title: String) = {
    <html>
      <head>
        <title>{ title }</title>
      </head>
      <body>
        <h1>{ title }</h1>
        <p>{ samplesHtml }</p>
        <p>
          <a href="http://ctan.org/tex-archive/macros/generic/diagrams/xypic/xy/doc/xyguide.pdf">This pdf</a> tells in details how to write diagrams in xypic format.
        </p>
        { signature }
        <hr/>
          {
//             Res.read("/samples.txt")
          }
     </body>
   </html>
  }

  def oneSample(xy: String) =
    <tr>
      <td>{ img("dws?op=aspng&format=xy&in=" + DiagramRenderer.encode(xy)) }</td>
      <td>{ xy }</td>
    </tr>

  override def doGetXML(req:HttpServletRequest) = {
    configure(req.getSession.getServletContext)
    page("XY Diagram Samples")
  }

    def samplesHtml = {

    <table border="1">
      <tr>
        <th>diagram</th>
        <th>xypic source</th>
      </tr>
      { DiagramSamples.samples map oneSample }
     </table>
    }
 }

object DiagramSamples {

  def samples = {
    ((List[String](),"") /: Res.read("/samples.txt").getLines()) (
      (accumulator: (List[String], String), line) => line match {
        case ""   => accumulator._2 match {
                     case "" => accumulator
                     case diagram => (diagram :: accumulator._1, "")
        }
        case some => (accumulator._1, accumulator._2 + "\n" + some)
      }
    )._1.reverse
  }
}