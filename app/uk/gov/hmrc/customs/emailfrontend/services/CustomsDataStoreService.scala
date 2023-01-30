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

package uk.gov.hmrc.customs.emailfrontend.services

import javax.inject.{Inject, Singleton}
import org.joda.time.DateTime
import play.api.Logging
import uk.gov.hmrc.auth.core.EnrolmentIdentifier
import uk.gov.hmrc.customs.emailfrontend.connectors.CustomsDataStoreConnector
import uk.gov.hmrc.customs.emailfrontend.connectors.http.responses.HttpErrorResponse
import uk.gov.hmrc.customs.emailfrontend.model.Eori
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import scala.concurrent.Future

@Singleton
class CustomsDataStoreService @Inject()(customsDataStoreConnector: CustomsDataStoreConnector) extends Logging {
  def storeEmail(enrolmentId: EnrolmentIdentifier, email: String, timestamp: DateTime)
                (implicit hc: HeaderCarrier): Future[Either[HttpErrorResponse, HttpResponse]] =
    customsDataStoreConnector.storeEmailAddress(Eori(enrolmentId), email, timestamp)
}
