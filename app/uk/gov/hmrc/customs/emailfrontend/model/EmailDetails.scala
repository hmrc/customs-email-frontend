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

package uk.gov.hmrc.customs.emailfrontend.model

import play.api.libs.json.{JsValue, Json}

import java.time.{LocalDateTime, ZoneOffset}

case class EmailDetails(currentEmail: Option[String], newEmail: String, timestamp: Option[LocalDateTime]) {

  private val twoHours = 2

  lazy val amendmentInProgress = timestamp match {
    case Some(date) => !date.isBefore(LocalDateTime.now.atOffset(ZoneOffset.UTC).minusHours(twoHours).toLocalDateTime)
    case None => false
  }
}

object EmailDetails {

  import uk.gov.hmrc.customs.emailfrontend.DateTimeUtil._

  implicit val jsonFormat = Json.format[EmailDetails]

  implicit def toJsonFormat(emailDetails: EmailDetails): JsValue = Json.toJson(emailDetails)

}

case class JourneyType(isVerify: Boolean)

object JourneyType {
  implicit val jsonFormat = Json.format[JourneyType]

  implicit def toJsonFormat(journeyType: JourneyType): JsValue = Json.toJson(journeyType)
}
