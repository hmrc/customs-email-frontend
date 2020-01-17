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

package acceptance.wiremockstub

import com.github.tomakehurst.wiremock.client.WireMock._
import play.api.http.Status
import play.mvc.Http.HeaderNames.CONTENT_TYPE
import play.mvc.Http.MimeTypes.JSON

trait StubEmailVerification {

  private val emailVerificationPostUrl = "/email-verification/verification-requests"
  private val verifiedEmailPostUrl = "/email-verification/verified-email-check"
  private val verifiedEmailContextPath = urlMatching(verifiedEmailPostUrl)

  private val verificationRequestJson: String = {
    """{
      |"email":"b@a.com",
      |"templateId" : "verifyEmailAddress",
      |"templateParameters":{},
      |"linkExpiryDuration":"P3D",
      |"continueUrl":"/manage-email-cds/email-address-confirmed"
      |}
    """.stripMargin
  }

  private val verifiedEmailRequestJson: String = {
    """{
      |"email" : "b@a.com"
      |}""".stripMargin
  }

  private def stubVerificationRequest(url: String, status: Int): Unit = {
    stubFor(post(urlEqualTo(url))
      .withRequestBody(equalToJson(verificationRequestJson))
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

  def stubVerifiedEmailResponse(): Unit = {
    stubFor(post(urlEqualTo(verifiedEmailPostUrl))
      .withRequestBody(equalToJson(verifiedEmailRequestJson))
      .willReturn(
        aResponse()
          .withStatus(Status.OK)
          .withHeader(CONTENT_TYPE, JSON)
      )
    )
  }

  def stubNotVerifiedEmailResponse(): Unit = {
    stubFor(post(urlEqualTo(verifiedEmailPostUrl))
      .withRequestBody(equalToJson(verifiedEmailRequestJson))
      .willReturn(
        aResponse()
          .withStatus(Status.NOT_FOUND)
          .withHeader(CONTENT_TYPE, JSON)
      )
    )
  }

  def verifyEmailVerifiedIsCalled(times:Int): Unit = {
    verify(times, postRequestedFor(verifiedEmailContextPath))
  }
}
