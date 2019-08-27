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

package uk.gov.hmrc.customs.emailfrontend.unit.controllers

import akka.stream.Materializer
import org.scalatest.{Matchers, OptionValues, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.MessagesApi
import play.api.mvc._
import play.api.test.CSRFTokenHelper.CSRFFRequestHeader
import play.api.test.FakeRequest
import play.api.{Configuration, Environment, Mode, Play}
import play.utils.OrderPreserving.groupBy
import uk.gov.hmrc.customs.emailfrontend.config.AppConfig
import uk.gov.hmrc.customs.emailfrontend.unit.{AuthBuilder, FakeAction}
import uk.gov.hmrc.play.bootstrap.config.{RunMode, ServicesConfig}

import scala.concurrent.ExecutionContext


trait ControllerSpec extends WordSpec with Matchers with MockitoSugar with GuiceOneAppPerSuite with AuthBuilder with OptionValues {

  implicit def materializer: Materializer = Play.materializer

  implicit def messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  implicit def bodyParser: BodyParser[AnyContent] = app.injector.instanceOf[BodyParser[AnyContent]]

  val env: Environment = Environment.simple()

  private val config = Configuration.load(env)

  private val serviceConfig = new ServicesConfig(config, new RunMode(config, Mode.Dev))

  implicit val appConfig: AppConfig = new AppConfig(config, serviceConfig)

  val request: RequestHeader = FakeRequest("GET", "/").withCSRFToken

  implicit val cc: ControllerComponents = app.injector.instanceOf[ControllerComponents]

  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  implicit val mcc: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]

  val fakeAction = new FakeAction(mockAuthConnector, cc.parsers.defaultBodyParser)(cc.messagesApi, cc.executionContext)

  private def formUrlEncodedBody(data: Seq[(String, String)]) =
    AnyContentAsFormUrlEncoded(groupBy(data)(_._1))

  def requestWithForm(data: (String, String)*): Request[AnyContentAsFormUrlEncoded] =
    Request(FakeRequest("GET", "/").withCSRFToken, formUrlEncodedBody(data))

}
