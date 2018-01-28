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
    "start with 'Hello'" in {
      "Hello world" must startWith("Hello")
    }
    "end with 'world'" in {
      "Hello world" must endWith("world")
    }
  }
}