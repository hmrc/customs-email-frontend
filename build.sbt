import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings.itSettings

val appName = "customs-email-frontend"

val silencerVersion = "1.7.16"
val scala3_3_3 = "3.3.3"
val bootstrap = "9.1.0"

val scalaStyleConfigFile = "scalastyle-config.xml"
val testScalaStyleConfigFile = "test-scalastyle-config.xml"
val testDirectory = "test"

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := scala3_3_3

lazy val scalastyleSettings = Seq(scalastyleConfig := baseDirectory.value / scalaStyleConfigFile,
  (Test / scalastyleConfig) := baseDirectory.value / testDirectory / testScalaStyleConfigFile)

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(itSettings())
  .settings(libraryDependencies ++= Seq("uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrap % Test))

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,

    ScoverageKeys.coverageExcludedFiles := "<empty>;Reverse.*;.*filters.*;.*handlers.*;.*components.*;" +
      ".*javascript.*;.*Routes.*;.*GuiceInjector;" +
      ".*FeatureSwitchController;" +
      ".*views.*;" +
      ".*ControllerConfiguration;.*LanguageSwitchController",
    ScoverageKeys.coverageMinimumStmtTotal := 85,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true,

    scalacOptions := scalacOptions.value.diff(Seq("-Wunused:all")),
    Test / scalacOptions ++= Seq(
      "-Wunused:imports",
      "-Wunused:params",
      "-Wunused:implicits",
      "-Wunused:explicits",
      "-Wunused:privates"),

    libraryDependencies ++= Seq(
      compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.for3Use2_13With("", ".12")),
      "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.for3Use2_13With("", ".12")),

    routesImport ++= Seq("uk.gov.hmrc.customs.emailfrontend.model.Ineligible"),
    TwirlKeys.templateImports ++= Seq(
      "play.twirl.api.HtmlFormat",
      "controllers.routes._",
      "uk.gov.hmrc.govukfrontend.views.html.components._"
    ),
    scalafmtDetailedError := true,
    scalafmtPrintDiff := true,
    scalafmtFailOnErrors := true
  )
  .settings(PlayKeys.playDefaultPort := 9898)
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(scalastyleSettings)

addCommandAlias("runAllChecks",
  ";clean;compile;coverage;test;it/test;scalafmtCheckAll;scalastyle;Test/scalastyle;coverageReport")
