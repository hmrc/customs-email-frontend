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

package uk.gov.hmrc.customs.emailfrontend.unit.controllers

import play.api.test.Helpers._
import uk.gov.hmrc.customs.emailfrontend.controllers.IneligibleUserController
import uk.gov.hmrc.customs.emailfrontend.views.html.ineligible_user

class IneligibleUserControllerSpec extends ControllerSpec {

  private val view = app.injector.instanceOf[ineligible_user]
  private val controller = new IneligibleUserController(fakeAction, view)

  "IneligibleUserController" should {

    "have a status of Unauthorised (401)" in withAuthorisedUserWithoutEnrolments {
      val eventualResult = controller.show(request)
      status(eventualResult) shouldBe UNAUTHORIZED
    }

    "have a status of See Other (303) and redirect to Government Gateway" in withUnauthorisedUser {
      val result = controller.show(request)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) should contain("/gg/sign-in?continue=http%3A%2F%2Flocalhost%3A9898%2Fcustoms-email-frontend%2Fstart&origin=customs-email-frontend")
    }

  }
}
