import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings.{itSettings, targetJvm}

val appName = "customs-email-frontend"

val silencerVersion = "1.17.13"
val scala2_13_8 = "2.13.8"
val bootstrap = "7.22.0"

val scalaStyleConfigFile = "scalastyle-config.xml"
val testScalaStyleConfigFile = "test-scalastyle-config.xml"
val testDirectory = "test"

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := scala2_13_8

lazy val scalastyleSettings = Seq(scalastyleConfig := baseDirectory.value /  scalaStyleConfigFile,
  (Test / scalastyleConfig) := baseDirectory.value/ testDirectory /  testScalaStyleConfigFile)

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(itSettings())
  .settings(libraryDependencies ++= Seq("uk.gov.hmrc" %% "bootstrap-test-play-28" % bootstrap % Test))

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    targetJvm := "jvm-11",
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,

    ScoverageKeys.coverageExcludedFiles := "<empty>;Reverse.*;.*filters.*;.*handlers.*;.*components.*;" +
      ".*javascript.*;.*Routes.*;.*GuiceInjector;" +
      ".*FeatureSwitchController;" +
      ".*views.*;" +
      ".*ControllerConfiguration;.*LanguageSwitchController",
    ScoverageKeys.coverageMinimumStmtTotal := 85,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,

    scalacOptions ++= Seq(
      "-Wunused:imports",
      "-Wunused:patvars",
      "-Wunused:implicits",
      "-Wunused:explicits",
      "-Wunused:privates",
      "-P:silencer:pathFilters=target/.*",
      "-P:silencer:pathFilters=routes"),

    Test / scalacOptions ++= Seq(
      "-Wunused:imports",
      "-Wunused:params",
      "-Wunused:patvars",
      "-Wunused:implicits",
      "-Wunused:explicits",
      "-Wunused:privates"),

    libraryDependencies ++= Seq(
      compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
      "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
    ),

    routesImport ++= Seq("uk.gov.hmrc.customs.emailfrontend.model.Ineligible"),
    TwirlKeys.templateImports ++= Seq(
      "play.twirl.api.HtmlFormat",
      "controllers.routes._",
      "uk.gov.hmrc.govukfrontend.views.html.components._"
    ),
  )
  .settings(PlayKeys.playDefaultPort := 9898)
  .configs(IntegrationTest)
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(scalastyleSettings)
