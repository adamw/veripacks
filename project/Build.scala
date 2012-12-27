import sbt._
import Keys._

object BuildSettings {
  val buildSettings = Defaults.defaultSettings ++ Seq (
    organization  := "org.veripacks",
    version       := "0.0.1-SNAPSHOT",
    scalaVersion  := "2.10.0-RC5",
    scalacOptions += "",
    licenses      := ("Apache2", new java.net.URL("http://www.apache.org/licenses/LICENSE-2.0.txt")) :: Nil
  )
}

object Dependencies {
  val asm           = "org.ow2.asm"               % "asm"                   % "4.1"

  val scalaLogging  = "com.typesafe"              % "scalalogging-slf4j_2.10.0-RC3" % "0.4.0"
  val logback       = "ch.qos.logback"            % "logback-classic"       % "1.0.7"

  val scalatest     = "org.scalatest"             % "scalatest_2.10.0-RC3"  % "1.8-B1"    % "test"
  val mockito       = "org.mockito"               % "mockito-core"          % "1.9.5"     % "test"

  val testing = Seq(scalatest, mockito, logback % "test")
}

object VeripacksBuild extends Build {
  import BuildSettings._
  import Dependencies._

  lazy val root: Project = Project(
    "veripacks-root",
    file("."),
    settings = buildSettings
  ) aggregate(annotations, verifier)

  lazy val annotations: Project = Project(
    "veripacks-annotations",
    file("annotations"),
    settings = buildSettings
  )

  lazy val verifier: Project = Project(
    "veripacks-verifier",
    file("verifier"),
    settings = buildSettings ++ Seq(libraryDependencies ++= testing ++ Seq(asm, scalaLogging))
  ) dependsOn(annotations)
}
