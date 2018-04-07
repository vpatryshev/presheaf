package org.presheaf

import java.io.{ByteArrayInputStream, BufferedOutputStream, File, InputStream}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.sys.process._

/**
  * OS - representing Operating System
  */

object OS {

  def log(thing: Any) {
    println("" + new java.util.Date + "] " + thing + "\n")
  }

  def run(cmd: String): (Int, String, String) = {
    val se = new StringBuilder
    val so = new StringBuilder
    val is = new ByteArrayInputStream("\4,\4,\4,\4".getBytes)
    val status = cmd #< is ! ProcessLogger(
      o => so append (o + "\n"),
      e => se append (e + "\n"))
    (status, so.toString, se.toString)
  }
  
  def ln(target: File, link: File) {
    run("ln -s " + target.getAbsolutePath + " " + link.getAbsolutePath)
  }

  def chmod(target: File, flags: String) {
    run("chmod " + flags + " " + target.getAbsolutePath)
  }

  def cp(source: File, target: File) {
    run("cp " + source.getAbsolutePath + " " + target.getAbsolutePath)
  }

  def whoami: String = {
    run("whoami")._2.toString.trim
  }

  val IsHome = "/home/(\\w+)".r

  def homeDir: String = {
    def findIt(file: File) : Option[File] = {
      file.getAbsolutePath match {
        case IsHome(owner) => Some(file)
        case _ => if (file.getAbsolutePath.contains("/")) findIt(file.getParentFile) else None
      }
    }
    findIt(new File(new File(".").getAbsolutePath)).get.getAbsolutePath
  }

}