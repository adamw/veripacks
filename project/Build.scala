import sbt._
import sbt.Keys._
import scala.Some

object BuildSettings {
  val buildSettings = Defaults.defaultSettings ++ Seq (
    organization  := "org.veripacks",
    version       := "0.4-SNAPSHOT",
    scalaVersion  := "2.10.0",
    // Sonatype OSS deployment
    publishTo <<= version { (v: String) =>
      val nexus = "https://oss.sonatype.org/"
      if (v.trim.endsWith("SNAPSHOT"))
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    },
    credentials   += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => false },
    pomExtra := (
      <scm>
        <url>git@github.com:adamw/veripacks.git</url>
        <connection>scm:git:git@github.com:adamw/veripacks.git</connection>
      </scm>
        <developers>
          <developer>
            <id>adamw</id>
            <name>Adam Warski</name>
            <url>http://www.warski.org</url>
          </developer>
        </developers>),
    scalacOptions += "-unchecked",
    homepage      := Some(new java.net.URL("http://www.veripacks.org")),
    licenses      := ("Apache2", new java.net.URL("http://www.apache.org/licenses/LICENSE-2.0.txt")) :: Nil
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
      libraryDependencies ++= testing ++ Seq(asm, scalaLogging)
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
