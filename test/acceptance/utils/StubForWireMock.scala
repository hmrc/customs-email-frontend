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
import play.api.http.Status
import play.api.libs.json.Json
import play.mvc.Http.HeaderNames.CONTENT_TYPE
import play.mvc.Http.MimeTypes.JSON
import play.mvc.Http.Status.CREATED
import uk.gov.hmrc.customs.emailfrontend.connectors.EmailVerificationKeys._

trait StubForWireMock {

  val eoriNumber = "123456789"
  val authUrl = "/auth/authorise"
  val save4LaterGetUrl = s"/save4later/customs-email-frontend/$eoriNumber"
  val save4LaterPutUrl = s"/save4later/customs-email-frontend/$eoriNumber/data/email"
  val emailVerificationPostUrl = "/email-verification/verification-requests"


  def authRequestJson() =
    """{
      |"authorise" : [{
      | "enrolment" : "HMRC-CUS-ORG",
      | "identifiers" : [],
      | "state" : "Activated"
      |}],
      | "retrieve" : ["allEnrolments","internalId"]
      |}
    """.stripMargin

  def authenticate() = {
    stubFor(post(urlEqualTo(authUrl))
      .withRequestBody(equalToJson(authRequestJson()))
      .willReturn(
        aResponse()
          .withStatus(Status.OK)
          .withBody(
            s"""{"allEnrolments": [{
               | "key": "HMRC-CUS-ORG",
               | "identifiers": [
               |   {
               |     "key": "EORINumber",
               |     "value": "$eoriNumber"
               |   }
               | ]
               |}
               |],
            "internalId": "$eoriNumber"}""".stripMargin)
      )
    )
  }


  def authRequestJsonGG() =
    """{
      |"authorise" : [{
      | "authProviders" : ["GovernmentGateway"]
      |}],
      | "retrieve" : ["allEnrolments","internalId"]
      |}
    """.stripMargin


  def authenticateGG() = {
    stubFor(post(urlEqualTo(authUrl))
      .withRequestBody(equalToJson(authRequestJsonGG()))
      .willReturn(
        aResponse()
          .withStatus(Status.OK)
          .withBody(s"""{"allEnrolments": [], "internalId": "$eoriNumber"}""".stripMargin)
      )
    )
  }

  def verificationRequestJson(): String = {
    """{
      |"email":"b@a.com",
      |"templateId" : "verifyEmailAddress",
      |"templateParameters":{},
      |"linkExpiryDuration":"P3D",
      |"continueUrl":"/customs-email-frontend/email-address-confirmed"
      |}
    """.stripMargin
  }

  def stubVerificationRequest(url: String, response: String,status: Int): Unit = {
    stubFor(post(urlEqualTo(url))
      .withRequestBody(equalToJson(verificationRequestJson()))
      .willReturn(
        aResponse()
          .withBody(response)
          .withStatus(status)
          .withHeader(CONTENT_TYPE, JSON)
      )
    )
  }

  def stubVerificationRequestSent() = {
    stubVerificationRequest(emailVerificationPostUrl, "", CREATED)
  }


  def save4LaterWithNoData() = {
    stubFor(get(urlEqualTo(save4LaterGetUrl))
      .willReturn(
        aResponse()
          .withStatus(Status.OK)
          .withBody("""{"data": {}, "id": ""}""".stripMargin)
      )
    )

    stubFor(put(urlEqualTo(save4LaterPutUrl))
      .willReturn(
        aResponse()
          .withStatus(Status.OK)
          .withBody("""{"data": {}, "id": ""}""".stripMargin)
      )
    )
  }

  def save4LaterWithData() = {
    val encryptedEmail = "YKEtCuoQiCSDa7UDy8cs/mhnhVx31sNgNMJ3yXL47rLKc5P2y6Vk4Nsv4fn+OapA"
    stubFor(get(urlEqualTo(save4LaterGetUrl))
      .willReturn(
        aResponse()
          .withStatus(Status.OK)
          .withBody(s"""{"data": {"email": "$encryptedEmail"}, "id": "1"}""".stripMargin)
      )
    )
  }
}
