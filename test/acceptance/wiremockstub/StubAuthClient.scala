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

package acceptance.wiremockstub

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status

trait StubAuthClient {

  private val authUrl = "/auth/authorise"

  private val authRequestJson: String =
    """{
      |"authorise" : [{
      | "authProviders" : ["GovernmentGateway"]
      |}],
      | "retrieve" : ["allEnrolments","internalId","affinityGroup","credentialRole"]
      |}
    """.stripMargin

  def authenticate(
      internalId: String,
      eoriNumber: String,
      credentialRole: String = "User",
      affinityGroup: String = "Organisation"
  ): StubMapping =
    stubFor(
      post(urlEqualTo(authUrl))
        .withRequestBody(equalToJson(authRequestJson))
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
            .withBody(s"""{"allEnrolments": [
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
               |"internalId": "$internalId" , "credentialRole": "$credentialRole", "affinityGroup": "$affinityGroup"
               |}
              """.stripMargin)
        )
    )

  def authenticateGGUserAsAgentWithNoCDSEnrolment(
      internalId: String,
      eoriNumber: String,
      credentialRole: String,
      affinityGroup: String
  ): StubMapping =
    stubFor(
      post(urlEqualTo(authUrl))
        .withRequestBody(equalToJson(authRequestJson))
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
            .withBody(s"""{
               | "allEnrolments": [], "internalId": "$internalId" , "credentialRole": "$credentialRole", "affinityGroup": "$affinityGroup"
                }""".stripMargin)
        )
    )

  def authenticateGGUserWithError(internalId: String,
                                  reason: String): StubMapping =
    stubFor(
      post(urlEqualTo(authUrl))
        .withRequestBody(equalToJson(authRequestJson))
        .willReturn(
          aResponse()
            .withStatus(Status.UNAUTHORIZED)
            .withHeader("WWW-Authenticate", s"""MDTP detail="$reason"""")
        )
    )
}
