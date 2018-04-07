package org.presheaf.web

import java.io.{IOException, File, FileOutputStream}
import org.presheaf.{Diagram, OS}

import collection.mutable.ListBuffer
import util.matching.Regex
import Diagram._

/**
 * xypic Diagram Renderer
 * Produces a pdf and a png file
 */
class DiagramRenderer(val cache: File) {

  def toHtml(action: (String, String), results: (Int, String, String)): Seq[String] = {
    results._1 match {
      case 0 => Nil
        //<p>{ Text(action._1) } : OK    <code>{ action._2 }</code></p>
      case _       => 
        s"""<p>${ action._1 } = "${ action._2 }"</p>"""::
        s"""<p>result = ${results._1 }</p>"""::
        s"""<p>log = ${results._2}</p>"""::
        s"""<p><font color="red">err = ${results._3}</font></p>"""::
        Nil
    }
  }

  def runM(action: (String, String), logs: ListBuffer[String], env: Map[String, String]): Int = {
    val results = OS.run(action._2)
    logs ++= toHtml(action, results)
    results._1
  }

  def withExtension(file: File, extension: String): File = {
    val stripped = file.getAbsolutePath.substring(0, file.getAbsolutePath.lastIndexOf('.'))
    new File(stripped + "." + extension)
  }

  def delete(file: File, extensions: List[String]) {
    for (x <- extensions) { withExtension(file, x).delete }
  }

  def idFor(tex: String): String = "d" + DiagramRenderer.md5(tex)

  def checkCache() {
    if (!cache.exists) Diagram.bad("Serverm error, cache directory missing " + cache.getAbsolutePath)
    if (!cache.isDirectory) bad("Server error, check cache directory " + cache.getAbsolutePath)
  }

  def diagramFile(name: String): File = {
    checkCache()
    new File(cache, name + ".tex")
  }

  def process(sourceDiagram: String) : Diagram = {
      OS.log("decoded '" + sourceDiagram + "' to '" + sourceDiagram + "'")
      val id = idFor(sourceDiagram)
      val img: File = new File(cache, id + ".png")
      val pdf: File = new File(cache, id + ".pdf")
      val result =
          if (img.exists) new Diagram(id, sourceDiagram, img, pdf)
          else             doWithScript(sourceDiagram, id)
      OS.log(s"Renderer.process: $result.")
      result
    }
  }

  def doWithScript(source: String, name: String): Diagram = {
    val log = new ListBuffer[String]
    val file = diagramFile(name)
    val src: File = withExtension(file, "src")
    try {
      val srcFile = new FileOutputStream(src)
      srcFile.write(source.getBytes)
      srcFile.close()
    } catch {
      case ioe: IOException => 
        System.out.println(s"Got an $ioe while trying to write to $src - $source")
        log += "<p>Diagram already in the system, can't override</p>"
    }
    val img: File = withExtension(file, "png")
    val pdf: File = withExtension(file, "pdf")

    val command  = "sh /home/ubuntu/doit.sh "  + name
    // TODO: figure out wtf I transform an option to a tuple. it's wrong!
    runM("doit.sh" -> command, log, DiagramRenderer.env) match {
      case 0 =>
        println("\n------OK-------")
        new Diagram(name, source, img, pdf)
      case otherwise =>
        println(s"\n------OOPS $otherwise-------\n${log.mkString("\n")}")
        new Diagram(name, source, img, pdf, log)
    }
  }
}

object DiagramRenderer {

  lazy val binPath = "/usr/bin/"//appPath + "bin/"

  def env:Map[String, String] = Map(
//    "LD_LIBRARY_PATH" -> libPath,
//    "TEXINPUTS" -> texPath
    )

  import java.security._
  private val digest = MessageDigest.getInstance("MD5")
  digest.reset()
  def encode(b: Byte): String = java.lang.Integer.toString(b & 0xff, 36)

  def md5(message: String): String = {
    ("" /: digest.digest(message.getBytes("UTF-8"))) (_+ encode(_))
  }

}
