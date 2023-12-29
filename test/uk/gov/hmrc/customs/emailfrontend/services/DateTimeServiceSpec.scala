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

package uk.gov.hmrc.customs.emailfrontend.services

import org.joda.time.{DateTime, DateTimeZone}
import java.time.{Clock, Instant, ZoneId, ZonedDateTime}
import uk.gov.hmrc.customs.emailfrontend.utils.SpecBase

class DateTimeServiceSpec extends SpecBase {

  "nowUtc" should {
    "return current time in UTC" in new Setup {

      val dateTimeService: DateTimeService = new DateTimeService {
        override val UtcZoneId: ZoneId = ZoneId.of("UTC")

        override def nowUtc(): DateTime = new DateTime(fixedClock.instant().toEpochMilli, DateTimeZone.UTC)
      }

      val currentTime: DateTime = dateTimeService.nowUtc()

      currentTime.getYear shouldBe 2023
      currentTime.getMonthOfYear shouldBe 10
      currentTime.getDayOfMonth shouldBe 31
      currentTime.getHourOfDay shouldBe 12
    }

    "return instance of DateTime" in new Setup {
      val dateTimeServiceOb = new DateTimeService()

      dateTimeServiceOb.nowUtc().isInstanceOf[DateTime] shouldBe true
    }
  }

  "zonedDateTimeUtc" should {
    "return current time in UTC using java.time.ZonedDateTime" in new Setup {

      val dateTimeService: DateTimeService = new DateTimeService {
        override val UtcZoneId: ZoneId = ZoneId.of("UTC")

        override def zonedDateTimeUtc: ZonedDateTime = ZonedDateTime.now(fixedClock)
      }

      val currentTime: ZonedDateTime = dateTimeService.zonedDateTimeUtc

      currentTime.getYear shouldBe 2023
      currentTime.getMonthValue shouldBe 10
      currentTime.getDayOfMonth shouldBe 31
      currentTime.getHour shouldBe 12
    }

    "return correct ZoneId" in new Setup {
      val dateTimeServiceOb = new DateTimeService()

      dateTimeServiceOb.zonedDateTimeUtc.getZone shouldBe ZoneId.of("UTC")
    }
  }

  trait Setup {
    val fixedInstant: Instant = Instant.parse("2023-10-31T12:00:00Z")
    val fixedClock: Clock = Clock.fixed(fixedInstant, ZoneId.of("UTC"))
  }
}
