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
import play.api.test.Helpers.contentAsString
import uk.gov.hmrc.customs.emailfrontend.model.Ineligible
import uk.gov.hmrc.customs.emailfrontend.views.html.ineligible_user

class IneligibleUserViewSpec extends ViewSpec {

  private val view = app.injector.instanceOf[ineligible_user]

  private val doc: Document = Jsoup.parse(contentAsString(view.render(Ineligible.NoEnrolment, request, messages)))

  "IneligibleUser with no cds enrolment" should {
     val doc: Document = Jsoup.parse(contentAsString(view.render(Ineligible.NoEnrolment, request, messages)))

    "have the correct title" in {
      doc.title mustBe "You cannot use this service"
    }

    "have the correct heading" in {
      doc.body.getElementsByTag("h1").text mustBe "You cannot use this service"
    }
    "have the correct message no cds enrolment" in {
      doc.body.getElementById("info1").text mustBe "You must be enrolled to CDS to use this service."
      doc.body.getElementById("info2").text mustBe "You signed in to Government Gateway with CDS enrolled account."
    }
  }

  "IneligibleUser with Agent account" should {
    val doc: Document = Jsoup.parse(contentAsString(view.render(Ineligible.IsAgent, request, messages)))

    "have the correct title" in {
      doc.title mustBe "You cannot use this service"
    }

    "have the correct heading" in {
      doc.body.getElementsByTag("h1").text mustBe "You cannot use this service"
    }
    "have the correct message for an Agent" in {
      doc.body.getElementById("info1").text mustBe "You signed in to Government Gateway with an agent services account."
      doc.body.getElementById("info2").text mustBe "You need to sign in with the Government Gateway for the organisation or individual that is changing their email for CDS."
    }
  }

  "IneligibleUser for Organisation and not an Admin" should {
    val doc: Document = Jsoup.parse(contentAsString(view.render(Ineligible.NotAdmin, request, messages)))

    "have the correct title" in {
      doc.title mustBe "You cannot use this service"
    }

    "have the correct heading" in {
      doc.body.getElementsByTag("h1").text mustBe "You cannot use this service"
    }
    "have the correct message an Assistant account" in {
      doc.body.getElementById("info1").text mustBe "You signed in to Government Gateway as a standard user. To change your email for CDS you must be an administrator user."
      doc.body.getElementById("info2").text mustBe "Contact the person who set up your Government Gateway."
    }
  }
}
