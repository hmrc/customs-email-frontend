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

package unit.model

import org.scalatest.{EitherValues, MustMatchers, OptionValues, WordSpec}
import play.api.mvc.{PathBindable, QueryStringBindable}
import uk.gov.hmrc.customs.emailfrontend.model.Ineligible

class IneligibleSpec extends WordSpec with MustMatchers with EitherValues with OptionValues {

  "Ineligible" must {

    val pathBindable = implicitly[PathBindable[Ineligible.Value]]
    val queryBindable = implicitly[QueryStringBindable[Ineligible.Value]]

    "bind to `NotAdmin` from path" in {

      val result =
        pathBindable.bind("key", "not-admin").right.value

      result mustEqual Ineligible.NotAdmin
    }

    "bind to `no-enrolment` from path" in {

      val result =
        pathBindable.bind("key", "no-enrolment").right.value

      result mustEqual Ineligible.NoEnrolment
    }
    "bind to `is-agent` from path" in {

      val result =
        pathBindable.bind("key", "is-agent").right.value

      result mustEqual Ineligible.IsAgent
    }

    "fail to bind anything else from path" in {

      val result =
        pathBindable.bind("key", "foobar").left.value

      result mustEqual "invalid"
    }

    "unbind from `NotAdmin` to path" in {

      val result =
        pathBindable.unbind("key", Ineligible.NotAdmin)

      result mustEqual "not-admin"
    }

    "unbind from `NoEnrolment` to path" in {

      val result =
        pathBindable.unbind("key", Ineligible.NoEnrolment)

      result mustEqual "no-enrolment"
    }
    "unbind from `IsAgent` to path" in {

      val result =
        pathBindable.unbind("key", Ineligible.IsAgent)

      result mustEqual "is-agent"
    }

    "bind to `IsAgent` from query" in {

      val result =
        queryBindable.bind("key", Map("key" -> Seq("is-agent"))).value.right.value

      result mustEqual Ineligible.IsAgent
    }

    "bind to `NotAdmin` from query" in {

      val result =
        queryBindable.bind("key", Map("key" -> Seq("not-admin"))).value.right.value

      result mustEqual Ineligible.NotAdmin
    }

    "bind to `NoEnrolment` from query" in {

      val result =
        queryBindable.bind("key", Map("key" -> Seq("no-enrolment"))).value.right.value

      result mustEqual Ineligible.NoEnrolment
    }

    "fail to bind anything else from query" in {

      val result =
        queryBindable.bind("key", Map("key" -> Seq("foobar"))).value.left.value

      result mustEqual "invalid"
    }

    "unbind from `IsAgent` to query" in {

      val result =
        queryBindable.unbind("key", Ineligible.IsAgent)

      result mustEqual "is-agent"
    }

    "unbind from `NoEnrolment` to query" in {

      val result =
        queryBindable.unbind("key", Ineligible.NoEnrolment)

      result mustEqual "no-enrolment"
    }

    "unbind from `NotAdmin` to query" in {

      val result =
        queryBindable.unbind("key", Ineligible.NotAdmin)

      result mustEqual "not-admin"
    }
  }
}