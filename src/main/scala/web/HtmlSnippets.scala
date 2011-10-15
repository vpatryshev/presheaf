package org.presheaf

import scala.xml._
import xml.UnprefixedAttribute

object HtmlSnippets {
  def img(src: String) = <img src={src}/>

  def buildNo: String = {
    for (v <- Res.read("/buildno.txt").getLines()) {
      return "0000".substring(4 - v.length) + v
    }
    "?"
  }

  def version = "1." + buildNo
  
  def signature = <font size="-2"> v.{ version } Copyright (c) Vlad Patryshev</font> <br/><i>Questions? <a href="mail:vpatryshev@gmail.com">ask me</a></i>


}