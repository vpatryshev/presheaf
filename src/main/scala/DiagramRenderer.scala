package org.pullback

import java.io.{File, FileOutputStream}
import collection.mutable.ListBuffer
import scala.xml._
import util.matching.Regex
import javax.servlet.ServletContext

/**
 * xypic Diagram Renderer
 * Produces a pdf and a png file
 */
class DiagramRenderer(val cache: File) {

  def log(action: (String, String), results: (Option[Int], String, String)): Seq[Node] = {
    results._1 match {
      case Some(0) => <p>{ Text(action._1) } : OK    <code>{ action._2 }</code></p>
      case _       => {
        <p>{ Text(action._1) } = "{ Text(action._2) }"</p>
        <p>result = { Text(results._1.toString) }</p>
        <p>log = { Text(results._2.replaceAll("\\n", "<br/>")) }</p>
        <p><font color="red">err = { Text(results._3.replaceAll("\\n", "<br/>")) }</font></p>
      }
    }
  }

  def runM(action: (String, String), logs: ListBuffer[Node]): Option[Int] = {
    val results = OS.run(action._2, 4000)
    logs ++= log(action, results)
    results._1
  }

  val prefix =
"""\documentclass[12pt,notitlepage]{article}
\""" +
"""usepackage[all]{xy}

\begin{document}

\thispagestyle{empty}

\[\xymatrix{
"""

  val suffix = "\n}\\]\\end{document}\n"

  def buildTex(s: String) = prefix + s + suffix

  def withExtension(file: File, extension: String): File = {
    val stripped = file.getAbsolutePath.substring(0, file.getAbsolutePath.lastIndexOf('.'))
    new File(stripped + "." + extension)
  }

  def delete(file: File, extensions: List[String]) {
    for (x <- extensions) { withExtension(file, x).delete }
  }

  def encode(xy: String) = {
    val buf = new StringBuilder
    var withinSlash = false
    var withinCurly = false
    for (c <- xy) {
      withinCurly |= c == '{'
      withinCurly &= c != '}'
      if (withinCurly) {
        buf.append(if (c == ';') '|' else c)
      } else if (c == '/') {
        withinSlash = !withinSlash
        buf.append(if (withinSlash) "((" else "))")
      } else {
        buf.append(if (c == '&') "|" else if (c == '@') "()" else c)
      }
    }

    buf.toString
  }

  def decode(xy: String) = {
    val buf = new StringBuilder
    var withinCurly = false
    for (c <- xy.replaceAll("\\(\\(", "/").replaceAll("\\)\\)", "/").replaceAll("\\(\\)", "@")) {
      withinCurly |= c == '{'
      withinCurly &= c != '}'
      if (!withinCurly) {
        buf.append(if (c == '|') '&' else c)
      } else {
        buf.append(if (c == '|') ';' else c)
      }
    }
    buf.toString
  }

  def process(sourceDiagram: String) = {
    val diagram = decode(sourceDiagram)
    val file = new File(cache, encode(diagram).replaceAll("\\\\", "").replaceAll(" ", "") + ".tex")
    val imgRef: File = withExtension(file, "png")
    val pdfRef: File = withExtension(file, "pdf")
    val log = new ListBuffer[Node]

    if (!withExtension(file, "png").exists) {
      val pw = new FileOutputStream(file)
      pw.write(buildTex(diagram).getBytes())
      pw.close

      val latexCommand = DiagramRenderer.binPath + "latex -output-directory=" + cache.getAbsolutePath + " " + file.getAbsolutePath
      val dvipsCommand = DiagramRenderer.binPath + "dvips -E -o " + withExtension(file, "eps") + " " + withExtension(file, "dvi")
      val epsCommand = DiagramRenderer.binPath + "epstopdf " + withExtension(file, "eps")
      val dvipngCommand = DiagramRenderer.binPath + "dvipng -T tight -o " + withExtension(file, "png") + " " + withExtension(file, "dvi")

      for (val r1 <- runM("latex" -> latexCommand,   log) if r1 == 0;
           val r2 <- runM("dvips" -> dvipsCommand,   log) if r2 == 0;
           val r3 <- runM("epstopdf" -> epsCommand,  log) if r3 == 0;
           val r4 <- runM("dvipng" -> dvipngCommand, log) if r4 == 0) {
      }
//      delete(file, List("tex", "log", "aux", "dvi", "eps"))
    }
    (diagram, imgRef, pdfRef, log)
  }
}

object DiagramRenderer {

  val IsHome = new Regex("/home/(\\w+)")

  def findHome = {
    def findIt(file: File) : Option[File] = file.getAbsolutePath match {
      case IsHome(owner) => Some(file)
      case _ => if (file.getAbsolutePath.contains("/")) findIt(file.getParentFile) else None
    }
    findIt(new File(new File(".").getAbsolutePath)).get.getAbsolutePath
  }

  lazy val appPath = findHome + "/apps/"

  def binPath = appPath + "bin/"
  
  val archiveName = "apps.tgz"

  def ensureAppPresence(context: ServletContext) = {
    val source = new File(context.getRealPath("WEB-INF/classes/org/pullback/" + archiveName))
    val target = new File(appPath, archiveName)
    if (!target.exists && source.exists) {
      OS.cp(source, target)
      OS.run("tar xzf " + target.getAbsolutePath, 3000, new File(findHome))
      for (file <- new File(binPath).listFiles) {
        OS.chmod(file, "u+x")
      }
    }
    println("DiagramRenderer ensuring app presence: " + source.exists + "," + target.exists)
  }
}
