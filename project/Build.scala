import sbt._
import Keys._

object BuildSettings {
  val buildSettings = Defaults.defaultSettings ++ Seq (
    organization  := "org.veripacks",
    version       := "0.0.1-SNAPSHOT",
    scalaVersion  := "2.9.2",
    scalacOptions += "",
    licenses      := ("Apache2", new java.net.URL("http://www.apache.org/licenses/LICENSE-2.0.txt")) :: Nil
  )
}

object Dependencies {
  val javassist     = "org.javassist"             % "javassist"             % "3.16.1-GA"

  val scalatest     = "org.scalatest"             %% "scalatest"            % "1.8"       % "test"
  val mockito       = "org.mockito"               % "mockito-core"          % "1.9.5"     % "test"

  val testing = Seq(scalatest, mockito)
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
    settings = buildSettings ++ Seq(libraryDependencies ++= testing ++ Seq(javassist))
  ) dependsOn(annotations)
}
