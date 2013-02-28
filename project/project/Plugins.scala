import sbt._

object Plugins extends Build {
  lazy val plugins = Project("plugins", file("."))
    .dependsOn(
    uri("git://github.com/guardian/sbt-teamcity-test-reporting-plugin.git#v1.3")
  )
}