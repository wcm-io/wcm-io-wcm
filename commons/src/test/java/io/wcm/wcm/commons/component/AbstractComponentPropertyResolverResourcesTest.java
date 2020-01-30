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
package io.wcm.wcm.commons.component;

import static org.apache.sling.api.resource.ResourceResolver.PROPERTY_RESOURCE_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.components.ComponentContext;
import com.day.cq.wcm.commons.WCMUtils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;

import io.wcm.testing.mock.aem.junit5.AemContext;

abstract class AbstractComponentPropertyResolverResourcesTest {

  final AemContext context = new AemContext();

  abstract ComponentPropertyResolver getComponentPropertyResolver(@NotNull Page page);
  abstract ComponentPropertyResolver getComponentPropertyResolver(@NotNull Resource resource);
  abstract ComponentPropertyResolver getComponentPropertyResolver(@NotNull Resource resource, boolean ensureResourceType);
  abstract ComponentPropertyResolver getComponentPropertyResolver(@NotNull ComponentContext wcmComponentContext);

  @Test
  void testResourceWithoutResourceTypeWithoutPage() {
    Resource resource = context.create().resource("/content/r1");

    ComponentPropertyResolver underTest = getComponentPropertyResolver(resource)
        .componentPropertiesResolution(ComponentPropertyResolution.RESOLVE_INHERIT)
        .pagePropertiesResolution(ComponentPropertyResolution.RESOLVE_INHERIT);
    assertNull(underTest.getResources("node1"));
  }

  @Test
  void testResourceWithComponent() {
    Resource component = context.create().resource("/apps/app1/components/comp1");
    context.create().resource(component, "node1/item1");
    context.create().resource(component, "node1/item2");
    Resource resource = context.create().resource("/content/r1",
        PROPERTY_RESOURCE_TYPE, component.getPath());

    ComponentPropertyResolver underTest = getComponentPropertyResolver(resource);
    assertResources(underTest.getResources("node1"), "item1", "item2");
  }

  @Test
  void testResourceWithComponent_Ignore() {
    Resource component = context.create().resource("/apps/app1/components/comp1");
    context.create().resource(component, "node1/item1");
    context.create().resource(component, "node1/item2");
    Resource resource = context.create().resource("/content/r1",
        PROPERTY_RESOURCE_TYPE, component.getPath());

    ComponentPropertyResolver underTest = getComponentPropertyResolver(resource)
        .componentPropertiesResolution(ComponentPropertyResolution.IGNORE);
    assertNull(underTest.getResources("node1"));
  }

  @Test
  void testResourceWithSuperComponent_Inheritance() {
    Resource superComponent = context.create().resource("/apps/app1/components/comp2");
    context.create().resource(superComponent, "node1/item1");
    context.create().resource(superComponent, "node1/item2");
    Resource component = context.create().resource("/apps/app1/components/comp1",
        "sling:resourceSuperType", superComponent.getPath());
    context.create().resource(component, "node2/item1");
    Resource resource = context.create().resource("/content/r1",
        PROPERTY_RESOURCE_TYPE, component.getPath());

    ComponentPropertyResolver underTest = getComponentPropertyResolver(resource);
    assertResources(underTest.getResources("node1"), "item1", "item2");
    assertResources(underTest.getResources("node2"), "item1");
  }

  @Test
  void testResourceWithSuperComponent_NoInheritance() {
    Resource superComponent = context.create().resource("/apps/app1/components/comp2");
    context.create().resource(superComponent, "node1/item1");
    context.create().resource(superComponent, "node1/item2");
    Resource component = context.create().resource("/apps/app1/components/comp1",
        "sling:resourceSuperType", superComponent.getPath());
    context.create().resource(component, "node2/item1");
    Resource resource = context.create().resource("/content/r1",
        PROPERTY_RESOURCE_TYPE, component.getPath());

    ComponentPropertyResolver underTest = getComponentPropertyResolver(resource)
        .componentPropertiesResolution(ComponentPropertyResolution.RESOLVE);
    assertNull(underTest.getResources("node1"));
    assertResources(underTest.getResources("node2"), "item1");
  }

  @Test
  void testPage() {
    Page page = context.create().page("/content/page1");
    context.create().resource(page, "node1/item1");
    context.create().resource(page, "node1/item2");
    Resource resource = context.create().resource(page, "r1");

    ComponentPropertyResolver underTest = getComponentPropertyResolver(resource)
        .pagePropertiesResolution(ComponentPropertyResolution.RESOLVE_INHERIT);
    assertResources(underTest.getResources("node1"), "item1", "item2");
  }

  @Test
  void testPage_Inheritance() {
    Page page1 = context.create().page("/content/page1");
    context.create().resource(page1, "node1/item1");
    context.create().resource(page1, "node1/item2");
    Page page2 = context.create().page("/content/page1/page2");
    context.create().resource(page2, "node2/item1");
    Resource resource = context.create().resource(page2, "r1");

    ComponentPropertyResolver underTest = getComponentPropertyResolver(resource)
        .pagePropertiesResolution(ComponentPropertyResolution.RESOLVE_INHERIT);
    assertResources(underTest.getResources("node1"), "item1", "item2");
    assertResources(underTest.getResources("node2"), "item1");
  }

  @Test
  void testPage_NoInheritance() {
    Page page1 = context.create().page("/content/page1");
    context.create().resource(page1, "node1/item1");
    context.create().resource(page1, "node1/item2");
    Page page2 = context.create().page("/content/page1/page2");
    context.create().resource(page2, "node2/item1");
    Resource resource = context.create().resource(page2, "r1");

    ComponentPropertyResolver underTest = getComponentPropertyResolver(resource)
        .pagePropertiesResolution(ComponentPropertyResolution.RESOLVE);
    assertNull(underTest.get("prop1", String.class));
    assertNull(underTest.getResources("node1"));
    assertResources(underTest.getResources("node2"), "item1");
  }

  @Test
  void testPageAndComponent_Inheritance() {
    Page page1 = context.create().page("/content/page1");
    context.create().resource(page1, "node1/item1");
    context.create().resource(page1, "node1/item2");
    Page page2 = context.create().page("/content/page1/page2");
    context.create().resource(page2, "node2/item1");

    Resource superComponent = context.create().resource("/apps/app1/components/comp2");
    context.create().resource(superComponent, "node3/item1");
    context.create().resource(superComponent, "node3/item2");
    context.create().resource(superComponent, "node3/item3");
    Resource component = context.create().resource("/apps/app1/components/comp1",
        "sling:resourceSuperType", superComponent.getPath());
    context.create().resource(component, "node4/item2");
    context.create().resource(component, "node1/item2");
    context.create().resource(component, "node1/item4");

    Resource resource = context.create().resource(page2, "r1",
        PROPERTY_RESOURCE_TYPE, component.getPath());

    ComponentPropertyResolver underTest = getComponentPropertyResolver(resource)
        .componentPropertiesResolution(ComponentPropertyResolution.RESOLVE_INHERIT)
        .pagePropertiesResolution(ComponentPropertyResolution.RESOLVE_INHERIT);
    assertResources(underTest.getResources("node1"), "item1", "item2");
    assertResources(underTest.getResources("node2"), "item1");
    assertResources(underTest.getResources("node3"), "item1", "item2", "item3");
    assertResources(underTest.getResources("node4"), "item2");
    assertNull(underTest.getResources("nodeX"));
  }

  @Test
  void testPageAndPolicyAndComponent_Inheritance_ComponentContext() {
    context.contentPolicyMapping("app1/components/comp1",
        "node2", ImmutableSortedMap.of(
            "item2", ImmutableMap.of()),
        "node5", ImmutableSortedMap.of(
            "item5", ImmutableMap.of()));
    context.contentPolicyMapping("app1/components/comp2",
        "node6", ImmutableSortedMap.of(
            "item6", ImmutableMap.of(),
            "item7", ImmutableMap.of()));

    Page page1 = context.create().page("/content/page1");
    context.create().resource(page1, "node1/item1");
    context.create().resource(page1, "node1/item2");
    Page page2 = context.create().page("/content/page1/page2");
    context.create().resource(page2, "node2/item1");

    Resource superComponent = context.create().resource("/apps/app1/components/comp2");
    context.create().resource(superComponent, "node3/item1");
    context.create().resource(superComponent, "node3/item2");
    context.create().resource(superComponent, "node3/item3");
    Resource component = context.create().resource("/apps/app1/components/comp1",
        "sling:resourceSuperType", superComponent.getPath());
    context.create().resource(component, "node4/item2");
    context.create().resource(component, "node1/item2");
    context.create().resource(component, "node1/item4");

    Resource resource = context.create().resource(page2, "r1",
        PROPERTY_RESOURCE_TYPE, component.getPath());
    context.currentResource(resource);

    ComponentContext wcmComponentContext = WCMUtils.getComponentContext(context.request());
    ComponentPropertyResolver underTest = getComponentPropertyResolver(wcmComponentContext)
        .pagePropertiesResolution(ComponentPropertyResolution.RESOLVE_INHERIT)
        .contentPolicyResolution(ComponentPropertyResolution.RESOLVE)
        .componentPropertiesResolution(ComponentPropertyResolution.RESOLVE_INHERIT);

    assertResources(underTest.getResources("node1"), "item1", "item2");
    assertResources(underTest.getResources("node2"), "item1");
    assertResources(underTest.getResources("node3"), "item1", "item2", "item3");
    assertResources(underTest.getResources("node4"), "item2");
    assertResources(underTest.getResources("node5"), "item5");
    assertNull(underTest.getResources("node6"));
    assertNull(underTest.getResources("nodeX"));
  }

  @Test
  void testContentPolicy() {
    context.contentPolicyMapping("app1/components/comp1",
        "node1", ImmutableSortedMap.of(
            "item1", ImmutableMap.of(),
            "item2", ImmutableMap.of()));

    Page page = context.create().page("/content/page1");
    Resource resource = context.create().resource(page, "r1",
        PROPERTY_RESOURCE_TYPE, "app1/components/comp1");

    ComponentPropertyResolver underTest = getComponentPropertyResolver(resource)
        .contentPolicyResolution(ComponentPropertyResolution.RESOLVE);
    assertResourcesIgnoreOrder(underTest.getResources("node1"), "item1", "item2");
  }

  @Test
  void testContentPolicy_NoPolicy() {
    Page page = context.create().page("/content/page1");
    Resource resource = context.create().resource(page, "r1",
        PROPERTY_RESOURCE_TYPE, "app1/components/comp1");

    ComponentPropertyResolver underTest = getComponentPropertyResolver(resource)
        .contentPolicyResolution(ComponentPropertyResolution.RESOLVE);
    assertNull(underTest.getResources("node1"));
  }

  private void assertResources(Collection<Resource> resources, String... children) {
    assertNotNull(resources, "found resources");
    // compare child name using set instead of list (ignoring ordering)
    // until https://issues.apache.org/jira/browse/SLING-8628 is released
    List<String> expected = Arrays.stream(children)
        .collect(Collectors.toList());
    List<String> actual = resources.stream()
        .map(Resource::getName)
        .collect(Collectors.toList());
    assertEquals(expected, actual);
  }

  /*
   * Compare child resource names from content policies using set instead of list (ignoring child order)
   * until https://issues.apache.org/jira/browse/SLING-8628 is released
   */
  private void assertResourcesIgnoreOrder(Collection<Resource> resources, String... children) {
    assertNotNull(resources, "found resources");
    Set<String> expected = Arrays.stream(children)
        .collect(Collectors.toSet());
    Set<String> actual = resources.stream()
        .map(Resource::getName)
        .collect(Collectors.toSet());
    assertEquals(expected, actual);
  }

}
