import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings.{itSettings, targetJvm}

val appName = "customs-email-frontend"

val silencerVersion = "1.7.16"
val scala2_13_12 = "2.13.12"
val bootstrap = "8.6.0"

val scalaStyleConfigFile = "scalastyle-config.xml"
val testScalaStyleConfigFile = "test-scalastyle-config.xml"
val testDirectory = "test"

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := scala2_13_12

lazy val scalastyleSettings = Seq(scalastyleConfig := baseDirectory.value /  scalaStyleConfigFile,
  (Test / scalastyleConfig) := baseDirectory.value/ testDirectory /  testScalaStyleConfigFile)

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(itSettings())
  .settings(libraryDependencies ++= Seq("uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrap % Test))

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
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
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(scalastyleSettings)

addCommandAlias("runAllChecks", ";clean;compile;coverage;test;it/test;scalastyle;Test/scalastyle;coverageReport")
