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
import play.api.test.Helpers.contentAsString
import uk.gov.hmrc.customs.emailfrontend.views.html.start_page

class StartPageViewSpec extends ViewSpec {

  private val view = app.injector.instanceOf[start_page]

  private val eori = "AB123456789"

  private val doc = Jsoup.parse(contentAsString(view.render(eori, request, messages)))

  "Start page" should {
    "have the correct title" in {
      doc.title mustBe "Manage your email address for the Customs Declaration Service"
    }

    "have the correct heading" in {
      doc.getElementsByTag("h1").text mustBe "Manage your email address for the Customs Declaration Service"
    }

    "have the correct text explaining what the service is for" in {
      doc.getElementById("bulletP").text mustBe "Use this service to change or verify the email address we use to send you:"
      doc.getElementById("b1").text mustBe "updates on changes to the Customs Declaration Service"
      doc.getElementById("b2").text mustBe "notifications of new import statements and payments"
      doc.getElementById("b3").text mustBe "export declaration notifications"
    }

    "have the correct text explaining what you'll need before starting " in {
      doc.getElementsByTag("h2").text mustBe "Before you start"
      doc.getElementById("beforeStartS").text mustBe "You’ll need the Government Gateway user ID and password you used to apply for your EORI number or to get access to Customs Declaration Service."
    }

    "have the correct text on the button" in {
      doc.getElementsByClass("button--get-started").text mustBe "Start now"

    }

    "have the correct related content" in {
      doc.getElementById("subsection-title").text mustBe "Related content"
      doc.getElementById("link1").text mustBe "Check the status of an application you have already made"
      doc.getElementById("link1").attr("href") mustBe "gg-sign-in?details="

      doc.getElementById("link2").text mustBe "Check if you need to register with HMRC for importing or exporting"
      doc.getElementById("link2").attr("href") mustBe "/version20/eori-check-start"

      doc.getElementById("link3").text mustBe "The Duty Deferment Scheme"
      doc.getElementById("link3").attr("href") mustBe "https://www.gov.uk/government/publications/notice-101-deferring-duty-vat-and-other-charges?_nfpb=true&amp;_pageLabel=pageVAT_ShowContent&amp;id=HMCE_CL_000013&amp;propertyType=document"

      doc.getElementById("link4").text mustBe "VAT refunds on imported goods"
      doc.getElementById("link4").attr("href") mustBe "https://www.gov.uk/duty-relief-for-imports-and-exports"

      doc.getElementById("link5").text mustBe "Tariff data"
      doc.getElementById("link5").attr("href") mustBe "https://www.gov.uk/trade-tariff"
    }
  }

}
