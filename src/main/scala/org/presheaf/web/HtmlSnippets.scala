package org.presheaf.web

import java.util.Date

import org.presheaf.Res

import scala.xml._
import xml.UnprefixedAttribute

object HtmlSnippets {
  def img(src: String) = <img src={src}/>

  val BuildFile: String = "/buildno.txt"

  def buildNo: String = try {
    val vs = Res.read(BuildFile).getLines().toList
    vs match {
      case Nil => " No build data"
      case v::Nil => v + "(no build date)"
      case v::d::_ => "0000".substring(4 - v.length) + v + " " + d
    }
  } catch {
    case x: Exception => x.getMessage
  }

  def version = "1.1.0, build#" + buildNo
  
  def signature = <font size="-2"> v.{ version } Copyright (c) Vlad Patryshev</font> <br/><i>Questions? <a href="mail:vpatryshev@gmail.com">ask me</a></i>


}