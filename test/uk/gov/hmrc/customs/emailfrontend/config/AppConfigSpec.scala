/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.customs.emailfrontend.config

import org.scalatest.matchers.must.Matchers.mustEqual
import uk.gov.hmrc.customs.emailfrontend.model.ReferrerName
import uk.gov.hmrc.customs.emailfrontend.utils.SpecBase

class AppConfigSpec extends SpecBase {

  "AppConfig" should {

    "load external URLs correctly" in {
      appConfigInstance.accessibilityLinkUrl mustEqual "http://localhost:12346/accessibility-statement/manage-email-cds"
      appConfigInstance.ggSignInRedirectUrl mustEqual "http://localhost:9898/manage-email-cds/change-email-address"
      appConfigInstance.feedbackUrl mustEqual "http://localhost:9514/feedback/manage-email-cds"
      appConfigInstance.loginContinueUrl mustEqual "http://localhost:9898/manage-email-cds/start"
    }

    "load email verification settings correctly" in {
      appConfigInstance.emailVerificationTemplateId mustEqual "verifyEmailAddress"
      appConfigInstance.emailVerificationLinkExpiryDuration mustEqual "P3D"
      appConfigInstance.checkVerifiedEmailUrl mustEqual "http://localhost:9744/email-verification/verified-email-check"
      appConfigInstance.createEmailVerificationRequestUrl mustEqual "http://localhost:9744/email-verification/verification-requests"
    }

    "load service URLs correctly" in {
      appConfigInstance.customsDataStoreUrl mustEqual "http://localhost:9893/customs-data-store/update-email"
      appConfigInstance.subscriptionDisplayUrl mustEqual "http://localhost:8989/subscription-display"
      appConfigInstance.updateVerifiedEmailUrl mustEqual "http://localhost:8989/update-verified-email"
      appConfigInstance.save4LaterUrl mustEqual "http://localhost:8989/save4later"
    }

    "load timeout and countdown values correctly" in {
      appConfigInstance.timeout mustEqual 900
      appConfigInstance.countdown mustEqual 120
    }

    "load referrer services correctly" in {
      val expectedReferrers = Seq(
        ReferrerName("customs-finance", "/customs/payment-records"),
        ReferrerName("customs-exports", "/customs-declare-exports/"),
        ReferrerName("cds-file-upload", "/cds-file-upload-service/"),
        ReferrerName(
          "cds-reimbursement-claim",
          "/claim-for-reimbursement-of-import-duties/enter-movement-reference-number/"
        ),
        ReferrerName(
          "report-or-check-de-minimis-aid-northern-ireland",
          "/report-or-check-de-minimis-aid-northern-ireland/"
        ),
        ReferrerName("trader-goods-profiles", "/trader-goods-profiles/")
      )
      appConfigInstance.referrerName mustEqual expectedReferrers
    }

    "load specific referrer services correctly" in {
      appConfigInstance.customsFinanceReferrer mustEqual Some(
        ReferrerName("customs-finance", "/customs/payment-records")
      )
      appConfigInstance.traderGoodsProfilesReferrer mustEqual Some(
        ReferrerName("trader-goods-profiles", "/trader-goods-profiles/")
      )
    }
  }
}
