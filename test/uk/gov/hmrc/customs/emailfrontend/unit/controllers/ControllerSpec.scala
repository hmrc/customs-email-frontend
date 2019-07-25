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
import org.scalatest.{Matchers, WordSpecLike}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.MessagesApi
import play.api.mvc.{AnyContent, BodyParser}
import play.api.test.Helpers
import play.api.{Environment, Play}
import uk.gov.hmrc.customs.emailfrontend.config.AppConfig
import uk.gov.hmrc.customs.emailfrontend.unit.{AuthBuilder, FakeAction}

trait ControllerSpec extends WordSpecLike with Matchers with MockitoSugar with GuiceOneAppPerSuite with AuthBuilder {

  implicit def materializer: Materializer = Play.materializer

  implicit def messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  implicit def bodyParser: BodyParser[AnyContent] = app.injector.instanceOf[BodyParser[AnyContent]]

  val env: Environment = Environment.simple()

  private val mockAppConfig = mock[AppConfig]
  private val cc = Helpers.stubControllerComponents()

  val fakeAction = new FakeAction(mockAuthConnector, cc.parsers.defaultBodyParser)(cc.messagesApi, mockAppConfig, cc.executionContext)
}
