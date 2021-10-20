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

package uk.gov.hmrc.customs.emailfrontend.controllers

import org.joda.time.DateTime
import org.mockito.ArgumentMatchers.{eq => meq, _}
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.customs.emailfrontend.controllers.AmendmentInProgressController
import uk.gov.hmrc.customs.emailfrontend.model.{EmailDetails, InternalId}
import uk.gov.hmrc.customs.emailfrontend.services.Save4LaterService
import uk.gov.hmrc.customs.emailfrontend.views.html.amendment_in_progress
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class AmendmentInProgressControllerSpec extends ControllerSpec {

  private val view = app.injector.instanceOf[amendment_in_progress]
  private val mockSave4LaterService = mock[Save4LaterService]
  private val controller = new AmendmentInProgressController(
    fakeAction,
    view,
    mockSave4LaterService,
    mcc)

  "AmendmentInProgressController" should {
    "have a status of SEE_OTHER when the email status is not found " in withAuthorisedUser() {
      when(mockSave4LaterService.fetchEmail(any())(any(), any()))
        .thenReturn(Future.successful(None))

      val eventualResult = controller.show(request)

      status(eventualResult) shouldBe SEE_OTHER
      redirectLocation(eventualResult).value should endWith(
        "/manage-email-cds/signout")
    }

    "have a status of OK when email found in cache and verification in progress" in withAuthorisedUser() {
      when(
        mockSave4LaterService.fetchEmail(meq(InternalId("internalId")))(
          any[HeaderCarrier],
          any[ExecutionContext]))
        .thenReturn(Future.successful(
          Some(EmailDetails(None, "test@email.com", Some(DateTime.now())))))

      val eventualResult = controller.show(request)

      status(eventualResult) shouldBe OK
    }
  }
}
