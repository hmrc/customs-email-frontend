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

package unit.views

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.test.Helpers.contentAsString
import uk.gov.hmrc.customs.emailfrontend.forms.Forms
import uk.gov.hmrc.customs.emailfrontend.model.Email
import uk.gov.hmrc.customs.emailfrontend.views.html.what_is_your_email

class WhatIsYourEmailViewSpec extends ViewSpec {
  private val view = app.injector.instanceOf[what_is_your_email]
  private val form: Form[Email] = Forms.emailForm
  private val email: String = "email@test.com"
  private val formWithEmptyError: Form[Email] = Forms.emailForm.bind(Map("email" -> ""))
  private val formWithWrongFormatError: Form[Email] = Forms.emailForm.bind(Map("email" -> "invalid"))
  private val formWithTooLongError: Form[Email] = Forms.emailForm.bind(Map("email" -> "abcdefghijklmnopqrstuvwxyz1234567890@abcdefghijklmnopqrstuvwxyz1234567890"))
  private val doc: Document = Jsoup.parse(contentAsString(view(form, email)))
  private val docWithEmptyError: Document = Jsoup.parse(contentAsString(view.render(formWithEmptyError, email, request, messages)))
  private val docWithWrongFormatError: Document = Jsoup.parse(contentAsString(view.render(formWithWrongFormatError, email, request, messages)))
  private val docWithTooLongError: Document = Jsoup.parse(contentAsString(view.render(formWithTooLongError, email, request, messages)))

  "Email page" should {
    "have the correct title" in {
      doc.title mustBe "Change your email address for CDS"
    }

    "have the correct heading" in {
      doc.getElementsByTag("h1").text mustBe "Change your email address for CDS"
    }

    "have the correct label" in {
      doc.getElementsByTag("label").text mustBe "We'll use your new email address to replace email@test.com"
    }

    "display correct error when no email is entered" in {
      docWithEmptyError.getElementById("errors").text contains "Enter your email address"
      docWithEmptyError.getElementsByClass("error-message").text mustBe "Enter your email address"
    }

    "display correct error when email is entered with wrong format" in {
      docWithWrongFormatError.getElementById("errors").text contains "Enter a valid email address"
      docWithWrongFormatError.getElementsByClass("error-message").text mustBe "Enter a valid email address"
    }

    "display correct error when email is entered which is too long" in {
      docWithTooLongError.getElementById("errors").text contains "The email address must be 50 characters or less"
      docWithTooLongError.getElementsByClass("error-message").text mustBe "The email address must be 50 characters or less"
    }
  }
}
