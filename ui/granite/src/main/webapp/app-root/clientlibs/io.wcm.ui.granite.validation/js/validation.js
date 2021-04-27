/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2017 wcm.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
/*
 * Registers Coral UI 3 validators.
 */
;(function(document, Granite, $, undefined) {
  "use strict";

  // Predefined patterns
  var pattern = {
    email: /^[-0-9a-zA-Z.+_]+@[-0-9a-zA-Z.+_]+\.[a-zA-Z]{2,4}$/,
    // Matches all strings that seem to have a proper URL scheme - e.g. starting with http://, https://, mailto:, tel:
    url: /^([^\/]+:|\/\/).*$/,
    // Matches content paths like /xxx/yyy/zzz
    path: /^(\/[^\/]+)+$/
  };

  var foundationValidator = $(window).adaptTo("foundation-registry");

  var getValue = function(el) {
    if (el.value) {
      return el.value;
    }
    else {
      return $(el).val();
    }
  };

  // predefined "email" pattern validator
  foundationValidator.register('foundation.validation.validator', {
    selector: '[data-foundation-validation="wcmio.email"]',
    validate: function(el) {
      var value = getValue(el);
      var valid = value.length === 0 || pattern.email.test(value);
      if (!valid) {
        return Granite.I18n.get("Please enter a valid email address.");
      }
    }
  });

  // predefined "url" pattern validator
  foundationValidator.register('foundation.validation.validator', {
    selector: '[data-foundation-validation="wcmio.url"]',
    validate: function(el) {
      var value = getValue(el);
      var valid = value.length === 0 || pattern.url.test(value);
      if (!valid) {
        return Granite.I18n.get("Please enter a valid URL.");
      }
    }
  });

  // predefined "path" pattern validator
  foundationValidator.register('foundation.validation.validator', {
    selector: '[data-foundation-validation="wcmio.path"]',
    validate: function(el) {
      var value = getValue(el);
      var valid = value.length === 0 || pattern.path.test(value);
      if (!valid) {
        return Granite.I18n.get("Please enter a valid content path.");
      }
    }
  });

  // "pattern" validator with custom regex pattern and message
  foundationValidator.register('foundation.validation.validator', {
    selector: '[data-foundation-validation="wcmio.pattern"]',
    validate: function(el) {
      el = $(el);
      var regex = el.attr("data-wcmio-pattern");
      var regexMessage = el.attr("data-wcmio-patternmessage");
      var value = el.val();
      var valid = value.length === 0 || new RegExp(regex).test(value);
      if (!valid) {
        return Granite.I18n.get(regexMessage || "Value is invalid.");
      }
    }
  });

})(document, Granite, Granite.$);
