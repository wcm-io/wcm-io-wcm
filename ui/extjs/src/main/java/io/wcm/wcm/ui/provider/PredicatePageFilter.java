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
package io.wcm.wcm.ui.provider;

import org.apache.commons.collections.Predicate;

import com.day.cq.commons.predicate.PredicateProvider;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageFilter;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.api.scripting.SlingScriptHelper;

/**
 * PageFilter that uses a {@link Predicate} instance to evaluate pages in page
 * list.
 */
public class PredicatePageFilter extends PageFilter {

  private final Predicate mPredicate;

  /**
   * @param predicateName Predicate name (as registered via predicate.name OSGI
   * property)
   * @param request
   */
  public PredicatePageFilter(String predicateName, SlingHttpServletRequest request) {
    this(predicateName, false, false, request);
  }

  /**
   * @param predicateName Predicate name (as registered via predicate.name OSGI
   * property)
   * @param includeInvalid if <code>true</code> invalid pages are included.
   * @param includeHidden if <code>true</code> hidden pages are included.
   * @param request
   */
  public PredicatePageFilter(String predicateName, boolean includeInvalid, boolean includeHidden, SlingHttpServletRequest request) {
    super(includeInvalid, includeHidden);
    
    SlingBindings bindings = (SlingBindings)request.getAttribute(SlingBindings.class.getName());
    SlingScriptHelper scriptHelper = bindings.getSling();

    PredicateProvider predicateProvider = scriptHelper.getService(PredicateProvider.class);
    if (predicateProvider == null) {
      throw new RuntimeException("PredicateProvider service not running.");
    }
    mPredicate = predicateProvider.getPredicate(predicateName);
    if (mPredicate == null) {
      throw new RuntimeException("Predicate '" + predicateName + "' not defined.");
    }
  }

  @Override
  public boolean includes(Page pPage) {
    if (!super.includes(pPage)) {
      return false;
    }
    return mPredicate.evaluate(pPage);
  }

}
