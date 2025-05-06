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

import com.codahale.metrics.{Histogram, MetricRegistry}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.{await, running}
import play.api.{Application, inject}
import uk.gov.hmrc.customs.emailfrontend.utils.SpecBase
import uk.gov.hmrc.http.*
import uk.gov.hmrc.play.bootstrap.metrics.Metrics

import java.time.OffsetDateTime
import scala.concurrent.Future
import play.api.test.Helpers.defaultAwaitTimeout

class MetricsReporterServiceSpec extends SpecBase {

  "MetricsReporterService" should {

    "withResponseTimeLogging" should {
      "log successful call metrics" in new Setup {
        running(app) {
          await {
            service.withResponseTimeLogging("foo") {
              Future.successful("OK")
            }
          }

          verify(mockRegistry).histogram("responseTimes.foo.200")
          verify(mockHistogram).update(elapsedTimeInMillis)
        }
      }

      "log default error during call metrics" in new Setup {
        running(app) {
          assertThrows[InternalServerException] {
            await {
              service.withResponseTimeLogging("bar") {
                Future.failed(new InternalServerException("boom"))
              }
            }
          }

          verify(mockRegistry).histogram("responseTimes.bar.500")
          verify(mockHistogram).update(elapsedTimeInMillis)
        }
      }

      "log not found call metrics" in new Setup {
        running(app) {
          assertThrows[NotFoundException] {
            await {
              service.withResponseTimeLogging("bar") {
                Future.failed(new NotFoundException("boom"))
              }
            }
          }

          verify(mockRegistry).histogram("responseTimes.bar.404")
          verify(mockHistogram).update(elapsedTimeInMillis)
        }
      }

      "log bad request error call metrics" in new Setup {
        running(app) {
          assertThrows[BadRequestException] {
            await {
              service.withResponseTimeLogging("bar") {
                Future.failed(new BadRequestException("boom"))
              }
            }
          }

          verify(mockRegistry).histogram("responseTimes.bar.400")
          verify(mockHistogram).update(elapsedTimeInMillis)
        }
      }

      "log 5xx error call metrics" in new Setup {
        running(app) {
          assertThrows[UpstreamErrorResponse] {
            await {
              service.withResponseTimeLogging("bar") {
                Future.failed(UpstreamErrorResponse("boom", Status.SERVICE_UNAVAILABLE, Status.NOT_IMPLEMENTED))
              }
            }
          }

          verify(mockRegistry).histogram("responseTimes.bar.503")
          verify(mockHistogram).update(elapsedTimeInMillis)
        }
      }

      "log 4xx error call metrics" in new Setup {
        running(app) {
          assertThrows[UpstreamErrorResponse] {
            await {
              service.withResponseTimeLogging("bar") {
                Future.failed(UpstreamErrorResponse("boom", Status.FORBIDDEN, Status.NOT_IMPLEMENTED))
              }
            }
          }

          verify(mockRegistry).histogram("responseTimes.bar.403")
          verify(mockHistogram).update(elapsedTimeInMillis)
        }
      }

    }
  }

  trait Setup {
    val startTimestamp: Long = OffsetDateTime.parse("2018-11-09T17:15:30+01:00").toInstant.toEpochMilli
    val endTimestamp: Long   = OffsetDateTime.parse("2018-11-09T17:15:35+01:00").toInstant.toEpochMilli
    val elapsedTimeInMillis  = 5000L

    val mockDateTimeService: DateTimeService = mock[DateTimeService]
    val mockHistogram: Histogram             = mock[Histogram]
    val mockRegistry: MetricRegistry         = mock[MetricRegistry]
    val mockMetrics: Metrics                 = mock[Metrics]

    when(mockDateTimeService.timeStamp())
      .thenReturn(startTimestamp, endTimestamp)

    when(mockRegistry.histogram(any)).thenReturn(mockHistogram)
    when(mockMetrics.defaultRegistry).thenReturn(mockRegistry)

    val app: Application = GuiceApplicationBuilder()
      .overrides(
        inject.bind[DateTimeService].toInstance(mockDateTimeService),
        inject.bind[Histogram].toInstance(mockHistogram),
        inject.bind[Metrics].toInstance(mockMetrics)
      )
      .configure(
        "microservice.metrics.enabled" -> false,
        "metrics.enabled"              -> false,
        "auditing.enabled"             -> false
      )
      .build()

    val service: MetricsReporterService = app.injector.instanceOf[MetricsReporterService]
  }
}
