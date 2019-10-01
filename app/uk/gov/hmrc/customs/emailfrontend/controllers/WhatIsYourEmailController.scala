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
import uk.gov.hmrc.customs.emailfrontend.connectors.SubscriptionDisplayConnector
import uk.gov.hmrc.customs.emailfrontend.controllers.actions.Actions
import uk.gov.hmrc.customs.emailfrontend.controllers.routes.{EmailConfirmedController, WhatIsYourEmailController,CheckYourEmailController}
import uk.gov.hmrc.customs.emailfrontend.forms.Forms.emailForm
import uk.gov.hmrc.customs.emailfrontend.model._
import uk.gov.hmrc.customs.emailfrontend.services.{EmailCacheService, EmailVerificationService}
import uk.gov.hmrc.customs.emailfrontend.views.html.{change_your_email, what_is_your_email}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WhatIsYourEmailController @Inject()(actions: Actions, view: change_your_email,
                                          whatIsYourEmailView: what_is_your_email,
                                          emailCacheService: EmailCacheService,
                                          mcc: MessagesControllerComponents,
                                          subscriptionDisplayConnector: SubscriptionDisplayConnector,
                                          emailVerificationService: EmailVerificationService)
                                         (implicit override val messagesApi: MessagesApi, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport with DetermineRouteController {

  def show: Action[AnyContent] = (actions.authEnrolled andThen actions.isPermitted).async { implicit request =>
    for {
      status <- emailCacheService.emailAmendmentStatus(request.user.internalId)
      result <- redirectBasedOnAmendmentStatus(status)(redirectBasedOnEmailStatus)
    } yield result
  }

  private def redirectBasedOnEmailStatus(implicit request: AuthenticatedRequest[AnyContent]): Future[Result] =
    emailCacheService.fetchEmail(request.user.internalId) flatMap {
      _.fold {
        Logger.warn("[WhatIsYourEmailController][show] - email not found")
        Future.successful(Redirect(WhatIsYourEmailController.create()))
      } {
        emailStatus =>
          emailVerificationService.isEmailVerified(emailStatus.email).map {
            case Some(true) => Redirect(EmailConfirmedController.show())
            case Some(false) => Redirect(CheckYourEmailController.show())
            case None => ??? //ToDo redirect to retry page  Email Service is down or any other errors
          }
      }
    }

  def create: Action[AnyContent] = (actions.authEnrolled andThen actions.eori).async { implicit request =>

    for {
      status <- emailCacheService.emailAmendmentStatus(request.user.internalId)
      result <- redirectBasedOnAmendmentStatus(status)(redirectBaseOnEmailStatusAndSubscriptionDisplay)
    } yield result

  }


  private def redirectBaseOnEmailStatusAndSubscriptionDisplay(implicit request: EoriRequest[AnyContent]): Future[Result] = {
    subscriptionDisplayConnector.subscriptionDisplay(Eori(request.eori.id)).flatMap {
      case SubscriptionDisplayResponse(Some(email)) =>
        emailVerificationService.isEmailVerified(email).map {
          case Some(true) => Ok(view(emailForm, email)) //TODO is this correct
          case Some(false) => Redirect(WhatIsYourEmailController.verify())
          case None => ??? //ToDo redirect to retry page
        }
    }
  }

  def verify: Action[AnyContent] = actions.authEnrolled.async { implicit request =>
    for {
      status <- emailCacheService.emailAmendmentStatus(request.user.internalId)
      result <- redirectBasedOnAmendmentStatus(status)(Future.successful(Ok(whatIsYourEmailView(emailForm))))
    } yield result
  }

  def submit: Action[AnyContent] = (actions.authEnrolled andThen actions.eori).async { implicit request =>
    emailForm.bindFromRequest.fold(
      formWithErrors => {
        subscriptionDisplayConnector.subscriptionDisplay(Eori(request.eori.id)).map {
          case SubscriptionDisplayResponse(Some(email)) => BadRequest(view(formWithErrors, email))
        }
      },
      formData => {
        emailCacheService.saveEmail(request.user.internalId, EmailStatus(formData.value)).map {
          _ => Redirect(routes.CheckYourEmailController.show())
        }
      }
    )
  }

  def verifySubmit: Action[AnyContent] = (actions.authEnrolled andThen actions.eori).async { implicit request =>
    emailForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(whatIsYourEmailView(formWithErrors))),
      formData => {
        emailCacheService.saveEmail(request.user.internalId, EmailStatus(formData.value)).map {
          _ => Redirect(routes.CheckYourEmailController.show())
        }
      }
    )
  }
}
