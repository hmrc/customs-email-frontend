import sbt.*

object AppDependencies {

  private val bootstrapVersion = "9.8.0"

  private val excludeHttpVerbs = ExclusionRule(organization = "uk.gov.hmrc", name = "http-verbs-play-30")

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"                  %% "bootstrap-frontend-play-30" % bootstrapVersion excludeAll excludeHttpVerbs,
    "uk.gov.hmrc"                  %% "play-frontend-hmrc-play-30" % "11.11.0",
    "uk.gov.hmrc.mongo"            %% "hmrc-mongo-play-30"         % "2.4.0",
    "com.fasterxml.jackson.module" %% "jackson-module-scala"       % "2.17.0",
    "org.typelevel"                %% "cats-core"                  % "2.10.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30" % bootstrapVersion % Test excludeAll excludeHttpVerbs,
    "org.jsoup"          % "jsoup"                  % "1.17.2"         % Test,
    "org.scalatestplus" %% "mockito-4-11"           % "3.2.18.0"       % Test
  )
}
