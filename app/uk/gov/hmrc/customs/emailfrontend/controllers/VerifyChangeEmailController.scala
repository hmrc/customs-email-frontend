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
import uk.gov.hmrc.customs.emailfrontend.config.{AppConfig, ErrorHandler}
import uk.gov.hmrc.customs.emailfrontend.connectors.SubscriptionDisplayConnector
import uk.gov.hmrc.customs.emailfrontend.connectors.httpparsers.EmailVerificationRequestHttpParser.{EmailAlreadyVerified, EmailVerificationRequestSent}
import uk.gov.hmrc.customs.emailfrontend.controllers.actions.IdentifierAction
import uk.gov.hmrc.customs.emailfrontend.forms.Forms.{confirmVerifyChangeForm, emailForm}
import uk.gov.hmrc.customs.emailfrontend.model.{AuthenticatedRequest, EmailDetails, InternalId, SubscriptionDisplayResponse, VerifyChange}
import uk.gov.hmrc.customs.emailfrontend.services.{EmailVerificationService, Save4LaterService}
import uk.gov.hmrc.customs.emailfrontend.views.html.{verify_change_email, what_is_your_email}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class VerifyChangeEmailController @Inject()(identify: IdentifierAction,
                                            view: verify_change_email,
                                            whatIsYourEmailView: what_is_your_email,
                                            save4LaterService: Save4LaterService,
                                            mcc: MessagesControllerComponents,
                                            subscriptionDisplayConnector: SubscriptionDisplayConnector,
                                            emailVerificationService: EmailVerificationService,
                                            errorHandler: ErrorHandler,
                                            appConfig: AppConfig)
                                           (implicit override val messagesApi: MessagesApi,
                                            ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport with Logging {

  def show: Action[AnyContent] =
    identify.async { implicit request =>
      /*save4LaterService.routeBasedOnAmendment(request.user.internalId)(
        details =>
          (details.currentEmail, details.newEmail) match {
            case (Some(currentEmail), _) => Future.successful(Ok(view(confirmVerifyChangeForm, currentEmail)))
            case (None, _) => Future.successful(Ok(whatIsYourEmailView(emailForm)))
            case _ => Future.successful(Redirect(routes.WhatIsYourEmailController.problemWithService))
          },*/
        subscriptionDisplay
//      )
    }

  private def subscriptionDisplay()(implicit request: AuthenticatedRequest[AnyContent]) =
    subscriptionDisplayConnector.subscriptionDisplay(request.user.eori).flatMap {
      case SubscriptionDisplayResponse(Some(email), _, _, _) =>
        Future.successful(Ok(view(confirmVerifyChangeForm, email)))
      case _ => Future.successful(Redirect(routes.WhatIsYourEmailController.problemWithService))
    }.recover {
      handleNonFatalException()
    }

  private def handleNonFatalException(): PartialFunction[Throwable, Result] = {
    case NonFatal(e) => {
      logger.error(s"Subscription display failed with ${e.getMessage}")
      Redirect(routes.WhatIsYourEmailController.problemWithService)
    }
  }

  def submit: Action[AnyContent] = identify.async { implicit request =>
    logger.info(">>>>> Inside VerifyChangeEmailController submit")
    save4LaterService.fetchEmail(request.user.internalId).flatMap {
      case Some(emailDetails) =>
        confirmVerifyChangeForm.bindFromRequest.fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, emailDetails.newEmail))),
          formData => handleVerifyOrChange(request.user.internalId, formData, emailDetails, request.user.eori)
        )
      case None =>
        logger.warn("emailStatus cache none, user logged out")
        Future.successful(Redirect(routes.SignOutController.signOut))
    }
  }

  private def handleVerifyOrChange(internalId: InternalId, confirmVerifyChange: VerifyChange, details: EmailDetails, eori: String)
                         (implicit request: Request[AnyContent]): Future[Result] =
    confirmVerifyChange.isVerify match {
      case Some(true) => callEmailVerificationService(internalId, details, eori)
      case _ =>
         Future.successful(Redirect(routes.WhatIsYourEmailController.create))
    }

  private def callEmailVerificationService(internalId: InternalId, details: EmailDetails, eori: String)
                                          (implicit request: Request[AnyContent]): Future[Result] =
    emailVerificationService.createEmailVerificationRequest(details, routes.EmailConfirmedController.show.url, eori).flatMap {
      case Some(EmailVerificationRequestSent) => Future.successful(Redirect(routes.VerifyYourEmailController.show))
      case Some(EmailAlreadyVerified) =>
        save4LaterService.saveEmail(internalId, details.copy(timestamp = None)).map { _ =>
          Redirect(routes.EmailConfirmedController.show)
        }
      case _ => Future.successful(Redirect(routes.CheckYourEmailController.problemWithService))
    }

  }
