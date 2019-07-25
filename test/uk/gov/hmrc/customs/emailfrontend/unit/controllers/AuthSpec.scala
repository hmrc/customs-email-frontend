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
import uk.gov.hmrc.customs.emailfrontend.controllers.HelloWorldController
import uk.gov.hmrc.customs.emailfrontend.model.Eori
import uk.gov.hmrc.customs.emailfrontend.views.html.hello_world
import uk.gov.hmrc.play.bootstrap.config.{RunMode, ServicesConfig}


class AuthSpec extends ControllerSpec {

  private val config = Configuration.load(env)
  private val serviceConfig = new ServicesConfig(config, new RunMode(config, Mode.Dev))
  private implicit val appConfig: AppConfig = new AppConfig(config, serviceConfig)

  private val request = FakeRequest("GET", "/").withCSRFToken
  private val eori = Eori("ZZ123456789")
  private val view = app.injector.instanceOf[hello_world]

  val controller = new HelloWorldController(fakeAction, view)

  "Accessing a controller that requires a user to be authorised" should {
    "allow a fully authorised user access the page" in withAuthorisedUser(eori) {
      status(controller.helloWorld(request)) shouldBe OK
    }

    "not allow an authorised user without an enrolled eori to access the page" in withAuthorisedUserWithoutEori {
      status(controller.helloWorld(request)) shouldBe SEE_OTHER
    }

    "not allow an authorised user without any enrolments to access the page" in withAuthorisedUserWithoutEnrolments {
      status(controller.helloWorld(request)) shouldBe SEE_OTHER
    }

    "not allow a logged out user to access the page" in {
      status(controller.helloWorld(request)) shouldBe SEE_OTHER
    }
  }

}
