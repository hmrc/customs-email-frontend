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

import org.joda.time.DateTime
import play.api.Logging
import play.api.http.Status.NO_CONTENT
import uk.gov.hmrc.auth.core.EnrolmentIdentifier
import uk.gov.hmrc.customs.emailfrontend.connectors.CustomsDataStoreConnector
import uk.gov.hmrc.customs.emailfrontend.model.Eori
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CustomsDataStoreService @Inject()(customsDataStoreConnector: CustomsDataStoreConnector)
                                       (implicit ec: ExecutionContext) extends Logging {

  def storeEmail(enrolmentId: EnrolmentIdentifier, email: String, timestamp: DateTime)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    customsDataStoreConnector.storeEmailAddress(Eori(enrolmentId), email, timestamp).map { response =>
      response.status match {
        case NO_CONTENT =>
          logger.debug("CustomsDataStore: data store request is successful")
          response
        case _ =>
          logger.warn(s"CustomsDataStore: data store request is failed with status ${response.status}")
          response
      }
    }
}
