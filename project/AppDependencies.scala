import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc" %% "govuk-template" % "5.36.0-play-26",
    "uk.gov.hmrc" %% "play-ui" % "7.40.0-play-26",
    "uk.gov.hmrc" %% "http-caching-client" % "8.4.0-play-26",
    "uk.gov.hmrc" %% "bootstrap-play-26" % "0.42.0",
    "uk.gov.hmrc" %% "emailaddress" % "3.2.0"
  )

  val test = Seq(
    "uk.gov.hmrc" %% "bootstrap-play-26" % "0.42.0" % Test classifier "tests",
    "uk.gov.hmrc" %% "webdriver-factory" % "0.7.0",
    "org.scalatest" %% "scalatest" % "3.0.8" % "test",
    "org.jsoup" % "jsoup" % "1.12.1" % "test",
    "com.typesafe.play" %% "play-test" % current % "test",
    "org.pegdown" % "pegdown" % "1.6.0" % "test, it",
    "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % "test, it",
    "org.mockito" % "mockito-core" % "3.0.0" % "test,it",
    "com.github.tomakehurst" % "wiremock-standalone" % "2.23.2" % "test"
  )
}
