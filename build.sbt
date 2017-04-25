name := "Presheaf"

organization := "com.vpatryshev"

version := "1.1"

val sVer = "2.10.4"

scalaVersion := sVer

//jetty()
tomcat()

libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided"

libraryDependencies ++= Seq( // test
    "org.eclipse.jetty" % "jetty-webapp" % "9.1.0.v20131115" % "test"
  , "org.eclipse.jetty" % "jetty-plus" % "9.1.0.v20131115" % "test"
  , "javax.servlet" % "javax.servlet-api" % "3.1.0" % "test"
  ,"org.scala-lang" % "scala-actors" % sVer withSources
  , "org.scalatest" %% "scalatest" % "1.9.1" % "test"
)

ScoverageSbtPlugin.instrumentSettings

CoverallsPlugin.coverallsSettings

val deploy = taskKey[Unit]("Deploy the packaged .war file")

deploy := {
  val (_, warFile) = (packagedArtifact in (Compile, packageWar)).value
  ("bash deploy.sh " + warFile.getPath) !
}

ideaExcludeFolders += ".idea"

ideaExcludeFolders += ".idea_modules"

