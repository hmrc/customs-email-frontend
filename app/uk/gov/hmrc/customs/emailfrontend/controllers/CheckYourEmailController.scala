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

import javax.inject.Inject
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.customs.emailfrontend.controllers.actions.Actions
import uk.gov.hmrc.customs.emailfrontend.controllers.routes.{EmailConfirmedController, SignOutController, VerifyYourEmailController, WhatIsYourEmailController}
import uk.gov.hmrc.customs.emailfrontend.forms.Forms.confirmEmailForm
import uk.gov.hmrc.customs.emailfrontend.model.{EmailStatus, InternalId, YesNo}
import uk.gov.hmrc.customs.emailfrontend.services.{EmailCacheService, EmailVerificationService}
import uk.gov.hmrc.customs.emailfrontend.views.html.check_your_email
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

class CheckYourEmailController @Inject()(actions: Actions,
                                         view: check_your_email,
                                         emailVerificationService: EmailVerificationService,
                                         mcc: MessagesControllerComponents,
                                         emailCacheService: EmailCacheService)(implicit override val messagesApi: MessagesApi, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def show: Action[AnyContent] = (actions.authEnrolled andThen actions.isPermitted).async { implicit request =>
    emailCacheService.fetchEmail(request.user.internalId) flatMap {
      _.fold {
        Logger.warn("[CheckYourEmailController][show] - emailStatus cache none, user logged out")
        Future.successful(Redirect(SignOutController.signOut()))
      } {
        emailStatus =>
          Future.successful(Ok(view(confirmEmailForm, emailStatus.email)))
      }
    }
  }

  def submit: Action[AnyContent] = actions.authEnrolled.async { implicit request =>
    emailCacheService.fetchEmail(request.user.internalId) flatMap {
      _.fold {
        Logger.warn("[CheckYourEmailController][submit] - emailStatus cache none, user logged out")
        Future.successful(Redirect(SignOutController.signOut()))
      } {
        emailStatus =>
          confirmEmailForm.bindFromRequest.fold(
            formWithErrors => {
              Future.successful(BadRequest(view(formWithErrors, emailStatus.email)))
            },
            formData =>
              locationByAnswer(request.user.internalId, formData, emailStatus.email)
          )
      }
    }
  }

  private def submitNewDetails(internalId: InternalId, email: String)(implicit hc: HeaderCarrier): Future[Result] = {
    emailVerificationService.createEmailVerificationRequest(email, EmailConfirmedController.show().url) flatMap {
      case Some(true) => Future.successful(Redirect(VerifyYourEmailController.show()))
      case Some(false) =>
        Logger.warn(
          "[CheckYourEmailController][sendVerification] - " +
            "Unable to send email verification request. Service responded with 'already verified'"
        )
        emailCacheService.saveEmail(internalId, EmailStatus(email, isVerified = true)).map { _ =>
          Redirect(EmailConfirmedController.show())
        }
      case None => throw new IllegalStateException("CreateEmailVerificationRequest Failed")
    }
  }

  private def locationByAnswer(internalId: InternalId, confirmEmail: YesNo, email: String)(implicit request: Request[AnyContent]): Future[Result] = confirmEmail.isYes match {
    case Some(true) => submitNewDetails(internalId, email)
    case _ => Future.successful(Redirect(WhatIsYourEmailController.create()))
  }
}
