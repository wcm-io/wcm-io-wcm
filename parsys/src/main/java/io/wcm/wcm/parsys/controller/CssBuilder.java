/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2015 wcm.io
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
package io.wcm.wcm.parsys.controller;

import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

/**
 * Builds CSS strings from optional parts.
 */
class CssBuilder {

  private final SortedSet<String> items = new TreeSet<>();

  /**
   * Add CSS class item. Empty/Null items are ignore.d
   * @param cssClass Item
   */
  public void add(String cssClass) {
    if (StringUtils.isBlank(cssClass)) {
      return;
    }
    String[] parts = StringUtils.split(cssClass, " ");
    for (String part : parts) {
      items.add(StringUtils.trim(part));
    }
  }

  /**
   * @return CSS string or null if no valid items are defined.
   */
  public String build() {
    if (items.isEmpty()) {
      return null;
    }
    else {
      return StringUtils.join(items, " ");
    }
  }

  @Override
  public String toString() {
    return StringUtils.defaultString(build());
  }

}
