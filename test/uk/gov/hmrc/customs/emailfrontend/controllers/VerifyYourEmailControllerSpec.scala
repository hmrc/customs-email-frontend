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

package uk.gov.hmrc.customs.emailfrontend.controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.api.{Application, inject}
import uk.gov.hmrc.customs.emailfrontend.model.EmailDetails
import uk.gov.hmrc.customs.emailfrontend.services.Save4LaterService
import uk.gov.hmrc.customs.emailfrontend.utils.SpecBase
import uk.gov.hmrc.customs.emailfrontend.utils.TestData.testEmail

import java.time.LocalDateTime
import scala.concurrent.Future

class VerifyYourEmailControllerSpec extends SpecBase {

  "VerifyYourEmailController" should {
    "redirect to sign out page when no email found in cache" in new Setup {

      when(mockSave4LaterService.fetchEmail(any)(any)).thenReturn(Future.successful(None))

      running(app) {
        val request = FakeRequest(GET, routes.VerifyYourEmailController.show.url)
        val result  = route(app, request).value

        status(result)                 shouldBe SEE_OTHER
        redirectLocation(result).value shouldBe "/manage-email-cds/signout"
      }
    }

    "return status OK when email found in cache" in new Setup {

      when(mockSave4LaterService.fetchEmail(any)(any))
        .thenReturn(Future.successful(Some(EmailDetails(None, testEmail, None))))

      running(app) {
        val request = FakeRequest(GET, routes.VerifyYourEmailController.show.url)
        val result  = route(app, request).value

        status(result)        shouldBe OK
        contentAsString(result) should include(testEmail)
      }
    }

    "have a status of SEE_OTHER when user clicks browser back on the successful request " +
      "or uses already complete bookmarked request within 2 hours" in new Setup {

        when(mockSave4LaterService.fetchEmail(any)(any))
          .thenReturn(Future.successful(Some(EmailDetails(None, testEmail, Some(LocalDateTime.now())))))

        running(app) {
          val request = FakeRequest(GET, routes.VerifyYourEmailController.show.url)
          val result  = route(app, request).value

          status(result)               shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe routes.AmendmentInProgressController.show.url
        }
      }
  }

  trait Setup {
    protected val mockSave4LaterService: Save4LaterService = mock[Save4LaterService]
    protected val app: Application                         = applicationBuilder()
      .overrides(inject.bind[Save4LaterService].toInstance(mockSave4LaterService))
      .build()
  }

}
