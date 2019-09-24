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
import org.joda.time.DateTime
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.auth.core.EnrolmentIdentifier
import uk.gov.hmrc.customs.emailfrontend.DateTimeUtil
import uk.gov.hmrc.customs.emailfrontend.controllers.actions.Actions
import uk.gov.hmrc.customs.emailfrontend.controllers.routes.{SignOutController, VerifyYourEmailController}
import uk.gov.hmrc.customs.emailfrontend.model.{EmailStatus, EoriRequest}
import uk.gov.hmrc.customs.emailfrontend.services.{CustomsDataStoreService, EmailCacheService, EmailVerificationService, UpdateVerifiedEmailService}
import uk.gov.hmrc.customs.emailfrontend.views.html.email_confirmed
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

class EmailConfirmedController @Inject()(actions: Actions, view: email_confirmed,
                                         customsDataStoreService: CustomsDataStoreService,
                                         emailCacheService: EmailCacheService,
                                         emailVerificationService: EmailVerificationService,
                                         updateVerifiedEmailService: UpdateVerifiedEmailService,
                                         mcc: MessagesControllerComponents)
                                        (implicit override val messagesApi: MessagesApi, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def show: Action[AnyContent] = (actions.authEnrolled andThen actions.isPermitted andThen actions.eori).async { implicit request =>
    emailCacheService.fetchEmail(request.user.internalId) flatMap {
      _.fold {
        Logger.warn("[EmailConfirmedController][show] - emailStatus cache none, user logged out")
        Future.successful(Redirect(SignOutController.signOut()))
      } {
        emailStatus =>
          for {
            verified <- emailVerificationService.isEmailVerified(emailStatus.email)
            redirect <- if (verified.contains(true)) updateEmail(emailStatus) else Future.successful(Redirect(VerifyYourEmailController.show()))
          } yield redirect
      }
    }
  }

  private[this] def updateEmail(emailStatus: EmailStatus)(implicit request: EoriRequest[AnyContent]): Future[Result] = {
    updateVerifiedEmailService.updateVerifiedEmail(emailStatus.email, request.eori.id).flatMap {
      case Some(_) =>
        saveTimeStamp(DateTimeUtil.dateTime)
        customsDataStoreService.storeEmail(EnrolmentIdentifier("EORINumber", request.eori.id), emailStatus.email)
        Future.successful(Ok(view()))
      case None => ??? // TODO: no scenario ready to cover that case
    }
  }

  private[this] def saveTimeStamp(timestamp: DateTime)(implicit request: EoriRequest[AnyContent]): Future[CacheMap] = {
    for {
      _ <- emailCacheService.remove(request.user.internalId)
      savedTimestamp <- emailCacheService.saveTimeStamp(request.user.internalId, timestamp)
    } yield savedTimestamp
  }
}
