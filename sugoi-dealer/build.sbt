name := "sugoi-dealer"

version := "1.0"

scalaVersion := "2.12.3"

logLevel := Level.Debug

libraryDependencies ++= Seq(
  "org.apache.logging.log4j" % "log4j-api" % "2.8.2",
  "org.apache.logging.log4j" % "log4j-core" % "2.8.2",
  "org.apache.logging.log4j" %% "log4j-api-scala" % "11.0",
  "com.fasterxml.jackson.module" % "jackson-module-scala_2.12" % "2.9.0.pr3",
  "com.fasterxml.jackson.core" % "jackson-core" % "2.9.0.pr3",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)

parallelExecution := false

mainClass in (Compile, run) := Some("com.kenkoooo.sugoi.SugoiDealer")