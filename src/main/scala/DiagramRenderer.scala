package org.presheaf

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
      case Some(0) => Text("")
        //<p>{ Text(action._1) } : OK    <code>{ action._2 }</code></p>
      case _       => {
        <p>{ Text(action._1) } = "{ Text(action._2) }"</p>
        <p>result = { Text(results._1.toString) }</p>
        <p>log = { Text(results._2.replaceAll("\\n", "<br/>")) }</p>
        <p><font color="red">err = { Text(results._3.replaceAll("\\n", "<br/>")) }</font></p>
      }
    }
  }

  def runM(action: (String, String), logs: ListBuffer[Node], env: Map[String, String]): Option[Int] = {
    val results = OS.run(action._2, 4000, new File("."), env)
    logs ++= log(action, results)
    results._1
  }

  def withExtension(file: File, extension: String): File = {
    val stripped = file.getAbsolutePath.substring(0, file.getAbsolutePath.lastIndexOf('.'))
    new File(stripped + "." + extension)
  }

  def delete(file: File, extensions: List[String]) {
    for (x <- extensions) { withExtension(file, x).delete }
  }

  def nameFileFor(tex: String) = "d" + DiagramRenderer.md5(tex)

  def process(sourceDiagram: String) = {
    val diagram = DiagramRenderer.decode(sourceDiagram)
    val file = new File(cache, nameFileFor(diagram) + ".tex")
    val img: File = withExtension(file, "png")
    val pdf: File = withExtension(file, "pdf")
    val eps: File = withExtension(file, "eps")
    val dvi: File = withExtension(file, "dvi")
    val log = new ListBuffer[Node]

    if (!withExtension(file, "png").exists) {
      val pw = new FileOutputStream(file)
      pw.write(XYDiagram.buildTex(diagram).getBytes())
      pw.close

      val latexCommand  = DiagramRenderer.binPath + "latex -output-directory=" + cache.getAbsolutePath + " " + file.getAbsolutePath
      val dvipsCommand  = DiagramRenderer.binPath + "dvips -E -o " + eps + " " + dvi
      val epsCommand    = DiagramRenderer.binPath + "epstopdf " + eps
      val dvipngCommand = DiagramRenderer.binPath + "dvipng -T tight -o " + img + " " + dvi
      for (val r1 <- runM("latex"    ->  latexCommand, log, DiagramRenderer.env) if r1 == 0;
           val r2 <- runM("dvips"    ->  dvipsCommand, log, DiagramRenderer.env) if r2 == 0;
           val r3 <- runM("epstopdf" ->    epsCommand, log, DiagramRenderer.env) if r3 == 0;
           val r4 <- runM("dvipng"   -> dvipngCommand, log, DiagramRenderer.env) if r4 == 0) {
        delete(file, List("tex", "log", "aux", "dvi", "eps"))
      }
    }
    (diagram, img, pdf, log)
  }
}

object DiagramRenderer {

  val IsHome = new Regex("/home/(\\w+)")

  def homeDir = {
    def findIt(file: File) : Option[File] = {
      println("findit(" + file + ")->")
      println("->" + file.getAbsolutePath)
      file.getAbsolutePath match {
      case IsHome(owner) => Some(file)
      case _ => if (file.getAbsolutePath.contains("/")) findIt(file.getParentFile) else None
    }
    }
    println("homeDir?" + new File(".").getAbsolutePath)
    findIt(new File(new File(".").getAbsolutePath)).get.getAbsolutePath
  }

  lazy val binPath = "/usr/bin/"//appPath + "bin/"

  def env:Map[String, String] = Map(
//    "LD_LIBRARY_PATH" -> libPath,
//    "TEXINPUTS" -> texPath
    )

  def configure(context: ServletContext) = {
    println("Currently in " + new File(".").getAbsolutePath)
  }

  def encode(xy: String) = {
    val buf = new StringBuilder
    var withinSlash = false
    var withinCurly = false
    for (c <- xy; if (c >= ' '  )) {
      withinCurly |= c == '{'
      withinCurly &= c != '}'
      if (withinCurly) {
        if (c == ';') buf.append(".,") else buf.append(c)
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
    for (c <- xy.replaceAll("\\(\\(", "/").replaceAll("\\)\\)", "/").replaceAll("\\(\\)", "@").replaceAll(".,", ";")) {
      withinCurly |= c == '{'
      withinCurly &= c != '}'
      if (withinCurly) {
        buf.append(c)
      } else {
        buf.append(if (c == '|') '&' else c)
      }
    }
    buf.toString
  }

  import java.security._
  private val digest = MessageDigest.getInstance("MD5")
  def encode(b: Byte) = java.lang.Integer.toString(b & 0xff, 36)

  def md5(message: String) = {
    ("" /: digest.digest(message.getBytes("UTF-8"))) (_+ encode(_))
  }

}
