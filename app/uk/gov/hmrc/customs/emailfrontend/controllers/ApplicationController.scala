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

package uk.gov.hmrc.customs.emailfrontend.controllers

import javax.inject.{Inject, Singleton}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results.Ok
import play.api.mvc._
import uk.gov.hmrc.customs.emailfrontend.controllers.actions.Actions
import uk.gov.hmrc.customs.emailfrontend.views.html.start_page

@Singleton
class ApplicationController @Inject()(actions: Actions, view: start_page)(implicit val messagesApi: MessagesApi) extends I18nSupport {

  def show: Action[AnyContent] = (actions.authEnrolled andThen actions.eori) { implicit request =>
    Ok(view(request.eori.id))
  }

  def keepAlive: Action[AnyContent] = actions.unauthorised { implicit request =>
      Ok("Ok")
  }
}
