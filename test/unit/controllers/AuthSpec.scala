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

package unit.controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import play.api.test.Helpers._
import uk.gov.hmrc.customs.emailfrontend.config.{AppConfig, ErrorHandler}
import uk.gov.hmrc.customs.emailfrontend.connectors.SubscriptionDisplayConnector
import uk.gov.hmrc.customs.emailfrontend.controllers.WhatIsYourEmailController
import uk.gov.hmrc.customs.emailfrontend.services.{EmailVerificationService, Save4LaterService}
import uk.gov.hmrc.customs.emailfrontend.views.html.{change_your_email, what_is_your_email}
import org.scalatest.matchers.should.Matchers._

import scala.concurrent.Future

class AuthSpec extends ControllerSpec with BeforeAndAfterEach {

  private val ineligibleUrl = "/manage-email-cds/ineligible/no-enrolment"

  private val view = app.injector.instanceOf[change_your_email]
  private val verifyView = app.injector.instanceOf[what_is_your_email]
  private val mockSave4LaterService = mock[Save4LaterService]
  private val mockErrorHandler = mock[ErrorHandler]
  private val mockConfig = mock[AppConfig]
  private val mockSubscriptionDisplayConnector =
    mock[SubscriptionDisplayConnector]
  private val mockEmailVerificationService = mock[EmailVerificationService]

  private val controller = new WhatIsYourEmailController(
    fakeAction,
    view,
    verifyView,
    mockSave4LaterService,
    mcc,
    mockSubscriptionDisplayConnector,
    mockEmailVerificationService,
    mockErrorHandler,
    mockConfig
  )
  when(mockSave4LaterService.fetchEmail(any())(any(), any()))
    .thenReturn(Future.successful(None))

  "Accessing a controller that requires a user to be authorised" should {

    "allow a fully authorised user access the page" in withAuthorisedUser() {
      val result = controller.show(request)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) should contain(
        "/manage-email-cds/change-email-address/create")
    }

    "not allow an authorised user without any enrolments to access the page" in withAuthorisedUserWithoutEnrolments {
      val result = controller.show(request)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) should contain(ineligibleUrl)
    }

    "not allow an authorised user without an internal id to access the page" in withAuthorisedUserWithoutInternalId {
      val result = controller.show(request)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) should contain(ineligibleUrl)
    }

    "not allow a logged out user to access the page" in withUnauthorisedUser {
      val result = controller.show(request)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) should contain(
        "/bas-gateway/sign-in?continue_url=http%3A%2F%2Flocalhost%3A9898%2Fmanage-email-cds%2Fchange-email-address&origin=customs-email-frontend"
      )
    }

    "show 'ineligible user - no enrolment' page for an authorised user having no eori" in withAuthorisedUserWithoutEori {
      val eventualResult = controller.show(request)

      status(eventualResult) shouldBe SEE_OTHER
      redirectLocation(eventualResult).value should endWith(
        "/manage-email-cds/ineligible/no-enrolment")
    }

    "show 'ineligible user - agent' page for an authorised agent with no enrolments" in withAuthorisedAgentWithoutCDSEnrolment {
      val eventualResult = controller.show(request)

      status(eventualResult) shouldBe SEE_OTHER
      redirectLocation(eventualResult).value should endWith(
        "/manage-email-cds/ineligible/is-agent")
    }
  }
}
