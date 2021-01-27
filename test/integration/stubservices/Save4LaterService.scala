/*
 * Copyright 2021 HM Revenue & Customs
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
import play.api.libs.json.Json
import play.mvc.Http.HeaderNames.CONTENT_TYPE
import play.mvc.Http.MimeTypes.JSON
import play.mvc.Http.Status._
import uk.gov.hmrc.customs.emailfrontend.model.EmailDetails
import uk.gov.hmrc.http.HeaderCarrier

object Save4LaterService {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val id = "id-12345678"
  val emailKey = "email"

  case class User(name: String, tel: Int)

  object User {
    implicit val jsonFormat = Json.format[User]
  }

  val expectedUrl = s"/save4later/$id/$emailKey"
  val expectedDeleteUrl = s"/save4later/$id"

  val regexIntId =
    "([0-9a-fA-F]{8}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{12})"

  private val intId = "a14b5b18-9444-4ea1-99ad-a667047682ad"

  val email = EmailDetails(None, "john.doe@example.com", None)
  val emailJson = Json.toJson(email)
  val emailJsonAsString = Json.toJson(email).toString()

  def stubSave4LaterGET_OK() =
    stubSave4LaterGETResponse(expectedUrl, emailJsonAsString, OK)

  def stubSave4LaterGET_NOTFOUND() =
    stubSave4LaterGETResponse(expectedUrl, emailJsonAsString, NOT_FOUND)

  def stubSave4LaterGET_BAD_REQUEST() =
    stubSave4LaterGETResponse(expectedUrl, emailJsonAsString, BAD_REQUEST)

  def stubSave4LaterPUT() =
    stubSave4LaterPUTResponse(expectedUrl, emailJsonAsString, CREATED)

  def stubSave4LaterPUT_BAD_REQUEST() =
    stubSave4LaterPUTResponse(expectedUrl, emailJsonAsString, BAD_REQUEST)

  def stubSave4LaterDELETE() =
    stubSave4LaterDeleteResponse(expectedDeleteUrl, NO_CONTENT)
  def stubSave4LaterNotFoundDELETE() =
    stubSave4LaterDeleteResponse(expectedDeleteUrl, NOT_FOUND)

  def stubSave4LaterGETResponse(url: String,
                                response: String,
                                status: Int): Unit =
    stubFor(
      get(urlMatching(url))
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(response)
            .withHeader(CONTENT_TYPE, JSON)
        )
    )

  def stubSave4LaterPUTResponse(url: String,
                                response: String,
                                status: Int): Unit =
    stubFor(
      put(urlMatching(url))
        .withRequestBody(containing(emailJsonAsString))
        .willReturn(
          aResponse()
            .withBody(response)
            .withStatus(status)
            .withHeader(CONTENT_TYPE, JSON)
        )
    )

  def stubSave4LaterDeleteResponse(url: String, status: Int): Unit =
    stubFor(
      delete(urlMatching(url))
        .willReturn(
          aResponse()
            .withStatus(status)
            .withHeader(CONTENT_TYPE, JSON)
        )
    )

}
