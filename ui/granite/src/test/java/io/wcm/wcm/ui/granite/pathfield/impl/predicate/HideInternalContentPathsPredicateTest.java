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
package io.wcm.wcm.ui.granite.pathfield.impl.predicate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.collections.Predicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class HideInternalContentPathsPredicateTest {

  private final AemContext context = new AemContext();

  private Predicate underTest;

  @BeforeEach
  void setUp() {
    underTest = new HideInternalContentPathsPredicate();
  }

  @Test
  void testEvaluate() {
    assertTrue(underTest.evaluate(context.resourceResolver().getResource("/")));
    assertTrue(underTest.evaluate(context.create().resource("/content")));
    assertFalse(underTest.evaluate(context.create().resource("/system")));
    assertTrue(underTest.evaluate(context.create().resource("/system/any/other/path")));
    assertFalse(underTest.evaluate(context.create().resource("/content/dam")));
    assertFalse(underTest.evaluate(context.create().resource("/content/dam/catalogs")));
    assertTrue(underTest.evaluate(context.create().resource("/content/dam/any/other/path")));
    assertFalse(underTest.evaluate(context.create().resource("/content/launches")));
    assertTrue(underTest.evaluate(context.create().resource("/content/any/other/path")));
  }

}
