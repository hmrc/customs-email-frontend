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

import play.api.mvc.Session
import play.api.test.Helpers._
import uk.gov.hmrc.customs.emailfrontend.controllers.{ApplicationController, SignOutController}
import uk.gov.hmrc.customs.emailfrontend.views.html.start_page

class SignOutControllerSpec extends ControllerSpec {

  val controller = new SignOutController(fakeAction, appConfig)

  "SignOut Controller" should {
    "return have status See_Other (303) and redirect to feedback survey" in withAuthorisedUserWithoutEnrolments {
      val result = controller.signOut(request)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result).value should endWith("/feedback/CDS")
    }

    "clear the session once the user signs out" in withAuthorisedUserWithoutEnrolments {
      status(controller.signOut(request)) shouldBe SEE_OTHER
      val view = app.injector.instanceOf[start_page]
      val startPageController = new ApplicationController(fakeAction, view)
      val result = startPageController.show(request)
      session(result) shouldBe Session.emptyCookie
    }
  }
}
