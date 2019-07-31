/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.customs.emailfrontend.unit.views

import akka.util.Timeout
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.{Configuration, Environment, Mode}
import uk.gov.hmrc.customs.emailfrontend.config.AppConfig
import uk.gov.hmrc.play.bootstrap.config.{RunMode, ServicesConfig}

import scala.concurrent.duration._


trait ViewSpec extends PlaySpec with GuiceOneAppPerSuite {

  implicit val timeout: Timeout = 30.seconds
  private val messageApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val messages: Messages = MessagesImpl(Lang("en"), messageApi)
  implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  val env: Environment = Environment.simple()
  val config: Configuration = Configuration.load(env)
  val serviceConfig = new ServicesConfig(config, new RunMode(config, Mode.Dev))

  implicit val appConfig: AppConfig = new AppConfig(config, serviceConfig)

}
