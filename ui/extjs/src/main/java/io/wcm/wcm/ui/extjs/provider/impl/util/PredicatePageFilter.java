/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
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
package io.wcm.wcm.ui.extjs.provider.impl.util;

import org.apache.commons.collections.Predicate;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageFilter;

/**
 * PageFilter that uses a {@link Predicate} instance to evaluate pages in page list.
 */
public final class PredicatePageFilter extends PageFilter {

  private final Predicate predicate;

  /**
   * @param predicate Predicate
   */
  public PredicatePageFilter(Predicate predicate) {
    this(predicate, false, false);
  }

  /**
   * @param predicate Predicate
   * @param includeInvalid if <code>true</code> invalid pages are included.
   * @param includeHidden if <code>true</code> hidden pages are included.
   */
  public PredicatePageFilter(Predicate predicate, boolean includeInvalid, boolean includeHidden) {
    super(includeInvalid, includeHidden);
    this.predicate = predicate;
  }

  @Override
  public boolean includes(Page pPage) {
    if (!super.includes(pPage)) {
      return false;
    }
    return predicate.evaluate(pPage);
  }

}
