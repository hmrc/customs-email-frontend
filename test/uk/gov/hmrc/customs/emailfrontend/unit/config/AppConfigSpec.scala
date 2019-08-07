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

package uk.gov.hmrc.customs.emailfrontend.unit.config

import uk.gov.hmrc.customs.emailfrontend.unit.controllers.ControllerSpec

class AppConfigSpec extends ControllerSpec {

  "AppConfig" should {

    "have reportAProblemNonJSUrl defined" in {
      appConfig.reportAProblemNonJSUrl shouldBe "http://localhost:9250/contact/problem_reports_nonjs?service=CDS"
    }
    "have reportAProblemPartialUrl defined" in {
      appConfig.reportAProblemPartialUrl shouldBe "http://localhost:9250/contact/problem_reports_ajax?service=CDS"
    }
    "have assetsPrefix defined" in {
      appConfig.assetsPrefix shouldBe "http://localhost:9032/assets/3.4.0"
    }
    "have ggSignInRedirectUrl defined" in {
      appConfig.ggSignInRedirectUrl shouldBe "http://localhost:9898/customs-email-frontend/start"
    }
    "have analyticsToken defined" in {
      appConfig.analyticsToken shouldBe "N/A"
    }
    "have analyticsHost defined" in {
      appConfig.analyticsHost shouldBe "auto"
    }
  }
}