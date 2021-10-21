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

///*
// * Copyright 2021 HM Revenue & Customs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package uk.gov.hmrc.customs.emailfrontend.controllers
//
//import play.api.mvc.Session
//import play.api.test.Helpers._
//import uk.gov.hmrc.customs.emailfrontend.views.html.{accessibility_statement, start_page}
//
//class SignOutControllerSpec extends ControllerSpec {
//
//  val controller = new SignOutController(fakeAction, appConfig, mcc)
//
//  "SignOut Controller" should {
//    "redirect to feedback survey" in withAuthorisedUser() {
//      val result = controller.signOut(request)
//      status(result) shouldBe SEE_OTHER
//      redirectLocation(result).value should endWith(
//        "/feedback/manage-email-cds")
//    }
//
//    "clear the session once the user signs out" in withAuthorisedUser() {
//      status(controller.signOut(request)) shouldBe SEE_OTHER
//      val view = app.injector.instanceOf[start_page]
//      val accessibilityStatement =
//        app.injector.instanceOf[accessibility_statement]
//      val startPageController =
//        new ApplicationController(view, accessibilityStatement, mcc)
//      val result = startPageController.start(request)
//      session(result) shouldBe Session.emptyCookie
//    }
//  }
//}
