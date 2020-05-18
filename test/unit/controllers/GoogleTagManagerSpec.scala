/*
 * Copyright 2020 HM Revenue & Customs
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

package unit.controllers

import unit.views.ViewSpec
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.test.Helpers.contentAsString
import uk.gov.hmrc.customs.emailfrontend.forms.Forms
import uk.gov.hmrc.customs.emailfrontend.model.Email
import uk.gov.hmrc.customs.emailfrontend.views.html.what_is_your_email

class GoogleTagManagerSpec extends ViewSpec {

  private val view = app.injector.instanceOf[what_is_your_email]
  private val form: Form[Email] = Forms.emailForm
  private val doc: Document = Jsoup.parse(contentAsString(view.render(form, request, messages)))

  "Google Tag Manager" should {
    "include the javascript file in the header" in {
      doc.head().getElementsByTag("script").get(2).attr("src") must include("google-tag-manager.js")
    }

    "include a noscript snippet in the body" in {
      doc.body().getElementsByTag("iframe").attr("src") must include(
        "https://www.googletagmanager.com/ns.html?id=GTM-NDJKHWK"
      )
    }
  }
}
