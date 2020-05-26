/*
 * Copyright 2020 HM Revenue & Customs
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

import play.api.test.Helpers._
import uk.gov.hmrc.customs.emailfrontend.controllers.IneligibleUserController
import uk.gov.hmrc.customs.emailfrontend.model.Ineligible
import uk.gov.hmrc.customs.emailfrontend.views.html.ineligible_user

class IneligibleUserControllerSpec extends ControllerSpec {

  private val view = app.injector.instanceOf[ineligible_user]
  private val controller = new IneligibleUserController(fakeAction, view)

  "IneligibleUserController" should {
    "have a status of Unauthorised (401)" in withUnauthorisedUser {
      val eventualResult =
        controller.show(Ineligible.NoEnrolment).apply(request)
      status(eventualResult) shouldBe UNAUTHORIZED
    }
  }
}
