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
package io.wcm.wcm.ui.extjs.provider.impl.servlets;

import org.apache.commons.collections.Predicate;
import org.apache.commons.lang3.StringUtils;

import com.day.cq.commons.predicate.PredicateProvider;
import com.day.cq.wcm.api.Page;

/**
 * Dummy implementation of {@link PredicateProvider}.
 */
public class DummyPredicateProvider implements PredicateProvider {

  public static final String PREDICATE_PAGENAME_PAGE1 = "page1Predicate";

  @Override
  public Predicate getPredicate(String name) {
    if (StringUtils.equals(name, PREDICATE_PAGENAME_PAGE1)) {
      return new Predicate() {

        @Override
        public boolean evaluate(Object object) {
          Page page = (Page)object;
          return StringUtils.equals(page.getName(), "page1");
        }
      };
    }
    return null;
  }

}
