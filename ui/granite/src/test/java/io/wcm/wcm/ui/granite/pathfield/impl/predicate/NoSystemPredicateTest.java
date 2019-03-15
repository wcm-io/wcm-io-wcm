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
import static com.day.cq.commons.jcr.JcrConstants.NT_FOLDER;
import static com.day.cq.commons.jcr.JcrConstants.NT_UNSTRUCTURED;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.collections.Predicate;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import io.wcm.testing.mock.aem.junit.AemContext;

public class NoSystemPredicateTest {

  @Rule
  public AemContext context = new AemContext(ResourceResolverType.JCR_MOCK);

  private Predicate underTest;

  @Before
  public void setUp() {
    underTest = new NoSystemPredicate();
  }

  @Test
  public void testMatch() {
    assertTrue(underTest.evaluate(context.create().resource("/content/test1",
        JCR_PRIMARYTYPE, NT_FOLDER)));
    assertTrue(underTest.evaluate(context.create().resource("/content/test2",
        JCR_PRIMARYTYPE, NT_UNSTRUCTURED)));
  }

  @Test
  public void testNoMatch() {
    assertFalse(underTest.evaluate(context.create().resource("/content/rep:test1",
        JCR_PRIMARYTYPE, NT_UNSTRUCTURED)));
    assertFalse(underTest.evaluate(context.create().resource("/content/jcr:system",
        JCR_PRIMARYTYPE, NT_UNSTRUCTURED)));
  }

}
