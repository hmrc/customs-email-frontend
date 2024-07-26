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
import uk.gov.hmrc.customs.emailfrontend.connectors.SubscriptionDisplayConnector
import uk.gov.hmrc.customs.emailfrontend.connectors.httpparsers.EmailVerificationRequestHttpParser.{EmailAlreadyVerified, EmailVerificationRequestSent}
import uk.gov.hmrc.customs.emailfrontend.controllers.actions.IdentifierAction
import uk.gov.hmrc.customs.emailfrontend.forms.Forms.confirmVerifyChangeForm
import uk.gov.hmrc.customs.emailfrontend.model._
import uk.gov.hmrc.customs.emailfrontend.services.{EmailVerificationService, Save4LaterService}
import uk.gov.hmrc.customs.emailfrontend.views.html.verify_change_email
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.customs.emailfrontend.utils.Utils.emptyString

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class VerifyChangeEmailController @Inject()(identify: IdentifierAction,
                                            view: verify_change_email,
                                            save4LaterService: Save4LaterService,
                                            mcc: MessagesControllerComponents,
                                            subscriptionDisplayConnector: SubscriptionDisplayConnector,
                                            emailVerificationService: EmailVerificationService,
                                            errorHandler: ErrorHandler)
                                           (implicit override val messagesApi: MessagesApi,
                                            ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport with Logging {

  def show: Action[AnyContent] =
    identify.async { implicit request =>
      save4LaterService.routeBasedOnAmendment(request.user.internalId)(
        redirectBasedOnEmailStatus,
        Future.successful(Redirect(routes.VerifyChangeEmailController.create)))
    }

  private def redirectBasedOnEmailStatus(details: EmailDetails)
                                        (implicit request: Request[AnyContent]): Future[Result] =
    emailVerificationService.isEmailVerified(details.newEmail).map {
      case Some(true) => Redirect(routes.EmailConfirmedController.show)
      case Some(false) => Redirect(routes.CheckYourEmailController.show)
      case None => InternalServerError(errorHandler.problemWithService())
    }

  def create: Action[AnyContent] = identify.async { implicit request =>

    save4LaterService.routeBasedOnAmendment(request.user.internalId)(
      details =>
        (details.currentEmail, details.newEmail) match {
          case (Some(currentEmail), _) =>
            Future.successful(Ok(view(confirmVerifyChangeForm, Some(currentEmail))))
          case _ => Future.successful(Redirect(routes.WhatIsYourEmailController.problemWithService()))
        },
      subscriptionDisplay()
    )
  }

  private def subscriptionDisplay()(implicit request: AuthenticatedRequest[AnyContent]) =
    subscriptionDisplayConnector.subscriptionDisplay(request.user.eori).flatMap {
      case SubscriptionDisplayResponse(Some(email), _, _, _) =>
        Future.successful(Ok(view(confirmVerifyChangeForm, Some(email))))

      case SubscriptionDisplayResponse(_, _, Some("Processed Successfully"), _) =>
        Future.successful(Redirect(routes.WhatIsYourEmailController.verify))

      case SubscriptionDisplayResponse(None, _, Some(_), Some("FAIL")) =>
        Future.successful(Redirect(routes.VerifyChangeEmailController.problemWithService()))

      case SubscriptionDisplayResponse(None, _, None, None) =>
        Future.successful(Redirect(routes.WhatIsYourEmailController.verify))

      case SubscriptionDisplayResponse(None, None, _, _) =>
        Future.successful(Redirect(routes.WhatIsYourEmailController.verify))

      case _ => Future.successful(Redirect(routes.VerifyChangeEmailController.problemWithService()))
    }.recover {
      handleNonFatalException()
    }

  private def handleNonFatalException(): PartialFunction[Throwable, Result] = {
    case NonFatal(e) => {
      logger.error(s"Subscription display failed with ${e.getMessage}")
      Redirect(routes.VerifyChangeEmailController.problemWithService())
    }
  }

  def verifyChangeEmail: Action[AnyContent] = identify.async { implicit request =>
    subscriptionDisplayConnector.subscriptionDisplay(request.user.eori).flatMap {

      case SubscriptionDisplayResponse(Some(email), _, _, _) =>
        confirmVerifyChangeForm.bindFromRequest().fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, Some(email)))),
          formData =>
            formData.isVerify match {

              case Some(false) =>
                save4LaterService.saveJourneyType(request.user.internalId, JourneyType(formData.isVerify.get))

                save4LaterService.saveEmail(request.user.internalId,
                  EmailDetails(Some(email), emptyString, None)).map { _ =>

                  Redirect(routes.WhatIsYourEmailController.whatIsEmailAddress)
                }

              case Some(true) => {
                save4LaterService.saveJourneyType(request.user.internalId, JourneyType(formData.isVerify.get))

                callEmailVerificationService(request.user.internalId,
                  EmailDetails(Some(email), email, None), request.user.eori)
              }

              case _ =>
                Future.successful(Redirect(routes.VerifyChangeEmailController.problemWithService()))
            }
        )
      case _ =>
        Future.successful(Redirect(routes.VerifyChangeEmailController.problemWithService()))
    }
  }

  private def callEmailVerificationService(internalId: InternalId, details: EmailDetails, eori: String)
                                          (implicit request: Request[AnyContent]): Future[Result] =
    emailVerificationService.createEmailVerificationRequest(
      details, routes.EmailConfirmedController.show.url, eori).flatMap {

      case Some(EmailVerificationRequestSent) => Future.successful(Redirect(routes.VerifyYourEmailController.show))

      case Some(EmailAlreadyVerified) =>
        save4LaterService.saveEmail(internalId, details.copy(timestamp = None)).map { _ =>
          Redirect(routes.EmailConfirmedController.show)
        }

      case _ => Future.successful(Redirect(routes.CheckYourEmailController.problemWithService()))
    }

  def problemWithService(): Action[AnyContent] = identify.async { implicit request =>
    errorHandler.problemWithService().map(html => BadRequest(html))
  }
}
