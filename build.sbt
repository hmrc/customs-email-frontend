import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings
import uk.gov.hmrc.SbtArtifactory
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "customs-email-frontend"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory)
  .settings(
    majorVersion := 0,
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    scoverageSettings
  )
  .settings(publishingSettings: _*)
  .configs(IntegrationTest)
  .settings(integrationTestSettings(): _*)
  .settings(resolvers += Resolver.jcenterRepo)

PlayKeys.devSettings := Seq("play.server.http.port" -> "9898")

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(ScoverageKeys.coverageExcludedPackages := List("<empty>",
    "Reverse.*",
    "uk\\.gov\\.hmrc\\.customs\\.emailfrontend\\.controllers\\.actions\\.ActionsImpl*",
    "uk\\.gov\\.hmrc\\.customs\\.emailfrontend\\.views\\.html\\.partials*",
    ".*(BuildInfo|Routes|TestOnly).*").mkString(";"),
    ScoverageKeys.coverageMinimum := 97,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    parallelExecution in Test := true)
}