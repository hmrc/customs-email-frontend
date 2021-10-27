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

import play.api.Logging
import play.api.libs.json.{JsValue, Reads}

case class SubscriptionDisplayResponse(email: Option[String],
                                       emailVerificationTimestamp: Option[String],
                                       statusText: Option[String],
                                       paramValue: Option[String]) extends Logging

object SubscriptionDisplayResponse {
  implicit val etmpReads: Reads[SubscriptionDisplayResponse] =
    (json: JsValue) => for {
      email <- (json \ "subscriptionDisplayResponse" \ "responseDetail" \ "contactInformation" \ "emailAddress")
        .validateOpt[String]
      emailVerificationTimestamp <- (json \ "subscriptionDisplayResponse" \ "responseDetail" \ "contactInformation" \ "emailVerificationTimestamp")
        .validateOpt[String]
      statusText <- (json \ "subscriptionDisplayResponse" \ "responseCommon" \ "statusText")
        .validateOpt[String]
      paramValue <- (json \ "subscriptionDisplayResponse" \ "responseCommon" \ "returnParameters" \ 0 \ "paramValue")
        .validateOpt[String]

    } yield {
      SubscriptionDisplayResponse(email, emailVerificationTimestamp, statusText, paramValue)
    }
}
