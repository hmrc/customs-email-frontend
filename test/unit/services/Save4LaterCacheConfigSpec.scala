/*
 * Copyright 2020 HM Revenue & Customs
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

package unit.services

import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.customs.emailfrontend.config.AppConfig
import uk.gov.hmrc.customs.emailfrontend.services.Save4LaterCachingConfig
import uk.gov.hmrc.play.bootstrap.http.HttpClient

class Save4LaterCacheConfigSpec extends PlaySpec
  with ScalaFutures
  with MockitoSugar {

  private val mockHttpClient = mock[HttpClient]
  private val mockAppConfig = mock[AppConfig]

  val save4LaterConfig = new Save4LaterCachingConfig(mockHttpClient,mockAppConfig)

  "Save4LaterCachingConfig" should {
    "configure the Caching Config" in {
      save4LaterConfig.baseUri mustBe mockAppConfig.save4LaterBaseUrl
      save4LaterConfig.defaultSource mustBe mockAppConfig.appName
      save4LaterConfig.domain mustBe mockAppConfig.save4LaterDomain
      save4LaterConfig.http mustBe mockHttpClient
    }
  }
}
