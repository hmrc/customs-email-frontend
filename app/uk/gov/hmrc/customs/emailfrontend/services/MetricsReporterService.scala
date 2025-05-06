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

import com.codahale.metrics.MetricRegistry
import com.google.inject.Inject
import play.api.http.Status
import uk.gov.hmrc.http.{BadRequestException, NotFoundException, UpstreamErrorResponse}

import javax.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class MetricsReporterService @Inject() (val metrics: MetricRegistry, dateTimeService: DateTimeService) {

  def withResponseTimeLogging[T](resourceName: String)(future: Future[T])(implicit ec: ExecutionContext): Future[T] = {
    val startTimeStamp = dateTimeService.timeStamp()

    future.andThen { case response =>
      val httpResponseCode = response match {
        case Success(_)                                => Status.OK
        case Failure(exception: NotFoundException)     => exception.responseCode
        case Failure(exception: BadRequestException)   => exception.responseCode
        case Failure(exception: UpstreamErrorResponse) => exception.statusCode
        case Failure(_)                                => Status.INTERNAL_SERVER_ERROR
      }

      updateResponseTimeHistogram(resourceName, httpResponseCode, startTimeStamp, dateTimeService.timeStamp())
    }
  }

  private def updateResponseTimeHistogram(
    resourceName: String,
    httpResponseCode: Int,
    startTimeStamp: Long,
    endTimeStamp: Long
  ): Unit = {
    val RESPONSE_TIMES_METRIC = "responseTimes"
    val histogramName         = s"$RESPONSE_TIMES_METRIC.$resourceName.$httpResponseCode"
    val elapsedTimeInMillis   = endTimeStamp - startTimeStamp

    metrics.histogram(histogramName).update(elapsedTimeInMillis)
  }
}
