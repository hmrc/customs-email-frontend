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
import play.api.test.Helpers.contentAsString
import uk.gov.hmrc.customs.emailfrontend.views.html.start_page

class StartPageViewSpec extends ViewSpec {

  private val view = app.injector.instanceOf[start_page]

  private val eori = "AB123456789"

  private val doc = Jsoup.parse(contentAsString(view(eori)))

  "Start page" should {
    "have the correct title" in {
      doc.title mustBe "Hello from customs-email-frontend"
    }

    "have the correct heading" in {
      doc.getElementsByTag("h1").text mustBe "Hello from customs-email-frontend AB123456789 !"
    }
  }

}
