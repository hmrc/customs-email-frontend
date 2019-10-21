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
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.customs.emailfrontend.config.ErrorHandler
import uk.gov.hmrc.customs.emailfrontend.connectors.SubscriptionDisplayConnector
import uk.gov.hmrc.customs.emailfrontend.controllers.actions.Actions
import uk.gov.hmrc.customs.emailfrontend.controllers.routes.{CheckYourEmailController, EmailConfirmedController, WhatIsYourEmailController}
import uk.gov.hmrc.customs.emailfrontend.forms.Forms.emailForm
import uk.gov.hmrc.customs.emailfrontend.model._
import uk.gov.hmrc.customs.emailfrontend.services.{EmailCacheService, EmailVerificationService}
import uk.gov.hmrc.customs.emailfrontend.views.html._
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@Singleton
class WhatIsYourEmailController @Inject()(actions: Actions, view: change_your_email,
                                          whatIsYourEmailView: what_is_your_email,
                                          emailCacheService: EmailCacheService,
                                          mcc: MessagesControllerComponents,
                                          subscriptionDisplayConnector: SubscriptionDisplayConnector,
                                          emailVerificationService: EmailVerificationService,
                                          errorHandler: ErrorHandler)
                                         (implicit override val messagesApi: MessagesApi, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def show: Action[AnyContent] = (actions.auth
    andThen actions.isPermitted
    andThen actions.isEnrolled).async { implicit request =>
    emailCacheService.routeBasedOnAmendment(request.user.internalId)(redirectBasedOnEmailStatus,
      Future.successful(Redirect(WhatIsYourEmailController.create())))
  }

  private def redirectBasedOnEmailStatus(email: String)(implicit request: AuthenticatedRequest[AnyContent]): Future[Result] = {
    emailVerificationService.isEmailVerified(email).map {
      case Some(true) => Redirect(EmailConfirmedController.show())
      case Some(false) => Redirect(CheckYourEmailController.show())
      case None => ??? //ToDo redirect to retry page  Email Service is down or any other errors
    }
  }

  def create: Action[AnyContent] = (actions.auth
    andThen actions.isEnrolled
    andThen actions.eori).async { implicit request =>
    emailCacheService.routeBasedOnAmendment(request.user.internalId)(email => Future.successful(Ok(view(emailForm, email))), subscriptionDisplay)
  }

  private def subscriptionDisplay()(implicit request: EoriRequest[AnyContent]) = {
    subscriptionDisplayConnector.subscriptionDisplay(request.eori).flatMap {
      case SubscriptionDisplayResponse(Some(email), _) =>
        emailVerificationService.isEmailVerified(email).map {
          case Some(true) => Ok(view(emailForm, email))
          case Some(false) => Redirect(WhatIsYourEmailController.verify())
          case None => ??? //ToDo redirect to retry page
        }
      case SubscriptionDisplayResponse(None, Some(_)) => Future.successful(Redirect(routes.WhatIsYourEmailController.problemWithService()))
      //case SubscriptionDisplayResponse(_, _) => ??? //ToDo
    } recover {
      handleNonFatalException()
    }
  }

  def verify: Action[AnyContent] = (actions.auth
    andThen actions.isEnrolled
    andThen actions.eori).async { implicit request =>
    emailCacheService.routeBasedOnAmendment(request.user.internalId)(_ => Future.successful(Ok(whatIsYourEmailView(emailForm))),
      Future.successful(Ok(whatIsYourEmailView(emailForm))))
  }

  def submit: Action[AnyContent] = (actions.auth
    andThen actions.isEnrolled
    andThen actions.eori).async { implicit request =>
    emailForm.bindFromRequest.fold(
      formWithErrors => {
        subscriptionDisplayConnector.subscriptionDisplay(request.eori).map {
          case SubscriptionDisplayResponse(Some(email), _) => BadRequest(view(formWithErrors, email))
          case SubscriptionDisplayResponse(None, Some(_)) => Redirect(routes.WhatIsYourEmailController.problemWithService())
          //case SubscriptionDisplayResponse(_, _) => ??? //ToDo
        } recover {
          handleNonFatalException()
        }
      },
      formData => {
        emailCacheService.save(request.user.internalId, EmailDetails(formData.value, None)).map {
          _ => Redirect(routes.CheckYourEmailController.show())
        }
      }
    )
  }

  def verifySubmit: Action[AnyContent] = (actions.auth
    andThen actions.isEnrolled
    andThen actions.eori).async { implicit request =>
    emailForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(whatIsYourEmailView(formWithErrors))),
      formData => {
        emailCacheService.save(request.user.internalId, EmailDetails(formData.value, None)).map {
          _ => Redirect(routes.CheckYourEmailController.show())
        }
      }
    )
  }

  private def handleNonFatalException()(implicit request: EoriRequest[AnyContent]): PartialFunction[Throwable, Result] = {
    case NonFatal(e) => {
      Logger.error(s"Subscription display failed with ${e.getMessage}")
      Redirect(routes.WhatIsYourEmailController.problemWithService())
    }
  }

  def problemWithService(): Action[AnyContent] = actions.auth.async { implicit request =>
    Future.successful(BadRequest(errorHandler.problemWithService()))
  }
}
