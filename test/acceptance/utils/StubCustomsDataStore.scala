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
import play.api.http.Status
import play.mvc.Http.HeaderNames.CONTENT_TYPE
import play.mvc.Http.MimeTypes.JSON

trait StubCustomsDataStore {

  private val customsDataStoreGraphQl = "/customs-data-store/graphql"

  private val customsDataStoreContextPath: UrlPattern = urlMatching(customsDataStoreGraphQl)

  def stubCustomsDataStoreOkResponse(): Unit = {
    stubFor(post(urlEqualTo(customsDataStoreGraphQl))
      .willReturn(
        aResponse()
          .withStatus(Status.OK)
          .withHeader(CONTENT_TYPE, JSON)
      )
    )
  }

  def verifyCustomsDataStoreIsCalled(times: Int): Unit = {
    verify(times, postRequestedFor(customsDataStoreContextPath))
  }

  def verifyCustomsDataStoreIsNotCalled(): Unit = {
    verify(0, postRequestedFor(customsDataStoreContextPath))
  }
}
