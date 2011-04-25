package org.presheaf

import actors.Futures
import actors.Futures._
import java.io.{IOException, InputStream, BufferedOutputStream, File}

/**
 * OS - represents Operating System
 */

object  OS {

  def log(s: String) {
    println("" + new java.util.Date + ": " + s)
  }
  
  def dumper(stream: InputStream, buf: StringBuffer) =
    future(
      try {
        for (line <- scala.io.Source.fromInputStream(stream).getLines) buf.append(line)
      } catch {
        case ioe: Any => buf.append('\n').append(ioe.getMessage).append(ioe)
      }
    )

  val here = new File(".")

  def runWithGivenEnv(command: String, timeout: Long = 1000, dir: File = here, env: Array[String] = null) = {
    val stdout = new StringBuffer
    val stderr = new StringBuffer
    val ctrlD = 4
//    stdout.append(here.getAbsolutePath + "> " + command)
    val process = java.lang.Runtime.getRuntime.exec(command, env, dir)
    val processIn = new BufferedOutputStream(process.getOutputStream)
    for (i <- 1 to 10) processIn.write(ctrlD)

    Futures.awaitAll(timeout,
      future { process.waitFor },
      dumper(process.getInputStream, stdout),
      dumper(process.getErrorStream, stderr)
    )

    process.destroy()

    try {
      (Some(process.exitValue), stdout.toString, stderr.toString)
    } catch {
      case _ => (None, stdout.toString, stderr.toString)
    }
  }

    def run(command: String, timeout: Long = 1000, dir: File = here, env: Map[String, String] = null) = {
      if (env == null) {
        runWithGivenEnv(command, timeout, dir, null)
      } else {
        val envForRuntime = (initialEnv ++ env).toArray.map(p => p._1 + "=" + p._2)
        log(here.getAbsolutePath + "> " + command + "," + (if (env == null) "no env" else "\nenv=" + env.mkString(",")))
        runWithGivenEnv(command, timeout, dir, envForRuntime)
      }
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
    run("whoami")._2.toString
  }

  lazy val initialEnv = {
    val envString = runWithGivenEnv("env")._2
    val envArray = envString.split("\n")
    Map(envArray.map(_.split("=")).filter(_.length == 2).map((a:Array[String]) => a(0) -> a(1)): _*)
  }
}