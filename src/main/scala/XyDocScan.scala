package org.presheaf

import java.io.{InputStream, FileInputStream}

object XyDocScan {
  def bytes(is: InputStream) : Iterator[Byte] = new Iterator[Byte] {
    def hasNext = is.available > 0
    def next = is.read.toByte
  }

  def isText(b: Byte) = b == 0x0a || b == 0x0d || b >= 32 && b < 0xff

  def chars(is: InputStream) = bytes(is) filter isText map (_.toChar)

  import org.presheaf.DiagramSamples
  
  def main(args: Array[String]) {
    val scanner = new XyScanner(chars(new FileInputStream(args(0))))
    for (diagram <- scanner) {
      if (!diagram.isEmpty) println(diagram + "\n")
    }
  }

  class XyScanner(val input: Iterator[Char]) extends Iterator[String] {
    def matches(input: Iterator[Char], what: String): Boolean = {
      for (c <- what) {
        if (!(input.hasNext && c == input.next)) return false
      }
      true
    }

    // kind of cheap, won't find 'maman' in 'mamaman'
    def find(input: Iterator[Char], what: String) {
      while (input.hasNext) {
        if(matches(input, what)) {
          return
        }
      }
    }

    def hasNext = {
      input.hasNext
    }

    private[this] val sb = new StringBuilder

    def next = {
      find(input, "\\xymatrix{")
      if (!hasNext) {
        ""
      } else {
        sb.clear
        var level = 1
        while (input.hasNext && level > 0) {
          val c = input.next
          sb append c
          c match {
            case '{' => level = level + 1
            case '}' => level = level - 1
            case _   =>
          }
        }
        sb.dropRight(1).mkString("")
      }
    }
  }
}