import sbt.*

object AppDependencies {

  private val bootstrapVersion = "8.6.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30" % "8.1.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30" % "1.9.0",
    "uk.gov.hmrc" %% "emailaddress" % "3.8.0",
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.17.0",
    "org.typelevel" %% "cats-core" % "2.10.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapVersion % Test,
    "org.jsoup" % "jsoup" % "1.17.2" % Test,
    "org.mockito" %% "mockito-scala-scalatest" % "1.17.31" % Test
  )
}
