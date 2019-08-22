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
import play.api.mvc._
import uk.gov.hmrc.customs.emailfrontend.controllers.actions.Actions
import uk.gov.hmrc.customs.emailfrontend.forms.Forms.emailForm
import uk.gov.hmrc.customs.emailfrontend.model.EmailStatus
import uk.gov.hmrc.customs.emailfrontend.services.EmailCacheService
import uk.gov.hmrc.customs.emailfrontend.views.html.what_is_your_email
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WhatIsYourEmailController @Inject()(actions: Actions, view: what_is_your_email, emailCacheService: EmailCacheService, mcc: MessagesControllerComponents)
                                         (implicit override val messagesApi: MessagesApi, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def show: Action[AnyContent] = actions.auth { implicit request =>
    Ok(view(emailForm))
  }

  def submit: Action[AnyContent] = actions.auth.async { implicit request =>
    emailForm.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(BadRequest(view(formWithErrors)))
      },
      formData => {
        emailCacheService.saveEmail(Some(request.user.internalId.id), EmailStatus(formData.email)).map {
          _ => Redirect(routes.CheckYourEmailController.show())
        }
      }
    )
  }
}
