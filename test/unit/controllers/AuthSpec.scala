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

package unit.controllers

import org.scalatest.BeforeAndAfterEach
import play.api.test.Helpers._
import uk.gov.hmrc.customs.emailfrontend.controllers.ApplicationController
import uk.gov.hmrc.customs.emailfrontend.model.Eori
import uk.gov.hmrc.customs.emailfrontend.views.html.start_page

class AuthSpec extends ControllerSpec with BeforeAndAfterEach {

  private val eori = Eori("ZZ123456789")
  private val ineligibleUrl = "/customs-email-frontend/ineligible/no-enrolment"

  private val view = app.injector.instanceOf[start_page]

  val controller = new ApplicationController(fakeAction, view)

  "Accessing a controller that requires a user to be authorised" should {

    "allow a fully authorised user access the page" in withAuthorisedUser(eori) {
      status(controller.show(request)) shouldBe OK
    }

    "not allow an authorised user without an enrolled eori to access the page" in withAuthorisedUserWithoutEori {
      val result = controller.show(request)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) should contain(ineligibleUrl)
    }

    "not allow an authorised user with an empty eori to access the page" in withAuthorisedUser(Eori("")) {
      val result = controller.show(request)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) should contain(ineligibleUrl)
    }

    "not allow an authorised user with and eori and without an internal id to access the page" in withUnauthorisedUserWithoutInternalId {
      val result = controller.show(request)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) should contain(ineligibleUrl)
    }

    "not allow an authorised user without any enrolments to access the page" in withAuthorisedUserWithoutEnrolments {
      val result = controller.show(request)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) should contain(ineligibleUrl)
    }

    "not allow a logged out user to access the page" in withUnauthorisedUser {
      val result = controller.show(request)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) should contain("/gg/sign-in?continue=http%3A%2F%2Flocalhost%3A9898%2Fcustoms-email-frontend%2Fstart&origin=customs-email-frontend")
    }
  }

  "Keep Alive" should {
    "allow unauthenticated users to refresh their session" in withUnauthorisedUser {
      val result = controller.keepAlive(request)
      status(result) shouldBe OK
    }
  }
}
