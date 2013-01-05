import sbt._
import Keys._

object BuildSettings {
  val buildSettings = Defaults.defaultSettings ++ Seq (
    organization  := "org.veripacks",
    version       := "0.2-SNAPSHOT",
    scalaVersion  := "2.10.0",
    scalacOptions += "",
    licenses      := ("Apache2", new java.net.URL("http://www.apache.org/licenses/LICENSE-2.0.txt")) :: Nil,
    publishTo     <<= (version) { version: String =>
      val nexus = "http://nexus.softwaremill.com/content/repositories/"
      if (version.trim.endsWith("SNAPSHOT"))  Some("softwaremill-public-snapshots" at nexus + "snapshots/")
      else                                    Some("softwaremill-public-releases"  at nexus + "releases/")
    },
    credentials   += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    publishMavenStyle := true
  ) ++ net.virtualvoid.sbt.graph.Plugin.graphSettings
}

object Dependencies {
  val asm           = "org.ow2.asm"               % "asm"                   % "4.1"

  val scalaLogging  = "com.typesafe"              %% "scalalogging-slf4j"   % "1.0.1"
  val logback       = "ch.qos.logback"            % "logback-classic"       % "1.0.7"

  val scalatest     = "org.scalatest"             %% "scalatest"            % "1.9.1"     % "test"
  val mockito       = "org.mockito"               % "mockito-core"          % "1.9.5"     % "test"

  val testing = Seq(scalatest, mockito, logback % "test")
}

object VeripacksBuild extends Build {
  import BuildSettings._
  import Dependencies._

  lazy val root: Project = Project(
    "veripacks-root",
    file("."),
    settings = buildSettings ++ Seq(publishArtifact := false)
  ) aggregate(annotations, verifier, selfTest)

  lazy val annotations: Project = Project(
    "veripacks-annotations",
    file("annotations"),
    settings = buildSettings
  )

  lazy val verifier: Project = Project(
    "veripacks-verifier",
    file("verifier"),
    settings = buildSettings ++ Seq(
      libraryDependencies ++= testing ++ Seq(asm, scalaLogging),
      // For some reason trying to run veripacks-verifier/compile:doc in SBT generates an error ... maybe that's a
      // 2.10-RC issue. Disabling for now.
      publishArtifact in (Compile, packageDoc) := false
    )
  ) dependsOn(annotations)

  lazy val selfTest: Project = Project(
    "veripacks-self-test",
    file("self-test"),
    settings = buildSettings ++ Seq(
      libraryDependencies ++= testing,
      publishArtifact := false
    )
  ) dependsOn(verifier)
}
