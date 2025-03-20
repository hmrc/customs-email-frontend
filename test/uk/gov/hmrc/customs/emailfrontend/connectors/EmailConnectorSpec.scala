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

package uk.gov.hmrc.customs.emailfrontend.connectors

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.matchers.must.Matchers.mustBe
import uk.gov.hmrc.customs.emailfrontend.utils.SpecBase
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HttpReads, HttpResponse, UpstreamErrorResponse}
import play.api.http.Status.OK

import java.net.URL
import scala.concurrent.{ExecutionContext, Future}

class EmailConnectorSpec extends SpecBase {

  "sendEmail" should {

    "return 200 response when email is sent successfully" in new Setup {
      when(mockHttpClient.post(any[URL]())(any())).thenReturn(requestBuilder)
      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.execute(any[HttpReads[Either[UpstreamErrorResponse, HttpResponse]]], any[ExecutionContext]))
        .thenReturn(Future.successful(HttpResponse(OK)))

      val result: HttpResponse =
        connector.sendEmail("test@test.com", "test_template", Map("emailAddress" -> "test@test.com")).futureValue

      result.status mustBe OK
    }

    "return UpstreamErrorResponse when error occurs while sending email" in new Setup {}
  }

  trait Setup {
    val mockHttpClient: HttpClientV2   = mock[HttpClientV2]
    val requestBuilder: RequestBuilder = mock[RequestBuilder]

    val connector: EmailConnector = app.injector.instanceOf[EmailConnector]
  }
}
