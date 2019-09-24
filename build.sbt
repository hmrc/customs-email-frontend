import sbt.Configurations.config
import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, defaultSettings, scalaSettings}
import uk.gov.hmrc.SbtArtifactory
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "customs-email-frontend"

lazy val AcceptanceTest = config("acceptance") extend (Test)
lazy val IntegrationTest = config("it") extend Test
lazy val testConfig = Seq(AcceptanceTest, IntegrationTest, Test)

lazy val commonSettings: Seq[Setting[_]] = scalaSettings ++
  publishingSettings ++
  defaultSettings() ++
  gitStampSettings

lazy val unitTestSettings =
  inConfig(Test)(Defaults.testTasks) ++
    Seq(
      testOptions in Test := Seq(Tests.Filter(filterTestsOnPackageName("unit"))),
      testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oD"),
      fork in Test := true,
      unmanagedSourceDirectories in Test := Seq((baseDirectory in Test).value / "test"),
      addTestReportOption(Test, "test-reports")
    )

lazy val integrationTestSettings =
  inConfig(IntegrationTest)(Defaults.testTasks) ++
    Seq(
      testOptions in IntegrationTest := Seq(Tests.Filters(Seq(filterTestsOnPackageName("integration")))),
      testOptions in IntegrationTest += Tests.Argument(TestFrameworks.ScalaTest, "-oD"),
      fork in IntegrationTest := false,
      parallelExecution in IntegrationTest := false,
      addTestReportOption(IntegrationTest, "int-test-reports")
    )

lazy val acceptanceTestSettings =
  inConfig(AcceptanceTest)(Defaults.testTasks) ++
    Seq(
      testOptions in AcceptanceTest := Seq(Tests.Filter(filterTestsOnPackageName("acceptance"))),
      testOptions in AcceptanceTest += Tests.Argument(TestFrameworks.ScalaTest, "-oD"),
      fork in AcceptanceTest := false,
      parallelExecution in AcceptanceTest := false,
      addTestReportOption(AcceptanceTest, "acceptance-test-reports")
    )

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(ScoverageKeys.coverageExcludedPackages := List("<empty>",
    "Reverse.*",
    "uk\\.gov\\.hmrc\\.customs\\.emailfrontend\\.controllers\\.actions\\.ActionsImpl*",
    "uk\\.gov\\.hmrc\\.customs\\.emailfrontend\\.views\\.html\\.partials\\.main_template*",
    "uk\\.gov\\.hmrc\\.customs\\.emailfrontend\\.views\\.html\\.helpers*",
    ".*(BuildInfo|Routes|TestOnly).*").mkString(";"),
    ScoverageKeys.coverageMinimum := 98.7,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    parallelExecution in Test := true)
}

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory)
  .configs(testConfig: _*)
  .settings(
    majorVersion := 0,
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    scoverageSettings,
    publishingSettings,
    unitTestSettings,
    integrationTestSettings,
    acceptanceTestSettings,
    routesImport ++= Seq("uk.gov.hmrc.customs.emailfrontend.model._"),
    resolvers += Resolver.jcenterRepo
  )

PlayKeys.devSettings := Seq("play.server.http.port" -> "9898")

def filterTestsOnPackageName(rootPackage: String): String => Boolean = {
  testName => testName startsWith rootPackage
}
