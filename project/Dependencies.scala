
import sbt._

object Dependencies {

  lazy val scalaHttp = "org.scalaj" %% "scalaj-http" % "2.3.0"

  lazy val logbackLogging = "ch.qos.logback" % "logback-classic" % "1.2.3"

  lazy val logging = "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"

  lazy val jacksonCore = "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.2"

  lazy val jacksonScalaModule = "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.9.2"

  lazy val testing = "org.scalatest" %% "scalatest" % "3.0.4" % "test"

  lazy val mock = "org.scalamock" %% "scalamock" % "4.1.0" % Test

  lazy val dependencies = Seq(
    scalaHttp,
    logging,
    logbackLogging,
    jacksonCore,
    jacksonScalaModule,
    testing,
    mock
  )
}
