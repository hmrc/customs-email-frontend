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
import uk.gov.hmrc.customs.emailfrontend.config.AppConfig
import uk.gov.hmrc.customs.emailfrontend.controllers.actions.Actions
import uk.gov.hmrc.customs.emailfrontend.views.html.email_page
import uk.gov.hmrc.customs.emailfrontend.forms.Forms.emailForm
import play.api.mvc.Results.BadRequest

import scala.concurrent.Future

@Singleton
class EmailController @Inject()(actions: Actions, view: email_page)(implicit appConfig: AppConfig, override val messagesApi: MessagesApi) extends I18nSupport {


  def show: Action[AnyContent] = actions.auth { implicit request =>
    val form = emailForm
    Ok(view(form))
  }

  def submit: Action[AnyContent] = actions.auth.async { implicit request =>
      emailForm.bindFromRequest.fold(
        formWithErrors => {
          Future.successful(
            BadRequest(view(emailForm = formWithErrors))
          )
        },
        formData => {
          //todo: cache input email and go to another page
          val form = emailForm
          Future.successful(Ok(view(form)))
        }
      )
  }
}
