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

package uk.gov.hmrc.customs.emailfrontend.model

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{EitherValues, OptionValues}
import play.api.mvc.{PathBindable, QueryStringBindable}

class IneligibleSpec extends AnyWordSpec with Matchers with EitherValues with OptionValues {

  "Ineligible" must {

    val pathBindable  = implicitly[PathBindable[Ineligible.Value]]
    val queryBindable = implicitly[QueryStringBindable[Ineligible.Value]]

    "bind to `NotAdmin` from path" in {
      val result = pathBindable.bind("key", "not-admin").toOption.value
      result mustEqual Ineligible.NotAdmin
    }

    "bind to `no-enrolment` from path" in {
      val result = pathBindable.bind("key", "no-enrolment").toOption.value
      result mustEqual Ineligible.NoEnrolment
    }

    "bind to `is-agent` from path" in {
      val result = pathBindable.bind("key", "is-agent").toOption.value
      result mustEqual Ineligible.IsAgent
    }

    "fail to bind anything else from path" in {
      val result = pathBindable.bind("key", "foobar").left.value
      result mustEqual "invalid"
    }

    "unbind from `NotAdmin` to path" in {
      val result = pathBindable.unbind("key", Ineligible.NotAdmin)
      result mustEqual "not-admin"
    }

    "unbind from `NoEnrolment` to path" in {
      val result = pathBindable.unbind("key", Ineligible.NoEnrolment)
      result mustEqual "no-enrolment"
    }

    "unbind from `IsAgent` to path" in {
      val result = pathBindable.unbind("key", Ineligible.IsAgent)
      result mustEqual "is-agent"
    }

    "bind to `IsAgent` from query" in {

      val result =
        queryBindable
          .bind("key", Map("key" -> Seq("is-agent")))
          .value
          .toOption
          .value

      result mustEqual Ineligible.IsAgent
    }

    "bind to `NotAdmin` from query" in {

      val result =
        queryBindable
          .bind("key", Map("key" -> Seq("not-admin")))
          .value
          .toOption
          .value

      result mustEqual Ineligible.NotAdmin
    }

    "bind to `NoEnrolment` from query" in {

      val result =
        queryBindable
          .bind("key", Map("key" -> Seq("no-enrolment")))
          .value
          .toOption
          .value

      result mustEqual Ineligible.NoEnrolment
    }

    "fail to bind anything else from query" in {
      val result = queryBindable.bind("key", Map("key" -> Seq("foobar"))).value.left.value
      result mustEqual "invalid"
    }

    "unbind from `IsAgent` to query" in {
      val result = queryBindable.unbind("key", Ineligible.IsAgent)
      result mustEqual "is-agent"
    }

    "unbind from `NoEnrolment` to query" in {
      val result = queryBindable.unbind("key", Ineligible.NoEnrolment)
      result mustEqual "no-enrolment"
    }

    "unbind from `NotAdmin` to query" in {
      val result = queryBindable.unbind("key", Ineligible.NotAdmin)
      result mustEqual "not-admin"
    }
  }
}
