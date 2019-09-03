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

package uk.gov.hmrc.customs.emailfrontend.services

import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.http.Status.OK
import uk.gov.hmrc.customs.emailfrontend.connectors.CustomsDataStoreConnector
import uk.gov.hmrc.customs.emailfrontend.domain.DataStoreRequest
import uk.gov.hmrc.customs.emailfrontend.model.Eori
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CustomsDataStoreService @Inject()(customsDataStoreConnector: CustomsDataStoreConnector)(implicit ec: ExecutionContext) {

  def storeEmail(eori: Eori, email: String)(implicit hc: HeaderCarrier): Future[Either[String, Int]] = {
    customsDataStoreConnector.storeEmailAddress(DataStoreRequest(eori.id, email)) map { response =>
      response.status match {
        case OK =>
          Logger.info("CustomsDataStore: data store request is successful")
          Right(OK)
        case failStatus =>
          Logger.warn(s"CustomsDataStore: data store request is failed with status ${response.status}")
          Left(s"Request failed with status $failStatus")
      }
    }
  }
}
