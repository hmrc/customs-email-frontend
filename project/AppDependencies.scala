import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc" %% "govuk-template" % "5.68.0-play-28",
    "uk.gov.hmrc" %% "play-ui" % "9.4.0-play-28",
    "uk.gov.hmrc" %% "http-caching-client" % "9.3.0-play-28",
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % "5.6.0",
    "uk.gov.hmrc" %% "emailaddress" % "3.5.0",
    "uk.gov.hmrc" %% "play-language" % "4.11.0-play-28",
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.12.3"
  )

  val test = Seq(
    "uk.gov.hmrc" %% "webdriver-factory" % "0.16.0",
    "org.scalatest" %% "scalatest" % "3.0.8" % "test",
    "org.jsoup" % "jsoup" % "1.12.1" % "test",
    "com.typesafe.play" %% "play-test" % current % "test",
    "org.pegdown" % "pegdown" % "1.6.0" % "test, it",
    "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % "test, it",
    "org.mockito" % "mockito-core" % "3.7.7" % "test, it",
    "com.github.tomakehurst" % "wiremock-jre8" % "2.27.2" % "test, it"
  )
}
