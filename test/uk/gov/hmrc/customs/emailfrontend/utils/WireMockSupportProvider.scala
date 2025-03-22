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

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.http.RequestMethod
import com.github.tomakehurst.wiremock.http.RequestMethod.{DELETE, GET, POST, PUT}
import org.scalatest.Suite
import play.api.Configuration
import uk.gov.hmrc.http.test.WireMockSupport

trait WireMockSupportProvider extends WireMockSupport {
  this: Suite =>

  val X_FORWARDED_HOST = "X-Forwarded-Host"
  val CONTENT_TYPE     = "Content-Type"
  val ACCEPT           = "Accept"
  val AUTHORIZATION    = "Authorization"

  val AUTH_BEARER_TOKEN_VALUE       = "Bearer test1234567"
  val CONTENT_TYPE_APPLICATION_JSON = "application/json"

  val PARAM_NAME_EORI   = "EORI"
  val PARAM_NAME_eori   = "eori"
  val PARAM_NAME_REGIME = "regime"

  def config: Configuration

  protected def verifyExactlyOneEndPointUrlHit(urlToVerify: String, methodType: RequestMethod = GET): Unit =
    wireMockServer.verify(
      1,
      buildRequestPatternForRequestedUrl(urlToVerify, methodType)
    )

  protected def verifyEndPointUrlHit(urlToVerify: String, methodType: RequestMethod = GET): Unit =
    wireMockServer.verify(
      buildRequestPatternForRequestedUrl(urlToVerify, methodType)
    )

  private def buildRequestPatternForRequestedUrl(urlToVerify: String, methodType: RequestMethod) =
    methodType match {
      case GET    => getRequestedFor(urlPathMatching(urlToVerify))
      case POST   => postRequestedFor(urlPathMatching(urlToVerify))
      case PUT    => putRequestedFor(urlPathMatching(urlToVerify))
      case DELETE => deleteRequestedFor(urlPathMatching(urlToVerify))
      case _      => throw new RuntimeException("Invalid method type")
    }
}
