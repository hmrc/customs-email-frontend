/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.customs.emailfrontend.utils

import com.codahale.metrics.MetricRegistry
import org.mockito.Mockito.reset
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.CSRFTokenHelper.*
import play.api.test.FakeRequest
import uk.gov.hmrc.customs.emailfrontend.config.{AppConfig, ErrorHandler}
import uk.gov.hmrc.customs.emailfrontend.controllers.actions.IdentifierAction
import uk.gov.hmrc.customs.emailfrontend.utils.Utils.emptyString
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.metrics.Metrics

import scala.annotation.implicitNotFound
import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag

trait SpecBase
    extends AnyWordSpecLike
    with Matchers
    with MockitoSugar
    with OptionValues
    with ScalaFutures
    with BeforeAndAfterEach {

  def fakeRequest(method: String, path: String): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(method, path)
      .withHeaders("X-Session-ID" -> "someSessionId")

  def fakeRequestWithCsrf(method: String, path: String): FakeRequest[AnyContentAsEmpty.type] =
    fakeRequest(method, path).withCSRFToken
      .asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  @implicitNotFound("Pass a type for the identifier action")
  def applicationBuilder(
    disableAuth: Boolean = false
  ): GuiceApplicationBuilder = {

    val overrides: List[GuiceableModule]         = List(bind[Metrics].toInstance(new FakeMetrics))
    val optionalOverrides: List[GuiceableModule] = if (disableAuth) {
      Nil
    } else {
      List(bind[IdentifierAction].to[FakeIdentifierAgentAction])
    }

    new GuiceApplicationBuilder()
      .overrides(overrides ::: optionalOverrides: _*)
      .configure(
        "play.filters.csp.nonce.enabled"        -> false,
        "auditing.enabled"                      -> "false",
        "microservice.metrics.graphite.enabled" -> "false",
        "metrics.enabled"                       -> "false"
      )
  }

  def app: Application = applicationBuilder().build()

  lazy implicit val hc: HeaderCarrier    = HeaderCarrier()
  lazy implicit val ec: ExecutionContext = ExecutionContext.global
  lazy val mockAppConfig: AppConfig      = mock[AppConfig]
  lazy val appConfigInstance: AppConfig  = app.injector.instanceOf[AppConfig]

  implicit lazy val messages: Messages =
    app.injector.instanceOf[MessagesApi].preferred(fakeRequest(emptyString, emptyString))

  lazy val errorHandler: ErrorHandler = app.injector.instanceOf[ErrorHandler]

  override def beforeEach(): Unit = reset(mockAppConfig)
}

class FakeMetrics extends Metrics {
  override val defaultRegistry: MetricRegistry = new MetricRegistry
}

object TestImplicits {
  implicit class RemoveCsrf(s: String) {
    def removeCsrf(): String = {
      val regEx = "<[/]?input type[^>]*>"
      s.replaceAll(regEx, emptyString)
    }
  }
}
