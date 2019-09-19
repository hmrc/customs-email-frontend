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

package integration.stubservices

import com.github.tomakehurst.wiremock.client.WireMock._
import play.mvc.Http.HeaderNames.CONTENT_TYPE
import play.mvc.Http.MimeTypes.JSON
import play.mvc.Http.Status._

object UpdateVerifiedEmailStubService {

  private val expectedUr = "/update-verified-email"

  val updateVerifiedEmailRequest: String = {
    """{
      |    "requestCommon": {
      |      "regime": "CDS",
      |      "receiptDate": "2019-08-22T13:55:55Z",
      |      "acknowledgementReference": "16061ef4ea8740128ac49e9787d3d1f3"
      |    },
      |    "requestDetail": {
      |      "IDType": "EORI",
      |      "IDNumber": "GB173822879792263",
      |      "emailAddress": "test@email.com",
      |      "emailVerificationTimestamp": "2019-08-22T13:55:55Z"
      |    }
      |}""".stripMargin
  }

  private val verifiedEmailRequest: String = {
    """{
      |  "updateVerifiedEmailRequest": {
      |    "requestCommon": {
      |      "regime": "CDS",
      |      "receiptDate": "2019-08-22T13:55:55Z",
      |      "acknowledgementReference": "16061ef4ea8740128ac49e9787d3d1f3"
      |    },
      |    "requestDetail": {
      |      "IDType": "EORI",
      |      "IDNumber": "GB173822879792263",
      |      "emailAddress": "test@email.com",
      |      "emailVerificationTimestamp": "2019-08-22T13:55:55Z"
      |    }
      |  }
      |}""".stripMargin
  }

  val updatedVerifiedEmailResponse: String = {
    """{
      |  "updateVerifiedEmailResponse": {
      |    "responseCommon": {
      |      "status": "OK",
      |      "processingDate": "2019-08-22T13:55:53Z",
      |      "returnParameters": [
      |        {
      |          "paramName": "ETMPFORMBUNDLENUMBER",
      |          "paramValue": "093000001830"
      |        }
      |      ]
      |    }
      |  }
      |}""".stripMargin
  }

  private val badRequestResponse: String = {
    """{
      |  "errorDetail": {
      |    "timestamp": "2016-08-24T10:15:27Z",
      |    "correlationId": "f058ebd602f74d3f942e904344e8cde5",
      |    "errorCode": "400",
      |    "errorMessage": "Request cannot be processed ",
      |    "source": "JSON validation",
      |    "sourceFaultDetail": {
      |      "detail": [
      |        "Invalid Regime"
      |      ]
      |    }
      |  }
      |}""".stripMargin
  }

  private val serviceUnavailableResponse: String = {
    """{
      |  "errorDetail": {
      |    "timestamp": "2016-08-16T18:15:41Z",
      |    "correlationId": "",
      |    "errorCode": "500",
      |    "errorMessage": "Send timeout",
      |    "source": "ct-api",
      |    "sourceFaultDetail": {
      |      "detail": [
      |        "101504 - Send timeout"
      |      ]
      |    }
      |  }
      |}""".stripMargin
  }

  private val forbiddenResponse: String = {
    """{
      |  "errorDetail": {
      |    "timestamp": "2016-08-24T10:15:27Z",
      |    "correlationId": "f058ebd602f74d3f942e904344e8cde5",
      |    "errorCode": "403",;
      |    "errorMessage": "Forbidden",
      |    "source": "JSON validation",
      |    "sourceFaultDetail": {
      |      "detail": [
      |        "Forbidden Reason"
      |      ]
      |    }
      |  }
      |}""".stripMargin
  }

  def stubEmailUpdated(okResponse: String) = {
    stubUpdateVerifiedEmailRequest(okResponse, OK)
  }

  def stubBadRequest() = {
    stubUpdateVerifiedEmailRequest(badRequestResponse, BAD_REQUEST)
  }

  def stubServiceUnavailable() = {
    stubUpdateVerifiedEmailRequest(serviceUnavailableResponse, INTERNAL_SERVER_ERROR)
  }

  def stubForbidden() = {
    stubUpdateVerifiedEmailRequest(forbiddenResponse, FORBIDDEN)
  }

  private def stubUpdateVerifiedEmailRequest(response: String, status: Int): Unit = {
    stubFor(put(urlMatching(expectedUr))
      .withRequestBody(equalToJson(verifiedEmailRequest))
      .willReturn(
        aResponse()
          .withBody(response)
          .withStatus(status)
          .withHeader(CONTENT_TYPE, JSON)
      )
    )
  }
}
