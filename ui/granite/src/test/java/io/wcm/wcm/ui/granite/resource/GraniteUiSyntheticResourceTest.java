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
import static io.wcm.wcm.ui.granite.resource.GraniteUiSyntheticResource.copySubtree;
import static io.wcm.wcm.ui.granite.resource.GraniteUiSyntheticResource.create;
import static io.wcm.wcm.ui.granite.resource.GraniteUiSyntheticResource.wrap;
import static io.wcm.wcm.ui.granite.resource.GraniteUiSyntheticResource.wrapMerge;
import static org.apache.sling.api.resource.ResourceResolver.PROPERTY_RESOURCE_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.commons.jcr.JcrConstants;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class GraniteUiSyntheticResourceTest {

  private static final ValueMap SAMPLE_PROPERTES = new ValueMapDecorator(ImmutableMap.<String, Object>builder()
      .put("sling:resourceType", "/sample/type")
      .put("prop1", "value1")
      .put("prop2", 25)
      .build());

  private static final ValueMap OTHER_SAMPLE_PROPERTES = new ValueMapDecorator(ImmutableMap.<String, Object>builder()
      .put("prop1", "value2")
      .put("prop3", 55)
      .build());

  private final AemContext context = new AemContext(ResourceResolverType.JCR_MOCK);

  @Test
  void testCreate() {
    Resource underTest = create(context.resourceResolver(), "/my/path", "/my/type");
    assertEquals("/my/path", underTest.getPath());
    assertEquals("/my/type", underTest.getResourceType());
  }

  @Test
  void testCreateProperties() {
    Resource underTest = create(context.resourceResolver(), "/my/path", "/my/type", SAMPLE_PROPERTES);
    assertEquals("/my/path", underTest.getPath());
    assertEquals("/my/type", underTest.getResourceType());
    assertEquals("value1", underTest.getValueMap().get("prop1", String.class));
    assertEquals(25, (int)underTest.getValueMap().get("prop2", 0));
  }

  @Test
  void testCreateProperitesWithoutPath() {
    Resource underTest = create(context.resourceResolver(), SAMPLE_PROPERTES);
    assertEquals(null, underTest.getPath());
    assertEquals(JcrConstants.NT_UNSTRUCTURED, underTest.getResourceType());
    assertEquals("value1", underTest.getValueMap().get("prop1", String.class));
    assertEquals(25, (int)underTest.getValueMap().get("prop2", 0));
  }

  @Test
  void testWrap() {
    Resource original = context.create().resource("/original/path", SAMPLE_PROPERTES);

    Resource underTest = wrap(original);
    assertEquals("/original/path", underTest.getPath());
    assertEquals("value1", underTest.getValueMap().get("prop1", String.class));
    assertEquals(25, (int)underTest.getValueMap().get("prop2", 0));
  }

  @Test
  void testWrapProperties() {
    Resource original = context.create().resource("/original/path", SAMPLE_PROPERTES);

    Resource underTest = wrap(original, OTHER_SAMPLE_PROPERTES);
    assertEquals("/original/path", underTest.getPath());
    assertEquals("value2", underTest.getValueMap().get("prop1", String.class));
    assertEquals(0, (int)underTest.getValueMap().get("prop2", 0));
    assertEquals(55, (int)underTest.getValueMap().get("prop3", 0));
  }

  @Test
  void testWrapMerge() throws Exception {
    Resource original = context.create().resource("/original/path", SAMPLE_PROPERTES);

    Resource underTest = wrapMerge(original, OTHER_SAMPLE_PROPERTES);
    assertEquals("/original/path", underTest.getPath());
    assertEquals("value2", underTest.getValueMap().get("prop1", String.class));
    assertEquals(25, (int)underTest.getValueMap().get("prop2", 0));
    assertEquals(55, (int)underTest.getValueMap().get("prop3", 0));
  }

  @Test
  @SuppressWarnings("null")
  void testChild() {
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
  void testChildWithPropeties() {
    Resource parent = create(context.resourceResolver(), "/my/path", "/my/type");

    Resource child1 = child(parent, "child1", "/my/child/type", SAMPLE_PROPERTES);
    assertEquals("/my/path/child1", child1.getPath());
    assertEquals("/my/child/type", child1.getResourceType());
    assertEquals("value1", child1.getValueMap().get("prop1", String.class));
    assertEquals(25, (int)child1.getValueMap().get("prop2", 0));
  }

  @Test
  @SuppressWarnings("null")
  void testChildWithPropetiesThatAlreadyExists() {
    Resource existingParent = context.create().resource("/my/path",
        PROPERTY_RESOURCE_TYPE, "/my/type");
    context.create().resource(existingParent, "child1",
        PROPERTY_RESOURCE_TYPE, "/my/child/type",
        "prop1", "value1");

    Resource parent = wrapMerge(existingParent, ValueMap.EMPTY);

    Resource child1 = child(parent, "child1", "/my/child/type", SAMPLE_PROPERTES);
    child1 = parent.getChild("child1");

    assertEquals("/my/path/child1", child1.getPath());
    assertEquals("/my/child/type", child1.getResourceType());
    assertEquals("value1", child1.getValueMap().get("prop1", String.class));
    assertEquals(25, (int)child1.getValueMap().get("prop2", 0));
  }

  @Test
  void testCopySubtree() {
    Resource parent = create(context.resourceResolver(), "/target/path", "/my/type");

    Resource source = context.create().resource("/source",
        "sling:resourceType", "/my/type2",
        "prop1", "value1");
    context.create().resource("/source/child1",
        "sling:resourceType", "/my/type3",
        "prop1", "value2");
    context.create().resource("/source/child1/child11");
    context.create().resource("/source/child1/child12");
    context.create().resource("/source/child2");

    copySubtree(parent, source);

    List<Resource> children1 = ImmutableList.copyOf(parent.getChildren());
    assertEquals(1, children1.size());
    assertEquals("/target/path/source", children1.get(0).getPath());
    assertEquals("value1", children1.get(0).getValueMap().get("prop1", String.class));

    List<Resource> children2 = ImmutableList.copyOf(children1.get(0).getChildren());
    assertEquals(2, children2.size());
    assertEquals("/target/path/source/child1", children2.get(0).getPath());
    assertEquals("value2", children2.get(0).getValueMap().get("prop1", String.class));
    assertEquals("/target/path/source/child2", children2.get(1).getPath());

    List<Resource> children3 = ImmutableList.copyOf(children2.get(0).getChildren());
    assertEquals(2, children3.size());
    assertEquals("/target/path/source/child1/child11", children3.get(0).getPath());
    assertEquals("/target/path/source/child1/child12", children3.get(1).getPath());
  }

}
