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

import static com.day.cq.commons.jcr.JcrConstants.JCR_PRIMARYTYPE;
import static com.day.cq.commons.jcr.JcrConstants.NT_FILE;
import static com.day.cq.commons.jcr.JcrConstants.NT_HIERARCHYNODE;
import static com.day.cq.commons.jcr.JcrConstants.NT_UNSTRUCTURED;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.collections.Predicate;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class HierarchyNotFilePredicateTest {

  private final AemContext context = new AemContext(ResourceResolverType.JCR_MOCK);

  private Predicate underTest;

  @BeforeEach
  void setUp() {
    underTest = new HierarchyNotFilePredicate();
  }

  @Test
  void testMatch() {
    assertTrue(underTest.evaluate(context.create().resource("/content/test",
        JCR_PRIMARYTYPE, NT_HIERARCHYNODE)));
  }

  @Test
  void testNoMatch() {
    assertFalse(underTest.evaluate(context.create().resource("/content/test",
        JCR_PRIMARYTYPE, NT_UNSTRUCTURED)));
    assertFalse(underTest.evaluate(context.create().resource("/content/test",
        JCR_PRIMARYTYPE, NT_FILE)));
  }

}
