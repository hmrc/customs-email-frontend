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

import play.api.test.Helpers.{status, _}
import uk.gov.hmrc.customs.emailfrontend.controllers.EmailConfirmedController
import uk.gov.hmrc.customs.emailfrontend.views.html.email_confirmed


class EmailConfirmedControllerSpec extends ControllerSpec {

  private val view = app.injector.instanceOf[email_confirmed]
  private val controller = new EmailConfirmedController(fakeAction, view)

  "EmailConfirmedController" should {

    "have a status of OK" in withAuthorisedUser() {
      val eventualResult = controller.show(request)
      status(eventualResult) shouldBe OK
    }
  }
}
