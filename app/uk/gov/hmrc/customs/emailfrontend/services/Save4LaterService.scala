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

package uk.gov.hmrc.customs.emailfrontend.services

import play.api.Logging
import javax.inject.{Inject, Singleton}
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import uk.gov.hmrc.customs.emailfrontend.connectors.Save4LaterConnector
import uk.gov.hmrc.customs.emailfrontend.controllers.routes.AmendmentInProgressController
import uk.gov.hmrc.customs.emailfrontend.model.{EmailDetails, InternalId, ReferrerName}
import uk.gov.hmrc.http.HeaderCarrier
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class Save4LaterService @Inject()(save4LaterConnector: Save4LaterConnector) extends Logging {
  private val referrerKey = "referrer"
  private val emailKey = "email"

  def saveEmail(internalId: InternalId, emailDetails: EmailDetails)(implicit hc: HeaderCarrier): Future[Unit] =
    save4LaterConnector.put[EmailDetails](internalId.id, emailKey, emailDetails)

  def fetchEmail(
    internalId: InternalId
  )(implicit hc: HeaderCarrier): Future[Option[EmailDetails]] = {
    logger.info("retrieving email address and timestamp from save 4 later")
    save4LaterConnector.getEmail(internalId.id, emailKey)
  }

  def saveReferrer(
    internalId: InternalId,
    referrerName: ReferrerName
  )(implicit hc: HeaderCarrier): Future[Unit] = {
    logger.info("saving referrer name and referrer url  from mongo")
    save4LaterConnector.put[ReferrerName](internalId.id, referrerKey, referrerName)
  }

  def fetchReferrer(
    internalId: InternalId
  )(implicit hc: HeaderCarrier): Future[Option[ReferrerName]] = {
    logger.info("retrieving referrer name and referrer  from mongo")
    save4LaterConnector.getReferrer(internalId.id, referrerKey)
  }

  def remove(internalId: InternalId)(implicit hc: HeaderCarrier): Future[Unit] = {
    logger.info("removing cached data from  mongo")
    save4LaterConnector.delete(internalId.id)
  }
}

object Save4LaterService extends Logging {

  implicit class EmailCacheServiceHelper(save4LaterService: Save4LaterService) {

    def routeBasedOnAmendment(internalId: InternalId)(
      redirectBasedOnEmailStatus: EmailDetails => Future[Result],
      noEmail: Future[Result]
    )(implicit hc: HeaderCarrier, executionContext: ExecutionContext) =
      save4LaterService.fetchEmail(internalId).flatMap {
        case Some(data) if data.amendmentInProgress => {
          logger.info("email amendment in-progress")
          Future.successful(Redirect(AmendmentInProgressController.show()))
        }
        case Some(EmailDetails(_, _, Some(_))) => {
          logger.info("email amendment completed")
          save4LaterService.remove(internalId).flatMap(_ => noEmail)
        }
        case Some(details @ EmailDetails(_, _, None)) => {
          logger.info("email amendment not determined")
          redirectBasedOnEmailStatus(details)
        }
        case _ =>
          logger.info("email details not found in the cache")
          noEmail
      }
  }

}
