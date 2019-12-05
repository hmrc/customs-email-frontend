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

package unit.config

import unit.controllers.ControllerSpec

class AppConfigSpec extends ControllerSpec {

  "AppConfig" should {

    "have reportAProblemNonJSUrl defined" in {
      appConfig.reportAProblemNonJSUrl shouldBe "http://localhost:9250/contact/problem_reports_nonjs?service=CDS"
    }
    "have reportAProblemPartialUrl defined" in {
      appConfig.reportAProblemPartialUrl shouldBe "http://localhost:9250/contact/problem_reports_ajax?service=CDS"
    }
    "have assetsPrefix defined" in {
      appConfig.assetsPrefix shouldBe "https://www.development.tax.service.gov.uk/assets/3.4.0"
    }
    "have ggSignInRedirectUrl defined" in {
      appConfig.ggSignInRedirectUrl shouldBe "http://localhost:9898/manage-email-cds/change-email-address"
    }
    "have analyticsToken defined" in {
      appConfig.analyticsToken shouldBe "N/A"
    }
    "have analyticsHost defined" in {
      appConfig.analyticsHost shouldBe "auto"
    }
    "have feedbackSurveyUrl defined" in {
      appConfig.feedbackUrl shouldBe "http://localhost:9514/feedback/CDS"
    }
    "have save4LaterDomain defined" in {
      appConfig.save4LaterDomain shouldBe "save4later"
    }
    "have save4LaterBaseUrl defined" in {
      appConfig.save4LaterBaseUrl shouldBe "http://localhost:9272"
    }
    "have emailVerificationBaseUrl defined" in {
      appConfig.emailVerificationBaseUrl shouldBe "http://localhost:9744"
    }
    "have emailVerificationWithContext defined" in {
      appConfig.emailVerificationWithContext shouldBe "http://localhost:9744/email-verification"
    }
    "have emailVerificationTemplateId defined" in {
      appConfig.emailVerificationTemplateId shouldBe "verifyEmailAddress"
    }
    "have emailVerificationLinkExpiryDuration defined" in {
      appConfig.emailVerificationLinkExpiryDuration shouldBe "P3D"
    }
    "have appName defined" in {
      appConfig.appName shouldBe "customs-email-frontend"
    }
    "have customsHodsProxyBaseUrl defined" in {
      appConfig.customsHodsProxyBaseUrl shouldBe "http://localhost:8989"
    }
    "have subscriptionDisplayContext defined" in {
      appConfig.subscriptionDisplayContext shouldBe "subscription-display"
    }
    "have subscriptionDisplayUrl defined" in {
      appConfig.subscriptionDisplayUrl shouldBe "http://localhost:8989/subscription-display"
    }
    "have updateVerifiedEmailContext defined" in {
      appConfig.updateVerifiedEmailContext shouldBe "update-verified-email"
    }
    "have updateVerifiedEmailUrl defined" in {
      appConfig.updateVerifiedEmailUrl shouldBe "http://localhost:8989/update-verified-email"
    }
  }
}
