package org.presheaf.web

import java.io.{IOException, File, FileOutputStream}
import org.presheaf.{Diagram, OS}

import collection.mutable.ListBuffer
import scala.xml._
import util.matching.Regex
import javax.servlet.ServletContext
import Diagram._

/**
 * xypic Diagram Renderer
 * Produces a pdf and a png file
 */
class DiagramRenderer(val cache: File) {

  def asXhtml(s: String): Array[Node] = s split "\\n" flatMap {x =>  List(Text(x), <br/>)}

  def toHtml(action: (String, String), results: (Option[Int], String, String)): Seq[Node] = {
    results._1 match {
      case Some(0) => Nil
        //<p>{ Text(action._1) } : OK    <code>{ action._2 }</code></p>
      case _       => 
        <p>{ Text(action._1) } = "{ Text(action._2) }"</p>
        <p>result = { Text(results._1.toString) }</p>
        <p>log = { asXhtml(results  ._2) }</p>
        <p><font color="red">err = { asXhtml(results._3) }</font></p>
    }
  }

  def runM(action: (String, String), logs: ListBuffer[Node], env: Map[String, String]): Option[Int] = {
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

  def idFor(tex: String) = "d" + DiagramRenderer.md5(tex)

  def checkCache() {
    if (!cache.exists) Diagram.bad("Serverm error, cache directory missing " + cache.getAbsolutePath)
    if (!cache.isDirectory) bad("Server error, check cache directory " + cache.getAbsolutePath)
  }

  def diagramFile(name: String) = {
    checkCache()
    new File(cache, name + ".tex")
  }

  def process(sourceDiagram: String, opt: String) : Diagram = {

    if (sourceDiagram == null) {
      bad("No diagram provided")
    } else {
      OS.log("decoded '" + sourceDiagram + "' to '" + sourceDiagram + "'")
      val id = idFor(sourceDiagram)
      val img: File = new File(cache, id + ".png")
      val pdf: File = new File(cache, id + ".pdf")
      val result =
          if (img.exists) Diagram(id, sourceDiagram, img, pdf, new ListBuffer[Node])
          else             doWithScript(sourceDiagram, id)
      OS.log(s"Renderer.process: $result.")
      result
    }
  }

  def doWithScript(source: String, name: String): Diagram = {
    val log = new ListBuffer[Node]
    val file = diagramFile(name)
    val src: File = withExtension(file, "src")
    try {
      val srcFile = new FileOutputStream(src)
      srcFile.write(source.getBytes)
      srcFile.close()
    } catch {
      case ioe: IOException => 
        System.out.println(s"Got an $ioe while trying to write to $src - $source")
        log += <p>Diagram already in the system, can't override</p>
    }
    val img: File = withExtension(file, "png")
    val pdf: File = withExtension(file, "pdf")

    val command  = "sh /home/ubuntu/doit.sh "  + name
    // TODO: figure out wtf I transform an option to a tuple. it's wrong!
    runM("doit.sh" -> command, log, DiagramRenderer.env) match {
      case Some(0) =>
        println("\n------OK-------")
        Diagram(name, source, img, pdf, Nil)
      case otherwise =>
        println(s"\n------OOPS $otherwise-------")
        Diagram(name, source, img, pdf, log)
    }
  }
}

object DiagramRenderer {

  val IsHome = new Regex("/home/(\\w+)")

  def homeDir = {
    def findIt(file: File) : Option[File] = {
      file.getAbsolutePath match {
      case IsHome(owner) => Some(file)
      case _ => if (file.getAbsolutePath.contains("/")) findIt(file.getParentFile) else None
    }
    }
    findIt(new File(new File(".").getAbsolutePath)).get.getAbsolutePath
  }

  lazy val binPath = "/usr/bin/"//appPath + "bin/"

  def env:Map[String, String] = Map(
//    "LD_LIBRARY_PATH" -> libPath,
//    "TEXINPUTS" -> texPath
    )

  def configure(context: ServletContext) = {
    OS.log("Currently in " + new File(".").getAbsolutePath)
  }

  def encode(xy: String) = xy

  def decode(xy: String) = {
    if (xy == null) null else
    xy.replaceAll("\n", " ")
  }

  import java.security._
  private val digest = MessageDigest.getInstance("MD5")
  digest.reset()
  def encode(b: Byte) = java.lang.Integer.toString(b & 0xff, 36)

  def md5(message: String) = {
    ("" /: digest.digest(message.getBytes("UTF-8"))) (_+ encode(_))
  }

}
