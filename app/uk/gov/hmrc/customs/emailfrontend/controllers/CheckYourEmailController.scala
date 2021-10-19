/*
 * Copyright 2021 HM Revenue & Customs
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
import uk.gov.hmrc.customs.emailfrontend.controllers.actions.Actions
import uk.gov.hmrc.customs.emailfrontend.controllers.routes.{EmailConfirmedController, SignOutController, VerifyYourEmailController, WhatIsYourEmailController}
import uk.gov.hmrc.customs.emailfrontend.forms.Forms.confirmEmailForm
import uk.gov.hmrc.customs.emailfrontend.model._
import uk.gov.hmrc.customs.emailfrontend.services.{EmailVerificationService, Save4LaterService}
import uk.gov.hmrc.customs.emailfrontend.views.html.check_your_email
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckYourEmailController @Inject()(actions: Actions,
                                         view: check_your_email,
                                         emailVerificationService: EmailVerificationService,
                                         mcc: MessagesControllerComponents,
                                         save4LaterService: Save4LaterService,
                                         errorHandler: ErrorHandler)
                                        (implicit override val messagesApi: MessagesApi, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport with Logging {

  def show: Action[AnyContent] =
    (actions.auth andThen actions.isPermitted andThen actions.isEnrolled).async { implicit request =>
      save4LaterService.routeBasedOnAmendment(request.user.internalId)(
        redirectWithEmail,
        Future.successful(Redirect(SignOutController.signOut()))
      )
    }

  private def redirectWithEmail(details: EmailDetails)(implicit request: EoriRequest[AnyContent]): Future[Result] =
    Future.successful(Ok(view(confirmEmailForm, details.newEmail)))

  def submit: Action[AnyContent] = (actions.auth andThen actions.isEnrolled).async { implicit request =>
    save4LaterService.fetchEmail(request.user.internalId).flatMap {
      case Some(emailDetails) =>
        confirmEmailForm.bindFromRequest.fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, emailDetails.newEmail))),
          formData => locationByAnswer(request.user.internalId, formData, emailDetails, request.eori.id)
        )
      case None =>
        logger.warn("emailStatus cache none, user logged out")
        Future.successful(Redirect(SignOutController.signOut()))
    }
  }

  private def submitNewDetails(internalId: InternalId, details: EmailDetails, eori: String)(
    implicit request: Request[AnyContent]
  ): Future[Result] =
    emailVerificationService.createEmailVerificationRequest(details, EmailConfirmedController.show().url, eori).flatMap {
      case Some(true) => Future.successful(Redirect(VerifyYourEmailController.show()))
      case Some(false) =>
        save4LaterService.saveEmail(internalId, details.copy(timestamp = None)).map { _ =>
          Redirect(EmailConfirmedController.show())
        }
      case None => Future.successful(Redirect(routes.CheckYourEmailController.problemWithService()))
    }

  private def locationByAnswer(internalId: InternalId, confirmEmail: YesNo, details: EmailDetails, eori: String)(
    implicit request: Request[AnyContent]
  ): Future[Result] = confirmEmail.isYes match {
    case Some(true) => submitNewDetails(internalId, details, eori)
    case _ =>
      save4LaterService
        .remove(internalId)
        .flatMap(_ => Future.successful(Redirect(WhatIsYourEmailController.create())))
  }

  def problemWithService(): Action[AnyContent] = actions.auth.async { implicit request =>
    Future.successful(BadRequest(errorHandler.problemWithService()))
  }
}
