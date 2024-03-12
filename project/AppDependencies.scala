import sbt.*

object AppDependencies {

  private val bootstrapVersion = "8.5.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-29" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-29" % "8.1.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-29" % "1.7.0",
    "uk.gov.hmrc" %% "emailaddress" % "3.8.0",
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.16.1",
    "org.typelevel" %% "cats-core" % "2.10.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-29" % bootstrapVersion % Test,
    "org.jsoup" % "jsoup" % "1.17.2" % Test,
    "com.vladsch.flexmark" % "flexmark-all" % "0.64.8" % Test,
    "org.mockito" %% "mockito-scala-scalatest" % "1.17.30" % Test
  )
}
