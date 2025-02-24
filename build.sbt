import com.softwaremill.SbtSoftwareMillCommon.commonSmlBuildSettings

lazy val commonSettings = commonSmlBuildSettings ++ Seq(
  organization := "com.softwaremill.pres",
  scalaVersion := "3.3.5"
)

val scalaTest = "org.scalatest" %% "scalatest" % "3.2.19" % Test

lazy val rootProject = (project in file("."))
  .settings(commonSettings*)
  .settings(
    name := "ox-flows-pres",
    libraryDependencies ++= Seq(
      scalaTest,
      "com.softwaremill.sttp.tapir" %% "tapir-netty-server-sync" % "1.11.15",
      "com.softwaremill.ox" %% "kafka" % "0.5.11",
      "ch.qos.logback" % "logback-classic" % "1.5.16",
      "org.apache.pekko" %% "pekko-connectors-kafka" % "1.1.0",
      "org.apache.pekko" %% "pekko-stream" % "1.1.3",
      "com.softwaremill.sttp.client4" %% "core" % "4.0.0-RC1"
    )
  )
