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
package io.wcm.wcm.ui.granite.pathfield.impl.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.commons.collections.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.common.collect.ImmutableList;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class PredicatedResourceWrapperTest {

  private final AemContext context = new AemContext();

  @Test
  void testPredicateMixed() {

    Resource resource = context.create().resource("/content/r1");
    context.create().resource("/content/r1/child1");
    context.create().resource("/content/r1/child2");
    context.create().resource("/content/r1/child3");

    PredicatedResourceWrapper underTest = new PredicatedResourceWrapper(resource, new HideChild2Predcate());

    assertTrue(underTest.hasChildren());

    List<Resource> children = ImmutableList.copyOf(underTest.listChildren());
    assertEquals(2, children.size());
    assertEquals("child1", children.get(0).getName());
    assertEquals("child3", children.get(1).getName());

    assertNotNull(underTest.getChild("child1"));
    assertNull(underTest.getChild("child2"));
    assertNotNull(underTest.getChild("child3"));
  }

  @Test
  void testPredicateOneChild() {

    Resource resource = context.create().resource("/content/r1");
    context.create().resource("/content/r1/child2");

    PredicatedResourceWrapper underTest = new PredicatedResourceWrapper(resource, new HideChild2Predcate());

    assertFalse(underTest.hasChildren());

    List<Resource> children = ImmutableList.copyOf(underTest.listChildren());
    assertTrue(children.isEmpty());

    assertNull(underTest.getChild("child2"));
  }

  @Test
  void testPredicateNoChild() {

    Resource resource = context.create().resource("/content/r1");

    PredicatedResourceWrapper underTest = new PredicatedResourceWrapper(resource, new HideChild2Predcate());

    assertFalse(underTest.hasChildren());

    List<Resource> children = ImmutableList.copyOf(underTest.listChildren());
    assertTrue(children.isEmpty());
  }

  private static class HideChild2Predcate implements Predicate {
    @Override
    public boolean evaluate(Object object) {
      Resource resource = (Resource)object;
      return !StringUtils.equals(resource.getName(), "child2");
    }
  }

}
