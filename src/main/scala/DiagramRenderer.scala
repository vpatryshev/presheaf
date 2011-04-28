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

  def log(action: (String, String), results: (Option[Int], String, String)): Seq[Node] = {
    results._1 match {
      case Some(0) => Text("")
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

  def checkCache {
    if (!cache.exists) throw new BadDiagram("System error, cache directory missing " + cache.getAbsolutePath)
    if (!cache.isDirectory) throw new BadDiagram("System error, check cache directory " + cache.getAbsolutePath)
  }

  def diagramFile(name: String) = {
    checkCache;
    new File(cache, name + ".tex")
  }

  def process(sourceDiagram: String, opt: String) : (String, File, File, Iterable[Node]) = {
    if (sourceDiagram == null) {
      throw new BadDiagram("No diagram provided")
    } else {
      val diagram = DiagramRenderer.decode(sourceDiagram)
      val name = nameFileFor(diagram)
      val file = new File(cache, name + ".tex")
      val img: File = withExtension(file, "png")
      val pdf: File = withExtension(file, "pdf")
      if (img.exists) (diagram, img, pdf, new ListBuffer[Node])
      else             doWithScript(diagram, name)
    }
  }

  def doWithScript(diagram: String, name: String) = {
    val file = diagramFile(name)
    val img: File = withExtension(file, "png")
    val pdf: File = withExtension(file, "pdf")
    val log = new ListBuffer[Node]
    val pw = new FileOutputStream(file)
    pw.write(XYDiagram.buildTex(diagram).getBytes())
    pw.close

    if (!file.exists) throw new BadDiagram("System error, diagram file missing after writing: " + file.getAbsolutePath)
    if (!file.canRead) throw new BadDiagram("System error, can't read new file " + file.getAbsolutePath)

    val command  = "/home/ubuntu/doxy.sh "  + name
    // TODO: figure out wtf I transform an option to a tuple. it's wrong!
    runM("doxy.sh" -> command, log, DiagramRenderer.env)
    (diagram, img, pdf, log)
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
        buf.append(if (c == '&') "__" else if (c == '@') "()" else c)
      }
    }

    buf.toString
  }

  def decode(xy: String) = {
    if (xy == null) null else
    xy.replaceAll("\\(\\(", "/")
      .replaceAll("\\)\\)", "/")
      .replaceAll("\\(\\)", "@")
      .replaceAll(".,", ";")
      .replaceAll("__", "&")
      .replaceAll("\n", " ")
  }

  import java.security._
  private val digest = MessageDigest.getInstance("MD5")
  digest.reset
  def encode(b: Byte) = java.lang.Integer.toString(b & 0xff, 36)

  def md5(message: String) = {
    ("" /: digest.digest(message.getBytes("UTF-8"))) (_+ encode(_))
  }

}
