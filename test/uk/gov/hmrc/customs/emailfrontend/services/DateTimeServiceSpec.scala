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

import uk.gov.hmrc.customs.emailfrontend.utils.SpecBase
import uk.gov.hmrc.customs.emailfrontend.utils.TestData.testUtcTimestamp

import java.time.*

class DateTimeServiceSpec extends SpecBase {

  "nowUtc" should {
    "return current time in UTC" in new Setup {

      val dateTimeService: DateTimeService = new DateTimeService {
        override val UtcZoneId: ZoneId = ZoneId.of("UTC")

        override def nowUtc(): LocalDateTime = ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime
      }

      val currentTime: LocalDateTime = dateTimeService.nowUtc()

      currentTime.getYear       should be >= 2023
      currentTime.getMonthValue should (be >= 1 and be <= 12)
      currentTime.getDayOfMonth should (be >= 1 and be <= 31)
      currentTime.getHour       should (be >= 0 and be <= 24)

    }

    "return instance of DateTime" in new Setup {
      val dateTimeServiceOb = new DateTimeService()
      dateTimeServiceOb.nowUtc().isInstanceOf[LocalDateTime] shouldBe true
    }
  }

  "zonedDateTimeUtc" should {
    "return current time in UTC using java.time.ZonedDateTime" in new Setup {

      val dateTimeService: DateTimeService = new DateTimeService {
        override val UtcZoneId: ZoneId = ZoneId.of("UTC")

        override def zonedDateTimeUtc: ZonedDateTime = ZonedDateTime.now(fixedClock)
      }

      val currentTime: ZonedDateTime = dateTimeService.zonedDateTimeUtc

      currentTime.getYear       shouldBe 2021
      currentTime.getMonthValue shouldBe 1
      currentTime.getDayOfMonth shouldBe 1
      currentTime.getHour       shouldBe 11
    }

    "return correct ZoneId" in new Setup {
      val dateTimeServiceOb = new DateTimeService()
      dateTimeServiceOb.zonedDateTimeUtc.getZone shouldBe ZoneId.of("UTC")
    }
  }

  "timeStamp" should {
    "return correct Long value of current timestamp" in {
      val dateTimeServiceOb = new DateTimeService()

      dateTimeServiceOb.timeStamp() shouldBe a[Long]
    }
  }

  trait Setup {
    val fixedInstant: Instant = Instant.parse(testUtcTimestamp)
    val fixedClock: Clock     = Clock.fixed(fixedInstant, ZoneId.of("UTC"))
  }
}
