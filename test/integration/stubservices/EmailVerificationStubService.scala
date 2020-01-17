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

package integration.stubservices

import com.github.tomakehurst.wiremock.client.WireMock._
import play.mvc.Http.HeaderNames.CONTENT_TYPE
import play.mvc.Http.MimeTypes.JSON
import play.mvc.Http.Status._

object EmailVerificationStubService {

  def expectedGetUrl(email:String) = s"/email-verification/verified-email-addresses/$email"

  private val expectedVerifiedEmailPostUrl = "/email-verification/verified-email-check"

  private val expectedPostUrl = "/email-verification/verification-requests"

  private def verificationRequestJson(): String = {
    """{
      |"email":"test@example.com",
      |"templateId" : "verifyEmailAddress",
      |"templateParameters":{},
      |"linkExpiryDuration":"P3D",
      |"continueUrl":"/customs/test-continue-url"
      |}
    """.stripMargin
  }

  val internalServerErrorResponse: String = {
    """{
      |  "code": "UNEXPECTED_ERROR",
      |  "message":"An unexpected error occurred."
      |}""".stripMargin
  }

  def stubEmailVerified() = {
    stubTheVerifiedEmailResponse(expectedVerifiedEmailPostUrl, "", OK)
  }

  def stubEmailNotVerified() = {
    stubTheVerifiedEmailResponse(expectedVerifiedEmailPostUrl, "", NOT_FOUND)
  }

  def stubEmailVerifiedInternalServerError() = {
    stubTheVerifiedEmailResponse(expectedVerifiedEmailPostUrl, internalServerErrorResponse, INTERNAL_SERVER_ERROR)
  }

  def stubVerificationRequestSent()={
    stubVerificationRequest(expectedPostUrl,"", CREATED)
  }

  def stubEmailAlreadyVerified()={
    stubVerificationRequest(expectedPostUrl,"", CONFLICT)
  }

  def stubVerificationRequestError()={
    stubVerificationRequest(expectedPostUrl, internalServerErrorResponse, INTERNAL_SERVER_ERROR)
  }

  def stubTheVerifiedEmailResponse(url: String, response: String, status: Int): Unit = {
    stubFor(post(urlEqualTo(url))
      .willReturn(
        aResponse()
          .withStatus(status)
          .withBody(response)
          .withHeader(CONTENT_TYPE, JSON)
      )
    )
  }

  def stubVerificationRequest(url: String, response: String, status: Int): Unit = {
    stubFor(post(urlMatching(url))
      .withRequestBody(equalToJson(verificationRequestJson()))
      .willReturn(
        aResponse()
          .withBody(response)
          .withStatus(status)
          .withHeader(CONTENT_TYPE, JSON)
      )
    )
  }
}
