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

import java.time.LocalDateTime
import play.api.libs.json.{Format, JsResult, JsValue, Json, OFormat}
import uk.gov.hmrc.customs.emailfrontend.RandomUUIDGenerator
import uk.gov.hmrc.customs.emailfrontend.utils.Utils

case class RequestCommon(regime: String, receiptDate: LocalDateTime, acknowledgementReference: String)

object RequestCommon {

  import uk.gov.hmrc.customs.emailfrontend.DateTimeUtil._

  implicit val localDateTimeFormat: Format[LocalDateTime] = new Format[LocalDateTime] {

    override def writes(o: LocalDateTime): JsValue = Utils.writesLocalDateTime(o)

    override def reads(json: JsValue): JsResult[LocalDateTime] = Utils.readsLocalDateTime(json)
  }

  def apply(): RequestCommon =
    RequestCommon("CDS", receiptDate = dateTime,
      acknowledgementReference = RandomUUIDGenerator.generateUUIDAsString)

  implicit val formats: OFormat[RequestCommon] = Json.format[RequestCommon]
}
