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

package uk.gov.hmrc.customs.emailfrontend.views

import play.api.data.Form
import play.api.i18n.Messages
import uk.gov.hmrc.customs.emailfrontend.Utils.emptyString

object ViewUtils {

  /**
   * Adds Error Prefix (Error :) in the input titleStr if Form has an error
   * otherwise title string is returned without error prefix
   *
   * @param form Form[_]
   * @param titleStr String
   * @param titleMessageArgs Seq[String]
   * @param messages Messages
   * @return String
   */
  def title(form: Form[_], titleStr: String, titleMessageArgs: Seq[String] = Seq())
           (implicit messages: Messages): String =
    titleWithoutForm(s"${errorPrefix(form)} ${messages(titleStr, titleMessageArgs: _*)}").trim

  def titleWithoutForm(title: String, titleMessageArgs: Seq[String] = Seq())(implicit messages: Messages): String =
    s"${messages(title, titleMessageArgs: _*)}"

  /**
   * Returns the value of input prefix message key if Form has any error
   *     Default message key is site.errorPrefix when no key is provided
   * Returns emptyString if Form has no error
   *
   * @param form Form[_]
   * @param prefixString String
   * @param messages Messages
   * @return String
   */
  def errorPrefix(form: Form[_],
                  prefixString: String = "site.errorPrefix")(implicit messages: Messages): String = {
    if (form.hasErrors || form.hasGlobalErrors) messages(prefixString) else emptyString
  }
}