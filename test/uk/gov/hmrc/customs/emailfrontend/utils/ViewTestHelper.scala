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

package uk.gov.hmrc.customs.emailfrontend.utils

import org.jsoup.nodes.Document
import org.scalatest.Assertion
import org.scalatest.matchers.must.Matchers.mustBe

trait ViewTestHelper extends SpecBase {

  def shouldContainCorrectServiceUrls(viewDoc: String, routes: String): Assertion = {
    viewDoc.contains(routes) mustBe true
    viewDoc.contains("/accessibility-statement/manage-email-cds") mustBe true
  }

  def shouldContainBackLinkUrl(viewDoc: Document, url: String): Assertion = {
    viewDoc.getElementsByClass("govuk-back-link").text() mustBe "Back"
    viewDoc.html().contains(url) mustBe true
  }

  def shouldContainCorrectBanners(viewDoc: Document): Assertion =
    viewDoc
      .getElementsByClass("govuk-phase-banner")
      .text() mustBe "BETA This is a new service – your feedback will help us to improve it."
}
