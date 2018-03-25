organization := "presheaf.org"

name := "Presheaf"

version := "1.1.0"


val WhichScala = "2.12.4"
val WhichScalatest = "3.0.4"

scalaVersion := WhichScala

//jetty()
tomcat()

libraryDependencies ++= Seq(
  "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.0",
  "com.typesafe.akka" %% "akka-actor" % "2.5.11",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.11" % Test,
  "com.typesafe.akka" %% "akka-stream" % "2.5.11",
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.11" % Test,
  "com.typesafe.akka" %% "akka-http" % "10.1.0",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.1.0" % Test
)

// https://mvnrepository.com/artifact/org.scala-lang.modules/scala-xml

libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.1.0" % "test"

libraryDependencies += "org.specs2" %% "specs2-core" % "3.8.8" % "test"


val deploy = taskKey[Unit]("Deploy the packaged .war file")

deploy := {
  val (_, warFile) = (packagedArtifact in (Compile, packageWar)).value
  ("bash deploy.sh " + warFile.getPath) !
}

ideaExcludeFolders += ".idea"

ideaExcludeFolders += ".idea_modules"

