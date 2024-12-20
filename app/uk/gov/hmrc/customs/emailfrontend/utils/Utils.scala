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

package uk.gov.hmrc.customs.emailfrontend.utils

import play.api.libs.json.{JsError, JsResult, JsSuccess, JsValue, Json}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Utils {
  val emptyString                      = ""
  val hyphen                           = "-"
  val singleSpace                      = " "
  val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")

  def writesLocalDateTime(o: LocalDateTime): JsValue = Json.toJson(dateFormatter.format(o))

  def readsLocalDateTime(json: JsValue): JsResult[LocalDateTime] =
    json.validate[String].flatMap { str =>
      try
        JsSuccess(LocalDateTime.parse(str, dateFormatter))
      catch {
        case _: Throwable => JsError("Invalid LocalDateTime format")
      }
    }

}
