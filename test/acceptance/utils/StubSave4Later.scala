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
import org.joda.time.format.ISODateTimeFormat
import play.api.http.Status
import play.api.libs.json.Json
import uk.gov.hmrc.crypto.CompositeSymmetricCrypto.aes
import uk.gov.hmrc.crypto.{CompositeSymmetricCrypto, PlainText}
import uk.gov.hmrc.customs.emailfrontend.DateTimeUtil
import uk.gov.hmrc.customs.emailfrontend.model.EmailDetails

trait StubSave4Later {

  private val crypto: CompositeSymmetricCrypto = aes("fqpLDZ4smuDsekHkeEBlCA==", Seq.empty)

  private val save4LaterGetUrl = (internalId: String) => s"/save4later/customs-email-frontend/$internalId"
  private val save4LaterPutUrl = (internalId: String) => s"/save4later/customs-email-frontend/$internalId/data/emailDetails"
  private val emailVerified = EmailDetails("b@a.com", None)
  private val emailVerifiedJson = Json.toJson(emailVerified).toString()
  private val today =  DateTimeUtil.dateTime.toString(ISODateTimeFormat.dateTimeNoMillis().withZoneUTC())

  private val encryptedEmail = encrypt(emailVerifiedJson)

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

  def encrypt(str: String): String = crypto.encrypt(PlainText(str)).value

}
