organization := "presheaf.org"

name := "Presheaf"

version := "1.1.0"


val WhichScala = "2.12.3"
val WhichScalatest = "3.0.4"

scalaVersion := WhichScala

//jetty()
tomcat()

libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided"

libraryDependencies ++= Seq(
  "org.eclipse.jetty" % "jetty-servlet" % "9.3.12.v20160915",
  "org.eclipse.jetty" % "jetty-server" % "9.3.12.v20160915"
)

// https://mvnrepository.com/artifact/org.scala-lang.modules/scala-xml
libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.0.6"

libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.1.0" % "test"

libraryDependencies += "org.specs2" %% "specs2-core" % "3.8.8" % "test"


val deploy = taskKey[Unit]("Deploy the packaged .war file")

deploy := {
  val (_, warFile) = (packagedArtifact in (Compile, packageWar)).value
  ("bash deploy.sh " + warFile.getPath) !
}

ideaExcludeFolders += ".idea"

ideaExcludeFolders += ".idea_modules"

