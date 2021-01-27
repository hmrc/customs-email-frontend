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

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.auth.core.EnrolmentIdentifier
import uk.gov.hmrc.customs.emailfrontend.DateTimeUtil
import uk.gov.hmrc.customs.emailfrontend.config.ErrorHandler
import uk.gov.hmrc.customs.emailfrontend.controllers.actions.Actions
import uk.gov.hmrc.customs.emailfrontend.controllers.routes.{SignOutController, VerifyYourEmailController}
import uk.gov.hmrc.customs.emailfrontend.model.{EmailDetails, EoriRequest}
import uk.gov.hmrc.customs.emailfrontend.services.{CustomsDataStoreService, EmailVerificationService, Save4LaterService, UpdateVerifiedEmailService}
import uk.gov.hmrc.customs.emailfrontend.views.html.email_confirmed
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

class EmailConfirmedController @Inject()(
  actions: Actions,
  view: email_confirmed,
  customsDataStoreService: CustomsDataStoreService,
  save4LaterService: Save4LaterService,
  emailVerificationService: EmailVerificationService,
  updateVerifiedEmailService: UpdateVerifiedEmailService,
  mcc: MessagesControllerComponents,
  errorHandler: ErrorHandler
)(implicit override val messagesApi: MessagesApi, ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def show: Action[AnyContent] =
    (actions.auth
      andThen actions.isPermitted
      andThen actions.isEnrolled).async { implicit request =>
      save4LaterService.routeBasedOnAmendment(request.user.internalId)(
        redirectBasedOnEmailStatus,
        Future.successful(Redirect(SignOutController.signOut()))
      )
    }

  private def redirectBasedOnEmailStatus(
    details: EmailDetails
  )(implicit request: EoriRequest[AnyContent]): Future[Result] =
    for {
      verified <- emailVerificationService.isEmailVerified(details.newEmail)
      redirect <- if (verified.contains(true)) updateEmail(details)
      else Future.successful(Redirect(VerifyYourEmailController.show()))
    } yield redirect

  private[this] def updateEmail(details: EmailDetails)(implicit request: EoriRequest[AnyContent]): Future[Result] =
    updateVerifiedEmailService
      .updateVerifiedEmail(details.currentEmail, details.newEmail, request.eori.id)
      .flatMap {
        case Some(true) =>
          save4LaterService.saveEmail(request.user.internalId, details.copy(timestamp = Some(DateTimeUtil.dateTime)))
          customsDataStoreService.storeEmail(EnrolmentIdentifier("EORINumber", request.eori.id), details.newEmail)
          save4LaterService.fetchReferrer(request.user.internalId).map { referrer =>
            Ok(view(details.newEmail, details.currentEmail, referrer.map(_.name), referrer.map(_.continueUrl)))
          }
        case _ => Future.successful(Redirect(routes.EmailConfirmedController.problemWithService()))
      }

  def problemWithService(): Action[AnyContent] = actions.auth.async { implicit request =>
    Future.successful(BadRequest(errorHandler.problemWithService()))
  }
}
