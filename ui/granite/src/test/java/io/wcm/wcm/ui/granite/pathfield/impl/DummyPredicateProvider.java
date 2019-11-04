/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2019 wcm.io
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
package io.wcm.wcm.ui.granite.pathfield.impl;

import org.apache.commons.collections.Predicate;

import com.day.cq.commons.predicate.PredicateProvider;

import io.wcm.testing.mock.aem.junit5.AemContext;

class DummyPredicateProvider implements PredicateProvider {

  public static final String PREDICATE_NAME = "predicate.name";

  private final AemContext context;

  DummyPredicateProvider(AemContext context) {
    this.context = context;
  }

  @Override
  public Predicate getPredicate(String name) {
    Predicate[] predicates = context.getServices(Predicate.class, "(" + PREDICATE_NAME + "=" + name + ")");
    if (predicates.length > 0) {
      return predicates[0];
    }
    else {
      return null;
    }
  }

}
