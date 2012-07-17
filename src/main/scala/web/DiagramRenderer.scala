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

  def asXhtml(s: String): Array[Node] = s split "\\n" flatMap {x =>  List(Text(x), <br/>)}

  def toHtml(action: (String, String), results: (Option[Int], String, String)): Seq[Node] = {
    results._1 match {
      case Some(0) => Nil
        //<p>{ Text(action._1) } : OK    <code>{ action._2 }</code></p>
      case _       => {
        <p>{ Text(action._1) } = "{ Text(action._2) }"</p>
        <p>result = { Text(results._1.toString) }</p>
        <p>log = { asXhtml(results  ._2) }</p>
        <p><font color="red">err = { asXhtml(results._3) }</font></p>
      }
    }
  }

  def runM(action: (String, String), logs: ListBuffer[Node], env: Map[String, String]): Option[Int] = {
    val results = OS.run(action._2, 4000, new File("."), env)
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

  def checkCache {
    if (!cache.exists) throw new BadDiagram("Serverm error, cache directory missing " + cache.getAbsolutePath)
    if (!cache.isDirectory) throw new BadDiagram("Server error, check cache directory " + cache.getAbsolutePath)
  }

  def diagramFile(name: String) = {
    checkCache
    new File(cache, name + ".tex")
  }

  def process(sourceDiagram: String, opt: String) : (String, String, File, File, Iterable[Node]) = {

    if (sourceDiagram == null) {
      throw new BadDiagram("No diagram provided")
    } else {
      val diagram = DiagramRenderer.decode(sourceDiagram)
      OS.log("decoded '" + sourceDiagram + "' to '" + diagram + "'")
      val id = idFor(diagram)
      val img: File = new File(cache, id + ".png")
      val pdf: File = new File(cache, id + ".pdf")
      val result =
          if (img.exists) (id, diagram, img, pdf, new ListBuffer[Node])
          else             doWithScript(diagram, id)
      OS.log("Renderer.process: " + result)
      result
    }
  }

  def doWithScript(diagram: String, name: String) = {
//    println("DiagramRenderer running diagram with script: " + name)
    val file = diagramFile(name)
    val src: File = withExtension(file, "src")
    val srcFile = new FileOutputStream(src)
    srcFile.write(diagram.getBytes)
    srcFile.close
    val img: File = withExtension(file, "png")
    val pdf: File = withExtension(file, "pdf")
    val log = new ListBuffer[Node]
    val pw = new FileOutputStream(file)
    pw.write(XYDiagram.buildTex(diagram).getBytes)
    pw.close

    if (!file.exists) throw new BadDiagram("System error, diagram file missing after writing: " + file.getAbsolutePath)
    if (!file.canRead) throw new BadDiagram("System error, can't read new file " + file.getAbsolutePath)

    val command  = "sh /root/doxy.sh "  + name
    // TODO: figure out wtf I transform an option to a tuple. it's wrong!
    runM("doxy.sh" -> command, log, DiagramRenderer.env)
    val result = (name, diagram, img, pdf, log)
//    println("DiagramRenderer: seems to succeed: " + result)
    result
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
//    println("homeDir?" + new File(".").getAbsolutePath)
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
  digest.reset
  def encode(b: Byte) = java.lang.Integer.toString(b & 0xff, 36)

  def md5(message: String) = {
    ("" /: digest.digest(message.getBytes("UTF-8"))) (_+ encode(_))
  }

}
