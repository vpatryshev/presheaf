package org.presheaf

import scala.xml._
import xml.UnprefixedAttribute

object HtmlSnippets {
  def img(src: String) = <img/> % new UnprefixedAttribute("src", src, Null)

  def oneSample(xy: String) =
    <tr>
      <td>{ img("dws?out=png&in=" + DiagramRenderer.encode(xy)) }</td>
      <td>{ xy }</td>
    </tr>

  def buildNo: String = {
    for (v <- Res.read("/buildno.txt").getLines()) {
      return "0000".substring(4 - v.length) + v
    }
    "?"
  }

  def version = "1." + buildNo
  
  def signature = <font size="-2"> v.{ version } Copyright (c) Vlad Patryshev</font> <br/><i>Questions? <a href="mail:vpatryshev@gmail.com">ask me</a></i>


}