import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc" %% "govuk-template" % "5.45.0-play-26",
    "uk.gov.hmrc" %% "play-ui" % "8.4.0-play-26",
    "uk.gov.hmrc" %% "http-caching-client" % "9.0.0-play-26",
    "uk.gov.hmrc" %% "bootstrap-play-26" % "1.3.0",
    "uk.gov.hmrc" %% "emailaddress" % "3.4.0",
    "uk.gov.hmrc" %% "play-language" % "4.2.0-play-26"
  )

  val test = Seq(
    "uk.gov.hmrc" %% "webdriver-factory" % "0.7.0",
    "org.scalatest" %% "scalatest" % "3.0.8" % "test",
    "org.jsoup" % "jsoup" % "1.12.1" % "test",
    "com.typesafe.play" %% "play-test" % current % "test",
    "org.pegdown" % "pegdown" % "1.6.0" % "test, it",
    "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % "test, it",
    "org.mockito" % "mockito-core" % "3.1.0" % "test, it",
    "com.github.tomakehurst" % "wiremock-standalone" % "2.25.1" % "test"
  )
}
