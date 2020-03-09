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
import com.github.tomakehurst.wiremock.matching.UrlPattern
import play.api.http.Status
import play.mvc.Http.HeaderNames.CONTENT_TYPE
import play.mvc.Http.MimeTypes.JSON

trait StubSubscriptionDisplay {

  private val subscriptionDisplay = "/subscription-display"

  private val subscriptionDisplayContextPath: UrlPattern = urlPathEqualTo(subscriptionDisplay)

  private val subscriptionDisplayResponseJson: String =
    """{
          "subscriptionDisplayResponse": {
            "responseDetail": {
              "contactInformation": {
              "emailAddress": "b@a.com",
              "emailVerificationTimestamp": "2019-09-06T12:30:59Z"
                  }
              }
          }
       }""".stripMargin

  private val subscriptionDisplayWithoutTimestampResponseJson: String =
    """{
        "subscriptionDisplayResponse": {
        "responseDetail": {
        "contactInformation": {
        "emailAddress": "b@a.com"
            }
          }
        }
    }""".stripMargin

  private val subscriptionDisplay200ErrorResponse: String =
    """{
      |  "subscriptionDisplayResponse": {
      |    "responseCommon": {
      |          "status": "OK",
      |          "statusText": "005 - No form bundle found",
      |          "processingDate": "2016-08-17T19:33:47Z",
      |          "returnParameters": [{
      |                "paramName": "POSITION",
      |                "paramValue": "FAIL"
      |          }]
      |    }
      |  }
      |}""".stripMargin

  private val subscriptionDisplay200EmailNotAvailableResponseJson = {
    """{
      |  "subscriptionDisplayResponse": {
      |    "responseCommon": {
      |      "status": "OK",
      |      "statusText": "Processed Successfully",
      |      "processingDate": "2016-08-17T19:33:47Z",
      |      "returnParameters": [
      |        {
      |          "paramName": "ETMPFORMBUNDLENUMBER",
      |          "paramValue": "9876543210"
      |        },
      |        {
      |          "paramName": "POSITION",
      |          "paramValue": "LINK"
      |        }
      |      ]
      |    },
      |    "responseDetail": {
      |      "EORINo": "GBE9XSDF10BCKEYAX",
      |      "EORIStartDate": "1999-01-01",
      |      "EORIEndDate": "2020-01-01",
      |      "CDSFullName": "John Doe",
      |      "CDSEstablishmentAddress": {
      |        "streetAndNumber": "Line 1",
      |        "city": "City name",
      |        "postalCode": "ZZ99 1AA",
      |        "countryCode": "GB"
      |      },
      |      "establishmentInTheCustomsTerritoryOfTheUnion": "0",
      |      "typeOfLegalEntity": "0003",
      |      "contactInformation": {
      |        "personOfContact": "John Doe",
      |        "streetAndNumber": "Line 1",
      |        "city": "City name",
      |        "postalCode": "ZZ99 1AA",
      |        "countryCode": "GB",
      |        "telephoneNumber": "01702215001",
      |        "faxNumber": "01702215002"
      |      },
      |      "VATIDs": [
      |        {
      |          "countryCode": "GB",
      |          "VATID": "12164568990"
      |        }
      |      ],
      |      "thirdCountryUniqueIdentificationNumber": [
      |        "321",
      |        "222"
      |      ],
      |      "consentToDisclosureOfPersonalData": "1",
      |      "shortName": "Robinson",
      |      "dateOfEstablishment": "1963-04-01",
      |      "typeOfPerson": "1",
      |      "principalEconomicActivity": "2000"
      |    }
      |  }
      |}
    """.stripMargin
  }


  private val subscriptionDisplay400ResponseJson: String =
    """{
      |"errorDetail": {
      |"timestamp": "2016-10-10T13:52:16Z",
      |"correlationId": "d60de98c-f499-47f5-b2d6-e80966e8d19e",
      |"errorCode": "400",
      |"errorMessage": "taxPayerID missing or invalid",
      |"source": "Back End",
      |"sourceFaultDetail": {
      |"detail": [
      |"002 - taxPayerID missing or invalid"
      |     ]
      |   }
      | }
      |}
      |""".stripMargin

  private val subscriptionDisplay500ResponseJson: String =
    """{
      |"errorDetail": {
      |"timestamp": "2016-10-10T14:12:20Z",
      |"correlationId": "ee8ef3d2-e9cc-4a42-8bf6-f82e809a23a7",
      |"errorCode": "500",
      |"errorMessage": "Send timeout",
      |"source": "ct-api",
      |"sourceFaultDetail": {
      |"detail": [
      |           "101504 - Send timeout"
      |          ]
      |    }
      |  }
      |}
      |""".stripMargin

  private val subscriptionDisplay404ResponseJson: String =
    """{
      |"errorDetail": {
      |"timestamp": "2016-10-10T14:12:20Z",
      |"correlationId": "ee8ef3d2-e9cc-4a42-8bf6-f82e809a23a7",
      |"errorCode": "404",
      |"errorMessage": "taxPayerID or EORI exists but no detail returned",
      |"source": "Back End",
      |"sourceFaultDetail": {
      |"detail": ["taxPayerID or EORI exists but no detail returned"]
      |   }
      |  }
      |}
      |"""

  private def stubSubscriptionDisplay(eoriNumber: String, status: Int, subscriptionDisplayResponse: String): Unit = {
    stubFor(get(urlPathEqualTo(s"$subscriptionDisplay"))
      .withQueryParam("EORI", equalTo(eoriNumber))
      .withQueryParam("regime", equalTo("CDS"))
      .withQueryParam("acknowledgementReference", matching("[\\w]{32}"))
      .willReturn(
        aResponse()
          .withStatus(status)
          .withBody(subscriptionDisplayResponse)
          .withHeader(CONTENT_TYPE, JSON)
      )
    )
  }

  def stubSubscriptionDisplayOkResponse(eoriNumber: String): Unit = {
    stubSubscriptionDisplay(eoriNumber, Status.OK, subscriptionDisplayResponseJson)
  }

  def stubSubscriptionWithoutTimestampDisplayOkResponse(eoriNumber: String): Unit = {
    stubSubscriptionDisplay(eoriNumber, Status.OK, subscriptionDisplayWithoutTimestampResponseJson)
  }

  def stubSubscriptionDisplayOk200ErrorResponse(eoriNumber: String): Unit = {
    stubSubscriptionDisplay(eoriNumber, Status.OK, subscriptionDisplay200ErrorResponse)
  }

  def stubSubscriptionDisplayBadRequestResponse(eoriNumber: String): Unit = {
    stubSubscriptionDisplay(eoriNumber, Status.BAD_REQUEST, subscriptionDisplay400ResponseJson)
  }

  def stubSubscriptionDisplayNotFoundResponse(eoriNumber: String): Unit = {
    stubSubscriptionDisplay(eoriNumber, Status.NOT_FOUND, subscriptionDisplay404ResponseJson)
  }

  def stubSubscriptionDisplayInternalServerResponse(eoriNumber: String): Unit = {
    stubSubscriptionDisplay(eoriNumber, Status.INTERNAL_SERVER_ERROR, subscriptionDisplay500ResponseJson)
  }

  def stubSubscriptionDisplayOk200EmailNotAvailableResponse(eoriNumber: String) = {
    stubSubscriptionDisplay(eoriNumber, Status.OK, subscriptionDisplay200EmailNotAvailableResponseJson)
  }

  def verifySubscriptionDisplayIsCalled(times:Int, eoriNumber:String): Unit = {
    verify(times, getRequestedFor(subscriptionDisplayContextPath).withQueryParam("EORI", equalTo(eoriNumber))
      .withQueryParam("regime", equalTo("CDS"))
      .withQueryParam("acknowledgementReference", matching("[\\w]{32}")))
  }

}
