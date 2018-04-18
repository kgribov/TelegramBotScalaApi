
import sbt._

object Dependencies {

  lazy val scalaHttp = "org.scalaj" %% "scalaj-http" % "2.3.0"

  lazy val logbackLogging = "ch.qos.logback" % "logback-classic" % "1.2.3"

  lazy val logging = "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"

  lazy val jacksonCore = "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.2"

  lazy val jacksonScalaModule = "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.9.2"

  lazy val cache = "com.github.blemale" %% "scaffeine" % "2.5.0" % "compile"

  lazy val cron = "com.github.alonsodomin.cron4s" %% "cron4s-core" % "0.4.5"

  lazy val testing = "org.scalatest" %% "scalatest" % "3.0.4" % Test

  lazy val mock = "org.scalamock" %% "scalamock" % "4.1.0" % Test

  lazy val dependencies = Seq(
    scalaHttp,
    logging,
    logbackLogging,
    jacksonCore,
    jacksonScalaModule,
    cache,
    cron,
    testing,
    mock
  )
}
