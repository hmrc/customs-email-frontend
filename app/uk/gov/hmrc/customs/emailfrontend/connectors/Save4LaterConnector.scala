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

package uk.gov.hmrc.customs.emailfrontend.connectors

import javax.inject.{Inject, Singleton}
import play.api.Logging
import play.api.libs.json._
import play.mvc.Http.Status._
import uk.gov.hmrc.customs.emailfrontend.audit.Auditable
import uk.gov.hmrc.customs.emailfrontend.config.AppConfig
import uk.gov.hmrc.customs.emailfrontend.connectors.http.responses.{BadRequest, HttpErrorResponse, UnhandledException}
import uk.gov.hmrc.customs.emailfrontend.model.{EmailDetails, ReferrerName}
import uk.gov.hmrc.http.{HttpClient, _}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class Save4LaterConnector @Inject()(http: HttpClient, appConfig: AppConfig, audit: Auditable) extends Logging {

  val LoggerComponentId = "Save4LaterConnector"

  def getEmailDetails(id: String, key: String)
                     (implicit hc: HeaderCarrier, reads: Reads[EmailDetails]): Future[Option[EmailDetails]] = {

    val url = s"${appConfig.save4LaterUrl}/$id/$key"
    http.GET[EmailDetails](url).map { response =>
      auditCallResponse[EmailDetails](url, response)
      Some(response)
    }.recover {
      case e => logger.error(s"Unable to get Email Details :${e.getMessage}")
        None
    }
  }

  def getReferrerName(id: String, key: String)
                     (implicit hc: HeaderCarrier, reads: Reads[ReferrerName]): Future[Option[ReferrerName]] = {

    val url = s"${appConfig.save4LaterUrl}/$id/$key"
    http.GET[ReferrerName](url).map { response =>
      auditCallResponse[ReferrerName](url, response)
      Some(response)
    }.recover {
      case e => logger.error(s"Unable to get Referrer :${e.getMessage}")
        None
    }
  }

  def put[T](id: String, key: String, payload: JsValue)
            (implicit hc: HeaderCarrier): Future[Either[HttpErrorResponse, Unit]] = {
    val url = s"${appConfig.save4LaterUrl}/$id/$key"
    logger.info(s"PUT: $url")
    auditCallRequest(url, payload)
    http.PUT[JsValue, HttpResponse](url, payload).map { response =>
      auditCallResponse(url, response.status)
      response.status match {
        case NO_CONTENT | CREATED | OK => Right(())
        case _ => Left(BadRequest)
      }
    }.recover {
      case e => logger.error(s"Request failed for call to $url, exception: ${e.getMessage}")
        Left(UnhandledException)
    }
  }

  def delete[T](id: String)(implicit hc: HeaderCarrier): Future[Either[HttpErrorResponse, Unit]] = {
    val url = s"${appConfig.save4LaterUrl}/$id"
    logger.info(s"DELETE: $url")
    auditCallRequest(url, JsNull)
    http.DELETE[HttpResponse](url).map { response =>
      auditCallResponse(url, response.status)
      response.status match {
        case NO_CONTENT => Right(())
        case _ => Left(BadRequest)
      }
    }.recover {
      case e => logger.error(s"Request failed for call to $url, exception: ${e.getMessage}")
        Left(UnhandledException)
    }
  }

  private def auditCallRequest[T](url: String, request: JsValue)
                                 (implicit hc: HeaderCarrier): Future[Unit] =
    Future {
      audit.sendExtendedDataEvent(
        transactionName = "Save4laterRequest",
        path = url,
        details = request,
        eventType = "Save4later"
      )
    }

  private def auditCallResponse[T](url: String, response: T)
                                  (implicit hc: HeaderCarrier, writes: Writes[T]): Future[Unit] =
    Future {
      audit.sendExtendedDataEvent(
        transactionName = "Save4laterResponse",
        path = url,
        details = Json.toJson(response),
        eventType = "Save4later"
      )
    }
}