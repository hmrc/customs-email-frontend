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

import org.scalatest.BeforeAndAfterEach
import play.api.test.Helpers._
import uk.gov.hmrc.customs.emailfrontend.controllers.ApplicationController
import uk.gov.hmrc.customs.emailfrontend.views.html.start_page
import uk.gov.hmrc.customs.emailfrontend.views.html.accessibility_statement

class ApplicationControllerSpec extends ControllerSpec with BeforeAndAfterEach {

  private val view = app.injector.instanceOf[start_page]
  private val accessibilityStatement = app.injector.instanceOf[accessibility_statement]
  private val controller = new ApplicationController(fakeAction, view, accessibilityStatement)

  "ApplicationController" should {
    "allow  the user to access the start page" in {
      val result = controller.show(request)
      status(result) shouldBe OK
    }

    "allow users to refresh their session" in {
      val result = controller.keepAlive(request)
      status(result) shouldBe OK
    }

    "allow  the user to access the accessibility statement page" in {
      val result = controller.accessibilityStatement(request)
      status(result) shouldBe OK
    }
  }
}
