package org.presheaf

import java.util.Date

import scala.xml._
import xml.UnprefixedAttribute

object HtmlSnippets {
  def img(src: String) = <img src={src}/>

  val BuildFile: String = "/buildno.txt"

  def buildNo: String = try {
    val vs = Res.read(BuildFile).getLines()
    if (!vs.hasNext) "No build data" else {
      val v = vs.next()
      if (!vs.hasNext) v + "/no build date" else {
        val d = vs.next()
        "0000".substring(4 - v.length) + v + " " + d
      }
    }
  } catch {
    case x: Exception => x.getMessage
  }

  def version = "4." + buildNo
  
  def signature = <font size="-2"> v.{ version } Copyright (c) Vlad Patryshev</font> <br/><i>Questions? <a href="mail:vpatryshev@gmail.com">ask me</a></i>


}