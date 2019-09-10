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
import com.github.tomakehurst.wiremock.matching.UrlPattern
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status
import play.mvc.Http.HeaderNames.CONTENT_TYPE
import play.mvc.Http.MimeTypes.JSON

trait StubForWireMock {

  val eoriNumber = "123456789"
  val authUrl = "/auth/authorise"
  val save4LaterGetUrl = s"/save4later/customs-email-frontend/$eoriNumber"
  val save4LaterPutUrl = s"/save4later/customs-email-frontend/$eoriNumber/data/email"
  val emailVerificationPostUrl = "/email-verification/verification-requests"
  val verifiedEmailPostUrl = "/email-verification/verified-email-check"
  val encryptedEmail = "YKEtCuoQiCSDa7UDy8cs/mhnhVx31sNgNMJ3yXL47rLKc5P2y6Vk4Nsv4fn+OapA"
  val customsDataStoreGraphQl = "/customs-data-store/graphql"


  private val customsDataStoreContextPath: UrlPattern = urlMatching(customsDataStoreGraphQl)
  private val verifiedEmailContextPath: UrlPattern = urlMatching("/email-verification/verified-email-check")

  private def authRequestJson(): String =
    """{
      |"authorise" : [{
      | "enrolment" : "HMRC-CUS-ORG",
      | "identifiers" : [],
      | "state" : "Activated"
      |}],
      | "retrieve" : ["allEnrolments","internalId"]
      |}
    """.stripMargin

  def authenticate(): StubMapping = {
    stubFor(post(urlEqualTo(authUrl))
      .withRequestBody(equalToJson(authRequestJson()))
      .willReturn(
        aResponse()
          .withStatus(Status.OK)
          .withBody(
            s"""{"allEnrolments": [
               |  {
               | "key": "HMRC-CUS-ORG",
               | "identifiers": [
               |   {
               |     "key": "EORINumber",
               |     "value": "$eoriNumber"
               |   }
               | ]
               |}
               |],
               |"internalId": "$eoriNumber"
               |}
              """.stripMargin)
      )
    )
  }

  private def authGGRequestJson(): String =
    """{
      |"authorise" : [{
      | "authProviders" : ["GovernmentGateway"]
      |}],
      | "retrieve" : ["allEnrolments","internalId"]
      |}
    """.stripMargin

  def authenticateGG(): StubMapping = {
    stubFor(post(urlEqualTo(authUrl))
      .withRequestBody(equalToJson(authGGRequestJson()))
      .willReturn(
        aResponse()
          .withStatus(Status.OK)
          .withBody(s"""{"allEnrolments": [], "internalId": "$eoriNumber"}""".stripMargin)
      )
    )
  }

  def authenticateGGUserAndReturnEoriEnrolment(): StubMapping = {
    stubFor(post(urlEqualTo(authUrl))
      .withRequestBody(equalToJson(authGGRequestJson()))
      .willReturn(
        aResponse()
          .withStatus(Status.OK)
          .withBody(
            s"""{"allEnrolments": [
               |  {
               | "key": "HMRC-CUS-ORG",
               | "identifiers": [
               |   {
               |     "key": "EORINumber",
               |     "value": "$eoriNumber"
               |   }
               | ]
               |}
               |],
               |"internalId": "$eoriNumber"
               |}
              """.stripMargin)
      )
    )
  }

  private def verificationRequestJson(): String = {
    """{
      |"email":"b@a.com",
      |"templateId" : "verifyEmailAddress",
      |"templateParameters":{},
      |"linkExpiryDuration":"P3D",
      |"continueUrl":"/customs-email-frontend/email-address-confirmed"
      |}
    """.stripMargin
  }

  private def verifiedEmailRequestJson(): String = {
    """{
      |"email" : "b@a.com"
      |}""".stripMargin
  }

  private def stubVerificationRequest(url: String, status: Int): Unit = {
    stubFor(post(urlEqualTo(url))
      .withRequestBody(equalToJson(verificationRequestJson()))
      .willReturn(
        aResponse()
          .withStatus(status)
          .withHeader(CONTENT_TYPE, JSON)
      )
    )
  }

  def stubVerificationRequestSent(): Unit = {
    stubVerificationRequest(emailVerificationPostUrl, Status.CREATED)
  }

  def stubEmailAlreadyVerified(): Unit = {
    stubVerificationRequest(emailVerificationPostUrl, Status.CONFLICT)
  }

  def save4LaterWithNoData(): StubMapping = {
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

  def save4LaterWithData(): StubMapping = {
    stubFor(get(urlEqualTo(save4LaterGetUrl))
      .willReturn(
        aResponse()
          .withStatus(Status.OK)
          .withBody(s"""{"data": {"email": "$encryptedEmail"}, "id": "1"}""".stripMargin)
      )
    )
  }

  def stubSave4LaterPUTEmailVerifiedResponse(): Unit = {
    stubFor(put(urlMatching(save4LaterPutUrl)).withRequestBody(containing(s"$encryptedEmail"))
      .willReturn(
        aResponse()
          .withBody("")
          .withStatus(Status.OK)
          .withHeader(CONTENT_TYPE, JSON)
      )
    )
  }

  def stubVerifiedEmailResponse(): Unit = {
    stubFor(post(urlEqualTo(verifiedEmailPostUrl))
      .withRequestBody(equalToJson(verifiedEmailRequestJson()))
      .willReturn(
        aResponse()
          .withStatus(Status.OK)
          .withBody("")
          .withHeader(CONTENT_TYPE, JSON)
      )
    )
  }

  def stubNotVerifiedEmailResponse(): Unit = {
    stubFor(post(urlEqualTo(verifiedEmailPostUrl))
      .withRequestBody(equalToJson(verifiedEmailRequestJson()))
      .willReturn(
        aResponse()
          .withStatus(Status.NOT_FOUND)
          .withBody("")
          .withHeader(CONTENT_TYPE, JSON)
      )
    )
  }

  def stubCustomsDataStoreOkResponse(): Unit = {
    stubFor(post(urlEqualTo(customsDataStoreGraphQl))
      .willReturn(
        aResponse()
          .withStatus(Status.OK)
          .withBody("")
          .withHeader(CONTENT_TYPE, JSON)
      )
    )
  }


  def verifyCustomsDataStoreIsCalled(): Unit = {
    verify(1, postRequestedFor(customsDataStoreContextPath))
  }

  def verifyCustomsDataStoreIsNotCalled(): Unit = {
    verify(0, postRequestedFor(customsDataStoreContextPath))
  }

  def verifyEmailVerifiedIsCalled(): Unit = {
    verify(1, postRequestedFor(verifiedEmailContextPath))
  }
}
