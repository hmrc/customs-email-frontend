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

import play.api.i18n.Lang.logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.*
import uk.gov.hmrc.customs.emailfrontend.config.ErrorHandler
import uk.gov.hmrc.customs.emailfrontend.connectors.EmailConnector
import uk.gov.hmrc.customs.emailfrontend.connectors.httpparsers.EmailVerificationRequestHttpParser.*
import uk.gov.hmrc.customs.emailfrontend.controllers.actions.IdentifierAction
import uk.gov.hmrc.customs.emailfrontend.forms.Forms.emailForm
import uk.gov.hmrc.customs.emailfrontend.model.{EmailDetails, InternalId}
import uk.gov.hmrc.customs.emailfrontend.services.{EmailVerificationService, Save4LaterService}
import uk.gov.hmrc.customs.emailfrontend.utils.Utils.emptyString
import uk.gov.hmrc.customs.emailfrontend.views.html.changing_your_email
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ChangingYourEmailController @Inject() (
  identify: IdentifierAction,
  view: changing_your_email,
  mcc: MessagesControllerComponents,
  emailVerificationService: EmailVerificationService,
  save4LaterService: Save4LaterService,
  emailConnector: EmailConnector,
  errorHandler: ErrorHandler
)(implicit override val messagesApi: MessagesApi, ec: ExecutionContext)
    extends FrontendController(mcc)
    with I18nSupport {

  def show: Action[AnyContent] = Action { implicit request =>
    Ok(view(emailForm))
  }

  def submit: Action[AnyContent] = identify.async { implicit request =>
    save4LaterService.fetchEmail(request.user.internalId).flatMap {

      case Some(emailDetails) =>
        callEmailVerificationService(request.user.internalId, emailDetails, request.user.eori)

      case None =>
        logger.warn("emailStatus cache none, user logged out")
        Future.successful(Redirect(routes.SignOutController.signOut))
    }
  }

  private def callEmailVerificationService(internalId: InternalId, details: EmailDetails, eori: String)(implicit
    request: Request[AnyContent]
  ): Future[Result] =
    emailVerificationService
      .createEmailVerificationRequest(details, routes.EmailConfirmedController.show.url, eori)
      .flatMap {

        case Some(EmailVerificationRequestSent) =>
          Future.successful(Redirect(routes.VerifyYourEmailController.show))

        case Some(EmailAlreadyVerified) =>
          sendEmailToCurrentAndNewEmailAddress(details)

          save4LaterService.saveEmail(internalId, details.copy(timestamp = None)).map { _ =>
            Redirect(routes.EmailConfirmedController.show)
          }

        case _ =>
          Future.successful(Redirect(routes.CheckYourEmailController.problemWithService()))
      }

  private def sendEmailToCurrentAndNewEmailAddress(details: EmailDetails)(implicit
    request: Request[AnyContent]
  ) =
    List(details.currentEmail.getOrElse(emptyString), details.newEmail).map { emailAddress =>
      logger.info(s"Email is being sent to $emailAddress")

      emailConnector
        .sendEmail(
          emailAddress,
          "customs_financials_change_email_address",
          Map("emailAddress" -> details.currentEmail.getOrElse(emptyString))
        )
        .map {
          case httpResponse if isSuccessfulResponse(httpResponse.status) =>
            logger.info(s"Email has been sent to $emailAddress")

          case httpResponse =>
            logger.warn(s"Error occurred while sending email to $emailAddress")
        }
    }

  def problemWithService(): Action[AnyContent] = identify.async { implicit request =>
    Future.successful(BadRequest(errorHandler.problemWithService()))
  }

  private def isSuccessfulResponse(status: Int) = List(CREATED, ACCEPTED, OK).contains(status)
}
