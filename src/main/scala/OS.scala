package org.pullback

import java.io.{InputStream, BufferedOutputStream, File}
import actors.Futures
import actors.Futures._

/**
 * OS - represents Operating System
 */

object OS {

  def dumper(stream: InputStream, buf: StringBuffer) =
    future(
      for (line <- scala.io.Source.fromInputStream(stream).getLines) buf.append(line)
    )

  val here = new File(".")

  def run(command: String, timeout: Long = 1000, dir: File = here) = {
    val stdout = new StringBuffer
    val stderr = new StringBuffer
    val ctrlD = 4
//  println("\n\n==command==" + command + "\n\n")
    stdout.append("cmd=" + command)

    val process = java.lang.Runtime.getRuntime.exec(command, null, dir)
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

  def ln(target: File, link: File) {
    run("ln -s " + target.getAbsolutePath + " " + link.getAbsolutePath)
  }

  def chmod(target: File, flags: String) {
    run("chmod " + flags + " " + target.getAbsolutePath)
  }

  def cp(source: File, target: File) {
    run("cp " + source.getAbsolutePath + " " + target.getAbsolutePath)
  }
}