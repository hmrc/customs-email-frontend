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

package uk.gov.hmrc.customs.emailfrontend.controllers

import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.customs.emailfrontend.config.ErrorHandler
import uk.gov.hmrc.customs.emailfrontend.controllers.actions.IdentifierAction
import uk.gov.hmrc.customs.emailfrontend.forms.Forms.confirmEmailForm
import uk.gov.hmrc.customs.emailfrontend.model._
import uk.gov.hmrc.customs.emailfrontend.services.{Save4LaterService}
import uk.gov.hmrc.customs.emailfrontend.views.html.{check_your_email}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckYourEmailController @Inject()(identify: IdentifierAction,
                                         view: check_your_email,
                                         mcc: MessagesControllerComponents,
                                         save4LaterService: Save4LaterService,
                                         errorHandler: ErrorHandler)
                                        (implicit override val messagesApi: MessagesApi, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport with Logging {

  def show: Action[AnyContent] =
    identify.async { implicit request =>
      save4LaterService.routeBasedOnAmendment(request.user.internalId)(
        details => Future.successful(Ok(view(confirmEmailForm, details.newEmail))),
        noEmail = Future.successful(Redirect(routes.SignOutController.signOut))
      )
    }

  def submit: Action[AnyContent] = identify.async { implicit request =>
    save4LaterService.fetchEmail(request.user.internalId).flatMap {
      case Some(emailDetails) =>
        confirmEmailForm.bindFromRequest().fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, emailDetails.newEmail))),
          formData => handleYesNo(request.user.internalId, formData)
        )
      case None =>
        logger.warn("emailStatus cache none, user logged out")
        Future.successful(Redirect(routes.SignOutController.signOut))
    }
  }

  private def handleYesNo(internalId: InternalId, confirmEmail: YesNo)
                         (implicit request: Request[AnyContent]): Future[Result] =
    confirmEmail.isYes match {
      case Some(true) => Future.successful(Redirect(routes.ChangingYourEmailController.show))
      case _ =>
        save4LaterService
          .remove(internalId)
          .flatMap(_ => Future.successful(Redirect(routes.WhatIsYourEmailController.whatIsEmailAddress)))
    }

  def problemWithService(): Action[AnyContent] = identify.async { implicit request =>
    Future.successful(BadRequest(errorHandler.problemWithService()))
  }
}
