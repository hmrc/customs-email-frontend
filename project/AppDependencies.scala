import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % "7.19.0",
    "uk.gov.hmrc" %% "play-frontend-hmrc" % "7.14.0-play-28",
    "uk.gov.hmrc" %% "http-caching-client" % "10.0.0-play-28",
    "uk.gov.hmrc" %% "emailaddress" % "3.8.0",
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.15.2",
    "org.typelevel" %% "cats-core" % "2.9.0"
  )

  val test = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-28" % "7.19.0" % Test,
    "org.jsoup" % "jsoup" % "1.16.1" % Test,
    "com.vladsch.flexmark" % "flexmark-all" % "0.36.8" % "test, it",
    "org.mockito" %% "mockito-scala-scalatest" % "1.17.14" % "test, it",
    "uk.gov.hmrc" %% "play-frontend-hmrc" % "7.14.0-play-28"
  )
}
