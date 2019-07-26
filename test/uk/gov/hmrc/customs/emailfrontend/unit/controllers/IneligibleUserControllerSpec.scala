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

import play.api.test.CSRFTokenHelper.CSRFFRequestHeader
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Configuration, Mode}
import uk.gov.hmrc.customs.emailfrontend.config.AppConfig
import uk.gov.hmrc.customs.emailfrontend.controllers.IneligibleUserController
import uk.gov.hmrc.customs.emailfrontend.views.html.ineligible_user
import uk.gov.hmrc.play.bootstrap.config.{RunMode, ServicesConfig}


class IneligibleUserControllerSpec extends ControllerSpec {

  private val request = FakeRequest("GET", "/").withCSRFToken

  private val view = app.injector.instanceOf[ineligible_user]

  private val config = Configuration.load(env)
  private val serviceConfig = new ServicesConfig(config, new RunMode(config, Mode.Dev))

  private implicit val appConfig: AppConfig = new AppConfig(config, serviceConfig)

  private val controller = new IneligibleUserController(fakeAction, view)


  "IneligibleUserController" should {

    "have a status of Unauthorised (401)" in withAuthorisedUserWithoutEnrolments {
      status(controller.show(request)) shouldBe UNAUTHORIZED
    }

    "have a status of See Other (303)" in withUnauthorisedUser {
      status(controller.show(request)) shouldBe SEE_OTHER
    }

  }
}
