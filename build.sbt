import sbt.Configurations.config
import sbt.Keys.testGrouping
import sbt.Tests.{Group, SubProcess}
import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, defaultSettings, scalaSettings, targetJvm}
import uk.gov.hmrc.gitstamp.GitStampPlugin.gitStampSettings
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "customs-email-frontend"

lazy val AcceptanceTest = config("acceptance") extend (Test)
lazy val IntegrationTest = config("it") extend Test
lazy val EndToEndTest = config("endtoend") extend Test
lazy val testConfig = Seq(EndToEndTest, AcceptanceTest, IntegrationTest, Test)

lazy val commonSettings: Seq[Setting[_]] = scalaSettings ++ publishingSettings ++ defaultSettings() ++ gitStampSettings

def forkedJvmPerTestConfig(tests: Seq[TestDefinition], packages: String*): Seq[Group] =
  tests.groupBy(_.name.takeWhile(_ != '.')).filter(packageAndTests => packages contains packageAndTests._1) map {
    case (packg, theTests) =>
      Group(packg, theTests, SubProcess(ForkOptions()))
  } toSeq
  
lazy val unitTestSettings =
  inConfig(Test)(Defaults.testTasks) ++
    Seq(
      testOptions in Test := Seq(Tests.Filter(filterTestsOnPackageName("unit"))),
      testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oD"),
      fork in Test := true,
      unmanagedSourceDirectories in Test := Seq((baseDirectory in Test).value / "test"),
      addTestReportOption(Test, "test-reports")
    )

def integrationTestFilter(name: String): Boolean = name startsWith "integration"
lazy val integrationTestSettings =
  inConfig(IntegrationTest)(Defaults.testTasks) ++
    Seq(
      testOptions in IntegrationTest := Seq(Tests.Filter(integrationTestFilter)),
      testOptions in IntegrationTest += Tests.Argument(TestFrameworks.ScalaTest, "-oD"),
      fork in IntegrationTest := false,
      parallelExecution in IntegrationTest := false,
      addTestReportOption(IntegrationTest, "int-test-reports"),
      testGrouping in IntegrationTest := forkedJvmPerTestConfig((definedTests in Test).value, "integration")      
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

lazy val endtoendTestSettings =
  inConfig(EndToEndTest)(Defaults.testTasks) ++
    Seq(
      testOptions in EndToEndTest := Seq(Tests.Filter(filterTestsOnPackageName("endtoend"))),
      testOptions in EndToEndTest += Tests.Argument(TestFrameworks.ScalaTest, "-oD"),
      fork in EndToEndTest := false,
      parallelExecution in EndToEndTest := false,
      addTestReportOption(EndToEndTest, "endtoend-test-reports")
    )

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(ScoverageKeys.coverageExcludedPackages := List("<empty>",
    "Reverse.*",
    "uk\\.gov\\.hmrc\\.customs\\.emailfrontend\\.controllers\\.actions\\.ActionsImpl*",
    "uk\\.gov\\.hmrc\\.customs\\.emailfrontend\\.views\\.html\\.partials\\.main_template*",
    "uk\\.gov\\.hmrc\\.customs\\.emailfrontend\\.views\\.html\\.helpers*",
    ".*(BuildInfo|Routes|TestOnly).*").mkString(";"),
    ScoverageKeys.coverageMinimum := 98.90,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    parallelExecution in Test := false)
}

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin)
  .configs(testConfig: _*)
  .settings(
    scalaVersion := "2.12.10",
    targetJvm := "jvm-1.8",
    majorVersion := 0,
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    scoverageSettings,
    publishingSettings,
    unitTestSettings,
    integrationTestSettings,
    acceptanceTestSettings,
    endtoendTestSettings,
    routesImport ++= Seq("uk.gov.hmrc.customs.emailfrontend.model._"),
    resolvers += Resolver.jcenterRepo,
    resolvers += "hmrc-releases" at "https://artefacts.tax.service.gov.uk/artifactory/hmrc-releases/"
  )

PlayKeys.devSettings := Seq("play.server.http.port" -> "9898")

def filterTestsOnPackageName(rootPackage: String): String => Boolean = {
  testName => testName startsWith rootPackage
}
