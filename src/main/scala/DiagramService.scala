package org.presheaf

import org.presheaf.HtmlSnippets._
import javax.servlet.http._
import xml.Node
import java.io.File

class DiagramService extends PresheafServlet {

  def page(diagram: String, imgRef: String, pdfRef: String, logs: Seq[Node]) = {
    <xml>
      <diagram>
        <source>{  diagram }</source>
        <image>{   imgRef  }</image>
        <pdf>{     pdfRef  }</pdf>
        <logs>{    logs    }</logs>
        <version>{ version }</version>
      </diagram>
    </xml>
  }

  val Q = "\""
  def quote(s: String) = Q + s.replaceAll(Q, "").replaceAll("\\\\", "\\\\\\\\") + Q
  def attr(nvp: (String,Object)) = nvp._1 + ":" + quote(nvp._2.toString)
  def json(map: Map[String, Object]) = map.map(attr _).mkString("{", ",", "}")

  override def doGet(req:HttpServletRequest, res:HttpServletResponse) : Unit = {
    res.setContentType("text/html")
    try {
      val (diagram, img, pdf, logs) : (String, File, File, Iterable[Node]) = process(req)
      req.getParameter("out") match {
        case "png" =>
          res.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY)
          res.setHeader("Location", ref(img))

        case "pdf" =>
          res.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY)
          res.setHeader("Location", ref(pdf))

        case _ =>
          res.getWriter.print(json(
            Map("source"  -> diagram,
                "logs"     -> logs.mkString("<br/>"),
                "imageUrl" -> ref(img),
                "pdfUrl"   -> ref(pdf),
                "version"  -> version)
                ))}
    } catch {
      case bd: BadDiagram => {
        println("Diagram service: bad diagram, " + bd.getMessage)
        res.sendError(500, bd.getMessage)
      }
      case e: Throwable   => {
        println("Diagram service: an exception")
        println(e)
        e.printStackTrace
        res.getWriter.print(json(Map("error" -> e.getMessage)))
        e.printStackTrace(); res.sendError(500, "Error while processing the diagram: " + e.getMessage)
      }
    }
  }
/*
def cached[K,V](f: K => V) : K => V = {
       val cache = new scala.collection.mutable.HashMap[K,V]
       k => cache.getOrElseUpdate(k, f(k))
     }

def linear(n: Int) = ((n + 1) / 2.

val best: Int => (Double, Int) = cached((n: Int) =>
                          if (n < 4) (linear(n), 0) else
                                          (for (k <- 2 to (n/2)) yield
                                              (1 + linear(k) * k / n + best(n-k)._1 * (n-k) / n, k)).min)

val theirs = List(14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 1)

def forSequence(s: List[Int]) = ((0., 0) /: s.reverse) ((a,b) => { val c = a._2 + b; (a._1 * a._2 / c + 1 + linear(b) * b / c, c)})

def bestSteps(height: Int): (Double, List[Int]) = {
  val first = best(height)
  (first._1, if (height < 4) Nil
             else first._2 :: bestSteps(height - first._2)._2)
}
*/
}