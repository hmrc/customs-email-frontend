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

package uk.gov.hmrc.customs.emailfrontend.model

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json.{Format, JsString, JsValue, Json, Reads, Writes}

case class UpdateEmail(eori: Eori, address: String, timestamp: DateTime)

object UpdateEmail {

  val jodaDateReads = Reads[DateTime](js =>
    js.validate[String].map[DateTime](dtString =>
      DateTime.parse(dtString)
    )
  )
  val jodaDateWrites: Writes[DateTime] = new Writes[DateTime] {
    def writes(d: DateTime): JsValue = JsString(d.toString(ISODateTimeFormat.dateTimeNoMillis().withZoneUTC()))
  }

  val eoriWrites: Writes[Eori] = new Writes[Eori] {
    def writes(eori: Eori): JsValue = JsString(eori.id)
  }

  val eoriReads = Reads[Eori] { js =>
    js.validate[String].map[Eori](eori =>
      Eori(eori)
    )
  }


  implicit val formatEori = Format(eoriReads, eoriWrites)
  implicit val dateTimeJF = Format(jodaDateReads, jodaDateWrites)
  implicit val format = Json.format[UpdateEmail]
}

