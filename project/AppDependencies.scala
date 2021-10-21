import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc" %% "govuk-template" % "5.68.0-play-28",
    "uk.gov.hmrc" %% "play-ui" % "9.4.0-play-28",
    "uk.gov.hmrc" %% "http-caching-client" % "9.3.0-play-28",
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % "5.13.0",
    "uk.gov.hmrc" %% "emailaddress" % "3.5.0",
    "uk.gov.hmrc" %% "play-language" % "4.11.0-play-28",
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.12.3",
    "org.typelevel" %% "cats-core" % "2.3.0"
  )

  val test = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-28" % "5.13.0" % Test,
    "org.jsoup" % "jsoup" % "1.13.1" % Test,
    "com.vladsch.flexmark" % "flexmark-all" % "0.36.8" % "test, it",
    "org.mockito" %% "mockito-scala-scalatest" % "1.16.37" % "test, it"
  )
}
