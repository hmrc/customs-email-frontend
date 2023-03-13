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
import uk.gov.hmrc.customs.emailfrontend.controllers.actions.IdentifierAction
import uk.gov.hmrc.customs.emailfrontend.forms.Forms.emailForm
import uk.gov.hmrc.customs.emailfrontend.model._
import uk.gov.hmrc.customs.emailfrontend.services.{EmailVerificationService, Save4LaterService}
import uk.gov.hmrc.customs.emailfrontend.views.html._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@Singleton
class WhatIsYourEmailController @Inject()(identify: IdentifierAction,
                                          view: change_your_email,
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
      save4LaterService.routeBasedOnAmendment(request.user.internalId)(
        redirectBasedOnEmailStatus,
        Future.successful(Redirect(routes.WhatIsYourEmailController.create))
      )
    }

  private def redirectBasedOnEmailStatus(details: EmailDetails)
                                        (implicit request: Request[AnyContent]): Future[Result] =
    emailVerificationService.isEmailVerified(details.newEmail).map {
      case Some(true) => Redirect(routes.EmailConfirmedController.show)
      case Some(false) => Redirect(routes.CheckYourEmailController.show)
      case None => InternalServerError(errorHandler.problemWithService)
    }

  def create: Action[AnyContent] = identify.async { implicit request =>
    save4LaterService.routeBasedOnAmendment(request.user.internalId)(
      details =>
        (details.currentEmail, details.newEmail) match {
          case (Some(currentEmail), _) => Future.successful(Ok(view(emailForm, appConfig)))
          case (None, _) => Future.successful(Ok(whatIsYourEmailView(emailForm)))
          case _ => Future.successful(Redirect(routes.WhatIsYourEmailController.problemWithService))
        },
      subscriptionDisplay
    )
  }

  def whatIsEmailAddress: Action[AnyContent] = identify.async { implicit request =>
    save4LaterService.saveJourneyType(request.user.internalId, JourneyType(false))
    Future.successful(Ok(view(emailForm, appConfig)))
  }

  private def subscriptionDisplay()(implicit request: AuthenticatedRequest[AnyContent]) =
    subscriptionDisplayConnector.subscriptionDisplay(request.user.eori).flatMap {
      case SubscriptionDisplayResponse(Some(email), Some(_), _, _) =>
        Future.successful(Ok(view(emailForm, appConfig)))
      case SubscriptionDisplayResponse(Some(_), _, _, _) =>
        Future.successful(Redirect(routes.WhatIsYourEmailController.verify))
      case SubscriptionDisplayResponse(_, _, Some("Processed Successfully"), _) =>
        Future.successful(Redirect(routes.WhatIsYourEmailController.verify))
      case SubscriptionDisplayResponse(None, _, Some(_), Some("FAIL")) =>
        Future.successful(Redirect(routes.WhatIsYourEmailController.problemWithService))
      case SubscriptionDisplayResponse(None, _, None, None) =>
        Future.successful(Redirect(routes.WhatIsYourEmailController.verify))
      case _ => Future.successful(Redirect(routes.WhatIsYourEmailController.problemWithService))
    }.recover {
      handleNonFatalException()
    }

  def verify: Action[AnyContent] = identify.async { implicit request =>
    save4LaterService.routeBasedOnAmendment(request.user.internalId)(
      _ => Future.successful(Ok(whatIsYourEmailView(emailForm))),
      Future.successful(Ok(whatIsYourEmailView(emailForm)))
    )
  }

  def submit: Action[AnyContent] = identify.async { implicit request =>
    emailForm.bindFromRequest.fold(
      formWithErrors => {
        subscriptionDisplayConnector.subscriptionDisplay(request.user.eori).map {
          case SubscriptionDisplayResponse(Some(email), _, _, _) =>
            BadRequest(view(formWithErrors, appConfig))
          case _ => Redirect(routes.WhatIsYourEmailController.problemWithService)
        }.recover {
          handleNonFatalException()
        }
      },
      formData => {
        subscriptionDisplayConnector.subscriptionDisplay(request.user.eori).flatMap {
          case SubscriptionDisplayResponse(currentEmail@Some(_), _, _, _) => {
            save4LaterService
              .saveEmail(request.user.internalId, EmailDetails(currentEmail, formData.value.trim, None))
              .map { _ =>
                Redirect(routes.CheckYourEmailController.show)
              }
          }
          case _ =>
            Future.successful(Redirect(routes.WhatIsYourEmailController.problemWithService))
        }
      }.recover {
        handleNonFatalException()
      }
    )
  }

  def verifySubmit: Action[AnyContent] = identify.async { implicit request =>
    emailForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(whatIsYourEmailView(formWithErrors))),
      formData => {
        save4LaterService
          .saveEmail(request.user.internalId, EmailDetails(None, formData.value, None))
          .map { _ =>
            Redirect(routes.CheckYourEmailController.show)
          }
      }
    )
  }

  private def handleNonFatalException(): PartialFunction[Throwable, Result] = {
    case NonFatal(e) => {
      logger.error(s"Subscription display failed with ${e.getMessage}")
      Redirect(routes.WhatIsYourEmailController.problemWithService)
    }
  }

  def problemWithService(): Action[AnyContent] = identify.async { implicit request =>
    Future.successful(BadRequest(errorHandler.problemWithService))
  }
}
