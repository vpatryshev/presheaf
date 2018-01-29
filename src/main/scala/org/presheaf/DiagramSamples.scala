package org.presheaf


object DiagramSamples {

  def samples = {
    ((List[String](),"") /: Res.read("/samples.txt").getLines()) (
      (accumulator: (List[String], String), line) => line match {
        case ""   => accumulator._2 match {
                     case "" => accumulator
                     case diagram => (diagram :: accumulator._1, "")
        }
        case some => (accumulator._1, accumulator._2 + "\n" + some)
      }
    )._1.reverse
  }
}