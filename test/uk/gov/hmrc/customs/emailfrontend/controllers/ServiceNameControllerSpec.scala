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

import org.mockito.ArgumentMatchers.{eq => meq}
import play.api.test.Helpers.{status, _}
import play.api.{Application, inject}
import uk.gov.hmrc.customs.emailfrontend.model.{InternalId, ReferrerName}
import uk.gov.hmrc.customs.emailfrontend.services.Save4LaterService
import uk.gov.hmrc.customs.emailfrontend.utils.{FakeIdentifierAgentAction, SpecBase}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class ServiceNameControllerSpec extends SpecBase {

  trait Setup {
    protected val mockSave4LaterService: Save4LaterService = mock[Save4LaterService]
    protected val app: Application = applicationBuilder[FakeIdentifierAgentAction]()
      .overrides(inject.bind[Save4LaterService].toInstance(mockSave4LaterService))
      .build()
  }

  "ServiceNameController" should {
    "redirect to chance-email-address page and store the referred service name in the cache when parameter found in the url" in new Setup {

      when(mockSave4LaterService.saveReferrer(meq(InternalId("fakeInternalId")), meq(ReferrerName("customs-finance", "/customs/payment-records")))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(Unit)))

      running(app) {
        val requestWithForm = fakeRequestWithCsrf(GET, routes.ServiceNameController.show("customs-finance").url)
        val result = route(app, requestWithForm).value
        status(result) shouldBe SEE_OTHER

        redirectLocation(result).value shouldBe routes.VerifyChangeEmailController.show.url
      }


    }

    "redirect to problem-with-service page when service name is not found in the url" in new Setup {
      running(app) {
        val requestWithForm = fakeRequestWithCsrf(GET, routes.ServiceNameController.show("not-a-service").url)
        val result = route(app, requestWithForm).value
        status(result) shouldBe SEE_OTHER

        redirectLocation(result).value shouldBe routes.VerifyChangeEmailController.show.url
      }
    }
  }
}
