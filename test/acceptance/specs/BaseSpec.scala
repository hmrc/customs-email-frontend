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

package acceptance.specs

import acceptance.utils.StubForWireMock
import org.scalatest.{BeforeAndAfterAll, FeatureSpec, GivenWhenThen}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import utils.{Constants, WireMockRunner}

trait BaseSpec extends FeatureSpec with GivenWhenThen with GuiceOneServerPerSuite with BeforeAndAfterAll with WireMockRunner with StubForWireMock {

  override lazy val port = Option(System.getProperty("port")).fold(9000)(_.toInt)

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(Map("metrics.enabled" -> false,
      "microservice.services.auth.port" -> Constants.wireMockPort,
      "microservice.services.cachable.short-lived-cache.port" -> Constants.wireMockPort))
    .disable[com.kenshoo.play.metrics.PlayModule]
    .build()

  override def beforeAll: Unit = {
    startMockServer()
  }

  override def afterAll: Unit = {
    stopMockServer()
  }
}
