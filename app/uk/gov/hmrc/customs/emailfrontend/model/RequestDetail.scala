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

import play.api.libs.json.*
import uk.gov.hmrc.customs.emailfrontend.utils.Utils

import java.time.LocalDateTime

case class RequestDetail(
  IDType: String,
  IDNumber: String,
  emailAddress: String,
  emailVerificationTimestamp: LocalDateTime
)

object RequestDetail {

  implicit val localDateTimeFormat: Format[LocalDateTime] = new Format[LocalDateTime] {

    override def writes(o: LocalDateTime): JsValue = Utils.writesLocalDateTime(o)

    override def reads(json: JsValue): JsResult[LocalDateTime] = Utils.readsLocalDateTime(json)
  }

  implicit val formats: OFormat[RequestDetail] = Json.format[RequestDetail]
}
