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

package acceptance.wiremockstub

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.joda.time.DateTime
import play.api.http.Status
import play.api.libs.json.Json
import uk.gov.hmrc.crypto.CompositeSymmetricCrypto.aes
import uk.gov.hmrc.crypto.{CompositeSymmetricCrypto, PlainText}
import uk.gov.hmrc.customs.emailfrontend.model.EmailDetails

trait StubSave4Later {

  private val crypto: CompositeSymmetricCrypto = aes("fqpLDZ4smuDsekHkeEBlCA==", Seq.empty)

  private val save4LaterGetUrl = (internalId: String) => s"/save4later/customs-email-frontend/$internalId"
  private val save4LaterPutUrl = (internalId: String) => s"/save4later/customs-email-frontend/$internalId/data/emailDetails"
  val emailDetails = EmailDetails(None, "b@a.com",  None)
  val emailDetailsWithPreviousEmail = EmailDetails(Some("old@email.com"), "b@a.com",  None)
  val emailDetailsWithTimestamp = EmailDetails(None, "b@a.com", Some(DateTime.now().minusHours(1)))
  val emailDetailsWithTimestampOver2Hours = EmailDetails(None, "b@a.com", Some(DateTime.now().minusHours(3)))

  private def encryptEmailDetails : EmailDetails => String = emailDetails => encrypt(Json.toJson(emailDetails).toString())

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

  def encrypt(str: String): String = crypto.encrypt(PlainText(str)).value

  def save4LaterWithData(internalId: String)(emailDetails: EmailDetails): StubMapping = {
    stubFor(get(urlEqualTo(save4LaterGetUrl(internalId)))
      .willReturn(
        aResponse()
          .withStatus(Status.OK)
          .withBody(s"""{"data": {"emailDetails": "${encryptEmailDetails(emailDetails)}"}, "id": "$internalId"}""".stripMargin)
      )
    )

    stubFor(delete(urlEqualTo(save4LaterGetUrl(internalId)))
      .willReturn(
        aResponse()
          .withStatus(Status.OK)
      )
    )
  }
}
