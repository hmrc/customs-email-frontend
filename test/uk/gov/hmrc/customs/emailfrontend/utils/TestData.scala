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

import uk.gov.hmrc.customs.emailfrontend.model.{EmailAddress, InternalId, LoggedInUser}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object TestData {
  val dateFormatter01: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
  val dateFormatter02: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")

  val testEori                       = "test_eori"
  val testEmail                      = "test@example.com"
  val testEmail2                     = "test_new_mail@test.com"
  val testEmailAddress: EmailAddress = EmailAddress(testEmail)

  val testLocalTimestamp     = "2016-3-17T9:30:47.114"
  val testUtcTimestamp       = "2021-01-01T11:11:11Z"
  val testUtcTimestampMillis = "2021-01-01T11:11:11.111Z"

  val loggedInUser: LoggedInUser = LoggedInUser(InternalId("some_id"), None, None, testEori)

  val YEAR    = 2024
  val MONTH   = 12
  val DAY     = 15
  val HOUR    = 14
  val MINUTES = 30
  val SECONDS = 28

  val TEST_LOCAL_DATE_TIME: LocalDateTime = LocalDateTime.of(YEAR, MONTH, DAY, HOUR, MINUTES, SECONDS)

  val TEST_REF = "12345acnd677"
}
