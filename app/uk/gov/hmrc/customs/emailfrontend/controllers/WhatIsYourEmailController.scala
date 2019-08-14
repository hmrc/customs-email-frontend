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
import play.api.mvc.Results.{BadRequest, Ok}
import play.api.mvc._
import uk.gov.hmrc.customs.emailfrontend.config.AppConfig
import uk.gov.hmrc.customs.emailfrontend.controllers.actions.Actions
import uk.gov.hmrc.customs.emailfrontend.forms.Forms.{confirmEmailForm, emailForm}
import uk.gov.hmrc.customs.emailfrontend.views.html.{confirm_email, what_is_your_email}

import scala.concurrent.Future

@Singleton
class WhatIsYourEmailController @Inject()(actions: Actions, view: what_is_your_email, nextView: confirm_email)(implicit val messagesApi: MessagesApi) extends I18nSupport {


  def show: Action[AnyContent] = actions.auth { implicit request =>
    Ok(view(emailForm))
  }

  def submit: Action[AnyContent] = actions.auth.async { implicit request =>
    emailForm.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(
          BadRequest(view(emailForm = formWithErrors))
        )
      },
      formData => {
        Future.successful(Ok(nextView(confirmEmailForm)))
      }
    )
  }
}
