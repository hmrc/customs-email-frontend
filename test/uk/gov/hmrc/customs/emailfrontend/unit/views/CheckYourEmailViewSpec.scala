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

package uk.gov.hmrc.customs.emailfrontend.unit.views

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.test.Helpers.contentAsString
import uk.gov.hmrc.customs.emailfrontend.forms.Forms
import uk.gov.hmrc.customs.emailfrontend.model.YesNo
import uk.gov.hmrc.customs.emailfrontend.views.html.check_your_email

class CheckYourEmailViewSpec extends ViewSpec {
  private val view = app.injector.instanceOf[check_your_email]
  private val form: Form[YesNo] = Forms.confirmEmailForm
  private val formWithEmptyError: Form[YesNo] = Forms.confirmEmailForm.bind(Map("isYes" -> ""))
  private val doc: Document = Jsoup.parse(contentAsString(view(form, "test@email.com")))
  private val docWithEmptyError: Document = Jsoup.parse(contentAsString(view(formWithEmptyError, "test@email.com")))

  "Confirm Email page" should {
    "have the correct title" in {
      doc.title mustBe "Check your email address"
    }

    "have the correct heading" in {
      doc.getElementsByTag("h1").text mustBe "Check your email address"
    }

    "have the correct content" in {
      doc.getElementsByTag("dt").text mustBe "Email address"
      doc.getElementById("whatNext").text mustBe "We'll send you a link to confirm your email address."
    }

    "have the correct field heading" in {
      doc.getElementsByClass("heading-medium").text mustBe "Is this the email address you want to use?"
    }

    "have the correct text on options" in {
      doc.getElementsByTag("label").text mustBe "Yes, this is the email address I want to use No, I need to change this email"
    }

    "have the correct error when an option isn't selected" in {
      docWithEmptyError.getElementById("errors").text contains "Tell us if this is the correct email address"
      docWithEmptyError.getElementsByClass("error-message").text mustBe "Tell us if this is the correct email address"
    }
  }
}
