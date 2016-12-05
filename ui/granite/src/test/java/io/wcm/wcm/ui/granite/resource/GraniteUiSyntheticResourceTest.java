/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 - 2015 wcm.io
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
package io.wcm.wcm.ui.granite.resource;

import static io.wcm.wcm.ui.granite.resource.GraniteUiSyntheticResource.child;
import static io.wcm.wcm.ui.granite.resource.GraniteUiSyntheticResource.create;
import static io.wcm.wcm.ui.granite.resource.GraniteUiSyntheticResource.wrap;
import static io.wcm.wcm.ui.granite.resource.GraniteUiSyntheticResource.wrapMerge;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit.SlingContext;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.commons.jcr.JcrConstants;
import com.google.common.collect.ImmutableList;

import io.wcm.sling.commons.resource.ImmutableValueMap;

public class GraniteUiSyntheticResourceTest {

  private static final ValueMap SAMPLE_PROPERTES = ImmutableValueMap.builder()
      .put("sling:resourceType", "/sample/type")
      .put("prop1", "value1")
      .put("prop2", 25)
      .build();

  private static final ValueMap OTHER_SAMPLE_PROPERTES = ImmutableValueMap.builder()
      .put("prop1", "value2")
      .put("prop3", 55)
      .build();

  @Rule
  public SlingContext context = new SlingContext(ResourceResolverType.JCR_MOCK);

  @Test
  public void testCreate() {
    Resource underTest = create(context.resourceResolver(), "/my/path", "/my/type");
    assertEquals("/my/path", underTest.getPath());
    assertEquals("/my/type", underTest.getResourceType());
  }

  @Test
  public void testCreateProperties() {
    Resource underTest = create(context.resourceResolver(), "/my/path", "/my/type", SAMPLE_PROPERTES);
    assertEquals("/my/path", underTest.getPath());
    assertEquals("/my/type", underTest.getResourceType());
    assertEquals("value1", underTest.getValueMap().get("prop1", String.class));
    assertEquals(25, (int)underTest.getValueMap().get("prop2", 0));
  }

  @Test
  public void testCreateProperitesWithoutPath() {
    Resource underTest = create(context.resourceResolver(), SAMPLE_PROPERTES);
    assertEquals(null, underTest.getPath());
    assertEquals(JcrConstants.NT_UNSTRUCTURED, underTest.getResourceType());
    assertEquals("value1", underTest.getValueMap().get("prop1", String.class));
    assertEquals(25, (int)underTest.getValueMap().get("prop2", 0));
  }

  @Test
  public void testWrap() {
    Resource original = context.create().resource("/original/path", SAMPLE_PROPERTES);

    Resource underTest = wrap(original);
    assertEquals("/original/path", underTest.getPath());
    assertEquals("value1", underTest.getValueMap().get("prop1", String.class));
    assertEquals(25, (int)underTest.getValueMap().get("prop2", 0));
  }

  @Test
  public void testWrapProperties() {
    Resource original = context.create().resource("/original/path", SAMPLE_PROPERTES);

    Resource underTest = wrap(original, OTHER_SAMPLE_PROPERTES);
    assertEquals("/original/path", underTest.getPath());
    assertEquals("value2", underTest.getValueMap().get("prop1", String.class));
    assertEquals(0, (int)underTest.getValueMap().get("prop2", 0));
    assertEquals(55, (int)underTest.getValueMap().get("prop3", 0));
  }

  @Test
  public void testWrapMerge() throws Exception {
    Resource original = context.create().resource("/original/path", SAMPLE_PROPERTES);

    Resource underTest = wrapMerge(original, OTHER_SAMPLE_PROPERTES);
    assertEquals("/original/path", underTest.getPath());
    assertEquals("value2", underTest.getValueMap().get("prop1", String.class));
    assertEquals(25, (int)underTest.getValueMap().get("prop2", 0));
    assertEquals(55, (int)underTest.getValueMap().get("prop3", 0));
  }

  @Test
  public void testChild() {
    Resource parent = create(context.resourceResolver(), "/my/path", "/my/type");

    Resource child1 = child(parent, "child1", "/my/child/type");
    assertEquals("/my/path/child1", child1.getPath());
    assertEquals("/my/child/type", child1.getResourceType());

    Resource child2 = child(parent, "child2", "/my/child/type");
    assertEquals("/my/path/child2", child2.getPath());
    assertEquals("/my/child/type", child2.getResourceType());

    List<Resource> childrenFromIterator = ImmutableList.copyOf(parent.listChildren());
    List<Resource> childrenFromIterable = ImmutableList.copyOf(parent.getChildren());
    assertEquals(childrenFromIterator, childrenFromIterable);
    assertEquals(2, childrenFromIterator.size());
    assertTrue(parent.hasChildren());

    assertEquals("/my/path/child1", parent.getChild("child1").getPath());
    assertEquals("/my/path/child2", parent.getChild("child2").getPath());
    assertNull(parent.getChild("child3"));
  }

  @Test
  public void testChildWithPropeties() {
    Resource parent = create(context.resourceResolver(), "/my/path", "/my/type");

    Resource child1 = child(parent, "child1", "/my/child/type", SAMPLE_PROPERTES);
    assertEquals("/my/path/child1", child1.getPath());
    assertEquals("/my/child/type", child1.getResourceType());
    assertEquals("value1", child1.getValueMap().get("prop1", String.class));
    assertEquals(25, (int)child1.getValueMap().get("prop2", 0));
  }

}
