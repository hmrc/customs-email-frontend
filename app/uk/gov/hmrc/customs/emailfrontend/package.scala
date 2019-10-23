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

package uk.gov.hmrc.customs

import java.time.Clock
import java.util.UUID

import org.joda.time.{DateTime, DateTimeZone}
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json._
import uk.gov.hmrc.auth.core.retrieve.{~ => Retrieve}

package object emailfrontend {

  object RandomUUIDGenerator {
    def generateUUIDAsString: String = UUID.randomUUID().toString.replace("-", "")
  }

  object DateTimeUtil {

    def dateTime: DateTime = {
      new DateTime(Clock.systemUTC().instant.toEpochMilli, DateTimeZone.UTC)
    }

    private def dateTimeWritesIsoUtc: Writes[DateTime] = new Writes[DateTime] {
      def writes(d: org.joda.time.DateTime): JsValue = {
        JsString(d.toString(ISODateTimeFormat.dateTimeNoMillis().withZoneUTC()))
      }
    }

    private def dateTimeReadsIso: Reads[DateTime] = new Reads[DateTime] {
      def reads(value: JsValue): JsResult[DateTime] = {
          JsSuccess(ISODateTimeFormat.dateTimeParser.parseDateTime(value.as[String]))
      }
    }

    def toString(dateTime: DateTime): String ={ dateTime.toString(ISODateTimeFormat.dateTimeNoMillis().withZoneUTC())}

    implicit val dateTimeReads: Reads[DateTime] = dateTimeReadsIso
    implicit val dateTimeWrites: Writes[DateTime] = dateTimeWritesIsoUtc

  }

  object Retrieval{
    implicit class AddRetrievals[A,B,C](r:Retrieve[A,B]){
      def add(c:C): A Retrieve B Retrieve C = {
        Retrieve(r,c)
      }
    }
  }


}
