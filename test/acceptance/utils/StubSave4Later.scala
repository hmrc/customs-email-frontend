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

package acceptance.utils

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status

trait StubSave4Later {

  private val save4LaterGetUrl = (internalId: String) => s"/save4later/customs-email-frontend/$internalId"
  private val save4LaterPutUrl = (internalId: String) => s"/save4later/customs-email-frontend/$internalId/data/email"
  private val encryptedEmail = "YKEtCuoQiCSDa7UDy8cs/mhnhVx31sNgNMJ3yXL47rLKc5P2y6Vk4Nsv4fn+OapA" //encrypted value for b@a.com

  def save4LaterWithNoData(internalId: String): StubMapping = {
    stubFor(get(urlEqualTo(save4LaterGetUrl(internalId)))
      .willReturn(
        aResponse()
          .withStatus(Status.OK)
          .withBody("""{"data": {}, "id": ""}""".stripMargin)
      )
    )

    stubFor(put(urlEqualTo(save4LaterPutUrl(internalId)))
      .willReturn(
        aResponse()
          .withStatus(Status.OK)
          .withBody("""{"data": {}, "id": ""}""".stripMargin)
      )
    )
  }

  def save4LaterWithData(internalId: String): StubMapping = {
    stubFor(get(urlEqualTo(save4LaterGetUrl(internalId)))
      .willReturn(
        aResponse()
          .withStatus(Status.OK)
          .withBody(s"""{"data": {"email": "$encryptedEmail"}, "id": "1"}""".stripMargin)
      )
    )
  }
}
