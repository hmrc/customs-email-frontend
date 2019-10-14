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

package uk.gov.hmrc.customs.emailfrontend.config

import javax.inject.{Inject, Singleton}
import play.api.i18n.MessagesApi
import play.api.mvc.Request
import play.twirl.api.Html
import uk.gov.hmrc.customs.emailfrontend.views.html.partials.error_template
import uk.gov.hmrc.customs.emailfrontend.views.html.problem_with_this_service
import uk.gov.hmrc.play.bootstrap.http.FrontendErrorHandler

@Singleton
class ErrorHandler @Inject()(override val messagesApi: MessagesApi,
                             errorView: error_template,
                             customErrorView: problem_with_this_service) extends FrontendErrorHandler {
  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit request: Request[_]): Html =
    errorView(pageTitle, heading, message)

  def problemWithService()(implicit request: Request[_]): Html = { customErrorView() }
}
