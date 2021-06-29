import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc" %% "govuk-template" % "5.68.0-play-28",
    "uk.gov.hmrc" %% "play-ui" % "9.6.0-play-28",
    "uk.gov.hmrc" %% "http-caching-client" % "9.5.0-play-28",
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % "5.5.0",
    "uk.gov.hmrc" %% "emailaddress" % "3.5.0",
    "uk.gov.hmrc" %% "play-language" % "5.1.0-play-28",
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.12.3"
  )

  val test = Seq(
    "uk.gov.hmrc" %% "webdriver-factory" % "0.22.0",
    "org.scalatest" %% "scalatest" % "3.0.8" % "test",
    "org.jsoup" % "jsoup" % "1.13.1" % "test",
    "com.typesafe.play" %% "play-test" % current % "test",
    "org.pegdown" % "pegdown" % "1.6.0" % "test, it",
    "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % "test, it",
    "org.scalatestplus" %% "mockito-3-4" % "3.2.9.0" % "test",
    "org.mockito" % "mockito-core" % "3.11.2" % "test, it",
    "com.github.tomakehurst" % "wiremock-jre8" % "2.28.1" % "test, it",
    "com.vladsch.flexmark" % "flexmark-all" % "0.35.10" % "test"
  )
}
