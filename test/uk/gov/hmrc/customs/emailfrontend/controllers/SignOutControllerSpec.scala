/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.customs.emailfrontend.controllers

import play.api.Application
import play.api.mvc.Session
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.customs.emailfrontend.config.AppConfig
import uk.gov.hmrc.customs.emailfrontend.utils.{FakeIdentifierAgentAction, SpecBase}

class SignOutControllerSpec extends SpecBase {

  "SignOut" should {

    "redirect to feedback survey" in new Setup {

      running(app) {
        val request = FakeRequest(GET, routes.SignOutController.signOut.url)
        val result  = route(app, request).value

        status(result)               shouldBe SEE_OTHER
        redirectLocation(result).value should endWith("/feedback/manage-email-cds")
      }
    }

    "clear the session once the user signs out" in new Setup {

      running(app) {
        val signOutRequest = FakeRequest(GET, routes.SignOutController.signOut.url)
        val signOutResult  = route(app, signOutRequest).value

        status(signOutResult) shouldBe SEE_OTHER

        val startPageRequest = FakeRequest(GET, routes.SignOutController.signOut.url)
        val startPageResult  = route(app, startPageRequest).value

        session(startPageResult) shouldBe Session.emptyCookie
      }
    }
  }

  "logoutNoSurvey" should {
    "redirect to loginContinue page" in new Setup {

      running(app) {
        val logOutNoSurveyRequest = FakeRequest(GET, routes.SignOutController.logoutNoSurvey.url)
        val result                = route(app, logOutNoSurveyRequest).value

        status(result)               shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe config.loginContinueUrl
      }
    }
  }

  trait Setup {
    val app: Application = applicationBuilder[FakeIdentifierAgentAction]().build()

    implicit val config: AppConfig = app.injector.instanceOf[AppConfig]
  }
}
