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

package uk.gov.hmrc.customs

import java.time.{Instant, LocalDateTime, ZoneOffset, ZonedDateTime}
import play.api.libs.json._
import uk.gov.hmrc.auth.core.retrieve.{~ => Retrieve}
import uk.gov.hmrc.customs.emailfrontend.utils.Utils.{emptyString, hyphen}

import java.util.UUID

package object emailfrontend {

  object RandomUUIDGenerator {
    def generateUUIDAsString: String = UUID.randomUUID().toString.replace(hyphen, emptyString)
  }

  object DateTimeUtil {

    def dateTime: LocalDateTime = {
      ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime
    }

    private def dateTimeWritesIsoUtc: Writes[Instant] = (d: Instant) =>
      JsString(d.atZone(ZoneOffset.UTC).toString)

    private def dateTimeReadsIso: Reads[Instant] = (value: JsValue) =>
      JsSuccess(ZonedDateTime.parse(value.as[String]).toInstant)

    implicit val dateTimeReads: Reads[Instant] = dateTimeReadsIso
    implicit val dateTimeWrites: Writes[Instant] = dateTimeWritesIsoUtc

  }

  object Retrieval {
    implicit class AddRetrievals[A, B, C](r: Retrieve[A, B]) {
      def add(c: C): A Retrieve B Retrieve C =
        Retrieve(r, c)
    }
  }

  object Utils {
    def stripWhiteSpaces(str: String): String = str.trim.replaceAll("\\s", emptyString)
  }
}
