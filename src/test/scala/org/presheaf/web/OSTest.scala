package org.presheaf.web

import org.presheaf.OS
import org.specs2.mutable._
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global

class OSTest extends Specification {

  "OS" should {
    "know who am i" in {
      OS.whoami must_== "vpatryshev"
    }
    "run simple stuff ok" in {
      OS.run("ls /opt/X11") must_== (Some(0), "bin\netc\ninclude\nlib\nlibexec\nshare\nvar\n", "")
    }
    "run simple stuff with error msg'" in {
      OS.run("ls /nonexistent") must_== (Some(1), "", "ls: /nonexistent: No such file or directory\n")
    }
  }
}