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

package uk.gov.hmrc.customs.emailfrontend.model

import java.time.Clock
import java.util.UUID

import org.joda.time.{DateTime, DateTimeZone}
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json.{JsError, JsResult, JsString, JsSuccess, JsValue, Json, Reads, Writes}

trait CommonHeader {

  private def dateTimeWritesIsoUtc: Writes[DateTime] = new Writes[DateTime] {
    def writes(d: org.joda.time.DateTime): JsValue = {
      JsString(d.toString(ISODateTimeFormat.dateTimeNoMillis().withZoneUTC()))
    }
  }

  private def dateTimeReadsIso: Reads[DateTime] = new Reads[DateTime] {
    def reads(value: JsValue): JsResult[DateTime] = {
      try {
        JsSuccess(ISODateTimeFormat.dateTimeParser.parseDateTime(value.as[String]))
      }
      catch {
        case e: Exception => JsError(s"Could not parse '${value.toString()}' as an ISO date. Reason: $e")
      }
    }
  }

  implicit val dateTimeReads = dateTimeReadsIso
  implicit val dateTimeWrites = dateTimeWritesIsoUtc
}

case class RequestCommon(regime: String,
                         receiptDate: DateTime,
                         acknowledgementReference: String)


case class RequestDetail(IDType: String,
                         IDNumber: String,
                         emailAddress: String,
                         emailVerificationTimestamp: String)


object RandomUUIDGenerator {
  def generateUUIDAsString: String = UUID.randomUUID().toString.replace("-", "")
}

object MDGDateFormat {
  def dateFormat: DateTime = {
    new DateTime(Clock.systemUTC().instant.toEpochMilli, DateTimeZone.UTC)
  }
}

object RequestCommon extends CommonHeader {

  import MDGDateFormat._

  def apply(): RequestCommon = RequestCommon("CDS",
    receiptDate = dateFormat,
    acknowledgementReference = RandomUUIDGenerator.generateUUIDAsString
  )


  implicit val formats = Json.format[RequestCommon]

}

object RequestDetail {
  implicit val formats = Json.format[RequestDetail]
}

case class UpdateVerifiedEmailRequest(requestCommon: RequestCommon, requestDetail: RequestDetail)

object UpdateVerifiedEmailRequest {
  implicit val formats = Json.format[UpdateVerifiedEmailRequest]
}

case class VerifiedEmailRequest(updateVerifiedEmailRequest: UpdateVerifiedEmailRequest)


object VerifiedEmailRequest {
  implicit val formats = Json.format[VerifiedEmailRequest]
}

case class MessagingServiceParam(paramName: String, paramValue: String)

object MessagingServiceParam {
  implicit val formats = Json.format[MessagingServiceParam]

  val positionParamName = "POSITION"
  val Fail = "FAIL"
  val formBundleIdParamName = "ETMPFORMBUNDLENUMBER"
}

case class ResponseCommon(status: String, statusText: Option[String], processingDate: String, returnParameters: List[MessagingServiceParam]) {
  require(returnParameters.nonEmpty)
}

object ResponseCommon {
  implicit val format = Json.format[ResponseCommon]
}

case class UpdateVerifiedEmailResponse(responseCommon: ResponseCommon)

object UpdateVerifiedEmailResponse {
  implicit val format = Json.format[UpdateVerifiedEmailResponse]
}

case class VerifiedEmailResponse(updateVerifiedEmailResponse: UpdateVerifiedEmailResponse)

object VerifiedEmailResponse {
  implicit val format = Json.format[VerifiedEmailResponse]
}