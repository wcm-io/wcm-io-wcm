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
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.SyntheticResource;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.components.ComponentContext;
import com.day.cq.wcm.commons.WCMUtils;

import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit5.AemContext;

abstract class AbstractComponentPropertyResolverTest {

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
    assertNull(underTest.get("prop1", String.class));
    assertEquals("def", underTest.get("prop1", "def"));
  }

  @Test
  void testResourceWithComponent() {
    Resource component = context.create().resource("/apps/app1/components/comp1",
        "prop1", "value1");
    Resource resource = context.create().resource("/content/r1",
        PROPERTY_RESOURCE_TYPE, component.getPath());

    ComponentPropertyResolver underTest = getComponentPropertyResolver(resource);
    assertEquals("value1", underTest.get("prop1", String.class));
    assertEquals("value1", underTest.get("prop1", "def"));
  }

  @Test
  void testResourceWithComponent_ForceResourceType() {
    Resource component1 = context.create().resource("/apps/app1/components/comp1",
        "prop1", "value1");
    Resource component2 = context.create().resource("/apps/app1/components/comp2",
        "prop1", "value2");
    Resource resource = context.create().resource("/content/r1",
        PROPERTY_RESOURCE_TYPE, component1.getPath());

    TypeOverwritingResourceWrapper resourceWrapper = new TypeOverwritingResourceWrapper(resource,
        component2.getPath());

    ComponentPropertyResolver underTest = getComponentPropertyResolver(resourceWrapper);
    assertEquals("value2", underTest.get("prop1", String.class));
    assertEquals("value2", underTest.get("prop1", "def"));
  }

  @Test
  void testResourceWithComponent_ChildResourceProperty() {
    Resource component = context.create().resource("/apps/app1/components/comp1",
        "prop1", "value1");
    context.create().resource(component, "child1",
        "prop11", "value11");
    Resource resource = context.create().resource("/content/r1",
        PROPERTY_RESOURCE_TYPE, component.getPath());

    ComponentPropertyResolver underTest = getComponentPropertyResolver(resource);
    assertEquals("value1", underTest.get("prop1", String.class));
    assertEquals("value1", underTest.get("prop1", "def"));
    assertEquals("value11", underTest.get("child1/prop11", String.class));
    assertEquals("value11", underTest.get("child1/prop11", "def"));
  }

  @Test
  void testResourceWithComponent_EnsureResourceType() {
    Resource component = context.create().resource("/apps/app1/components/comp1",
        "prop1", "value1");
    Resource resource = context.create().resource("/content/r1",
        PROPERTY_RESOURCE_TYPE, component.getPath());
    Resource subresource1 = context.create().resource(resource, "subresource1",
        JcrConstants.JCR_PRIMARYTYPE, JcrConstants.NT_UNSTRUCTURED);
    Resource subresource2 = context.create().resource(subresource1, "subresource2");

    ComponentPropertyResolver underTest = getComponentPropertyResolver(subresource2, true);
    assertEquals("value1", underTest.get("prop1", String.class));
    assertEquals("value1", underTest.get("prop1", "def"));
  }

  @Test
  void testResourceWithComponent_EnsureResourceType_ForceResourceType() {
    Resource component1 = context.create().resource("/apps/app1/components/comp1",
        "prop1", "value1");
    Resource component2 = context.create().resource("/apps/app1/components/comp2",
        "prop1", "value2");
    Resource resource = context.create().resource("/content/r1",
        PROPERTY_RESOURCE_TYPE, component1.getPath());
    Resource subresource1 = context.create().resource(resource, "subresource1",
        JcrConstants.JCR_PRIMARYTYPE, JcrConstants.NT_UNSTRUCTURED);
    Resource subresource2 = context.create().resource(subresource1, "subresource2");

    TypeOverwritingResourceWrapper resourceWrapper = new TypeOverwritingResourceWrapper(subresource2,
        component2.getPath());

    ComponentPropertyResolver underTest = getComponentPropertyResolver(resourceWrapper, true);
    assertEquals("value2", underTest.get("prop1", String.class));
    assertEquals("value2", underTest.get("prop1", "def"));
  }

  @Test
  void testResourceWithComponent_Ignore() {
    Resource component = context.create().resource("/apps/app1/components/comp1",
        "prop1", "value1");
    Resource resource = context.create().resource("/content/r1",
        PROPERTY_RESOURCE_TYPE, component.getPath());

    ComponentPropertyResolver underTest = getComponentPropertyResolver(resource)
        .componentPropertiesResolution(ComponentPropertyResolution.IGNORE);
    assertNull(underTest.get("prop1", String.class));
    assertEquals("def", underTest.get("prop1", "def"));
  }

  @Test
  void testResourceWithSuperComponent_Inheritance() {
    Resource superComponent = context.create().resource("/apps/app1/components/comp2",
        "prop1", "value1");
    Resource component = context.create().resource("/apps/app1/components/comp1",
        "sling:resourceSuperType", superComponent.getPath(),
        "prop2", "value2");
    Resource resource = context.create().resource("/content/r1",
        PROPERTY_RESOURCE_TYPE, component.getPath());

    ComponentPropertyResolver underTest = getComponentPropertyResolver(resource);
    assertEquals("value1", underTest.get("prop1", String.class));
    assertEquals("value1", underTest.get("prop1", "def"));
    assertEquals("value2", underTest.get("prop2", String.class));
    assertEquals("value2", underTest.get("prop2", "def"));
  }

  @Test
  void testResourceWithSuperComponent_NoInheritance() {
    Resource superComponent = context.create().resource("/apps/app1/components/comp2",
        "prop1", "value1");
    Resource component = context.create().resource("/apps/app1/components/comp1",
        "sling:resourceSuperType", superComponent.getPath(),
        "prop2", "value2");
    Resource resource = context.create().resource("/content/r1",
        PROPERTY_RESOURCE_TYPE, component.getPath());

    ComponentPropertyResolver underTest = getComponentPropertyResolver(resource)
        .componentPropertiesResolution(ComponentPropertyResolution.RESOLVE);
    assertNull(underTest.get("prop1", String.class));
    assertEquals("def", underTest.get("prop1", "def"));
    assertEquals("value2", underTest.get("prop2", String.class));
    assertEquals("value2", underTest.get("prop2", "def"));
  }

  @Test
  void testPage() {
    Page page = context.create().page("/content/page1", null,
        "prop1", "value1");
    Resource resource = context.create().resource(page, "r1");

    ComponentPropertyResolver underTest = getComponentPropertyResolver(resource)
        .pagePropertiesResolution(ComponentPropertyResolution.RESOLVE_INHERIT);
    assertEquals("value1", underTest.get("prop1", String.class));
    assertEquals("value1", underTest.get("prop1", "def"));
  }

  @Test
  void testPage_Inheritance() {
    context.create().page("/content/page1", null,
        "prop1", "value1");
    Page page2 = context.create().page("/content/page1/page2", null,
        "prop2", "value2");
    Resource resource = context.create().resource(page2, "r1");

    ComponentPropertyResolver underTest = getComponentPropertyResolver(resource)
        .pagePropertiesResolution(ComponentPropertyResolution.RESOLVE_INHERIT);
    assertEquals("value1", underTest.get("prop1", String.class));
    assertEquals("value1", underTest.get("prop1", "def"));
    assertEquals("value2", underTest.get("prop2", String.class));
    assertEquals("value2", underTest.get("prop2", "def"));
  }

  @Test
  void testPage_NoInheritance() {
    context.create().page("/content/page1", null,
        "prop1", "value1");
    Page page2 = context.create().page("/content/page1/page2", null,
        "prop2", "value2");
    Resource resource = context.create().resource(page2, "r1");

    ComponentPropertyResolver underTest = getComponentPropertyResolver(resource)
        .pagePropertiesResolution(ComponentPropertyResolution.RESOLVE);
    assertNull(underTest.get("prop1", String.class));
    assertEquals("def", underTest.get("prop1", "def"));
    assertEquals("value2", underTest.get("prop2", String.class));
    assertEquals("value2", underTest.get("prop2", "def"));
  }

  @Test
  void testPageAndComponent_Inheritance() {
    context.create().page("/content/page1", null,
        "prop1", "value1");
    Page page2 = context.create().page("/content/page1/page2", null,
        "prop2", "value2");

    Resource superComponent = context.create().resource("/apps/app1/components/comp2",
        "prop3", "value3");
    Resource component = context.create().resource("/apps/app1/components/comp1",
        "sling:resourceSuperType", superComponent.getPath(),
        "prop4", "value4");

    Resource resource = context.create().resource(page2, "r1",
        PROPERTY_RESOURCE_TYPE, component.getPath());

    ComponentPropertyResolver underTest = getComponentPropertyResolver(resource)
        .componentPropertiesResolution(ComponentPropertyResolution.RESOLVE_INHERIT)
        .pagePropertiesResolution(ComponentPropertyResolution.RESOLVE_INHERIT);
    assertEquals("value1", underTest.get("prop1", String.class));
    assertEquals("value1", underTest.get("prop1", "def"));
    assertEquals("value2", underTest.get("prop2", String.class));
    assertEquals("value2", underTest.get("prop2", "def"));
    assertEquals("value3", underTest.get("prop3", String.class));
    assertEquals("value3", underTest.get("prop3", "def"));
    assertEquals("value4", underTest.get("prop4", String.class));
    assertEquals("value4", underTest.get("prop4", "def"));
    assertNull(underTest.get("prop5", String.class));
    assertEquals("def5", underTest.get("prop5", "def5"));
  }

  @Test
  void testPageAndPolicyAndComponent_Inheritance_ComponentContext() {
    context.contentPolicyMapping("app1/components/comp1",
        "prop5", "value5b");
    context.contentPolicyMapping("app1/components/comp2",
        "prop6", "value6b");

    context.create().page("/content/page1", null,
        "prop1", "value1a");
    Page page2 = context.create().page("/content/page1/page2", null,
        "prop2", "value2a");

    Resource superComponent = context.create().resource("/apps/app1/components/comp2",
        "prop1", "value1",
        "prop3", "value3");
    Resource component = context.create().resource("/apps/app1/components/comp1",
        "sling:resourceSuperType", superComponent.getPath(),
        "prop2", "value2",
        "prop4", "value4",
        "prop5", "value5");

    Resource resource = context.create().resource(page2, "r1",
        PROPERTY_RESOURCE_TYPE, component.getPath());
    context.currentResource(resource);

    ComponentContext wcmComponentContext = WCMUtils.getComponentContext(context.request());
    ComponentPropertyResolver underTest = getComponentPropertyResolver(wcmComponentContext)
        .pagePropertiesResolution(ComponentPropertyResolution.RESOLVE_INHERIT)
        .contentPolicyResolution(ComponentPropertyResolution.RESOLVE)
        .componentPropertiesResolution(ComponentPropertyResolution.RESOLVE_INHERIT);

    assertEquals("value1a", underTest.get("prop1", String.class));
    assertEquals("value1a", underTest.get("prop1", "def"));
    assertEquals("value2a", underTest.get("prop2", String.class));
    assertEquals("value2a", underTest.get("prop2", "def"));
    assertEquals("value3", underTest.get("prop3", String.class));
    assertEquals("value3", underTest.get("prop3", "def"));
    assertEquals("value4", underTest.get("prop4", String.class));
    assertEquals("value4", underTest.get("prop4", "def"));
    assertEquals("value5b", underTest.get("prop5", String.class));
    assertEquals("value5b", underTest.get("prop5", "def"));
    assertNull(underTest.get("prop6", String.class));
    assertEquals("def", underTest.get("prop6", "def"));
    assertNull(underTest.get("prop7", String.class));
    assertEquals("def", underTest.get("prop7", "def"));
  }

  @Test
  void testContentPolicy() {
    context.contentPolicyMapping("app1/components/comp1",
        "prop1", "value1");

    Page page = context.create().page("/content/page1");
    Resource resource = context.create().resource(page, "r1",
        PROPERTY_RESOURCE_TYPE, "app1/components/comp1");

    ComponentPropertyResolver underTest = getComponentPropertyResolver(resource)
        .contentPolicyResolution(ComponentPropertyResolution.RESOLVE);
    assertEquals("value1", underTest.get("prop1", String.class));
    assertEquals("value1", underTest.get("prop1", "def"));
  }

  @Test
  void testContentPolicy_NoPolicy() {
    Page page = context.create().page("/content/page1");
    Resource resource = context.create().resource(page, "r1",
        PROPERTY_RESOURCE_TYPE, "app1/components/comp1");

    ComponentPropertyResolver underTest = getComponentPropertyResolver(resource)
        .contentPolicyResolution(ComponentPropertyResolution.RESOLVE);
    assertNull(underTest.get("prop1", String.class));
    assertEquals("def", underTest.get("prop1", "def"));
  }

  @Test
  void testContentPolicy_DeepProperty() {
    context.contentPolicyMapping("app1/components/comp1",
        "child1", ImmutableValueMap.of("prop1", "value1"));

    Page page = context.create().page("/content/page1");
    Resource resource = context.create().resource(page, "r1",
        PROPERTY_RESOURCE_TYPE, "app1/components/comp1");

    ComponentPropertyResolver underTest = getComponentPropertyResolver(resource)
        .contentPolicyResolution(ComponentPropertyResolution.RESOLVE);
    assertEquals("value1", underTest.get("child1/prop1", String.class));
    assertEquals("value1", underTest.get("child1/prop1", "def"));
  }

  @Test
  void testResourceWithoutResourceType() {
    Resource resource = context.create().resource("/content/r1");

    ComponentPropertyResolver underTest = getComponentPropertyResolver(resource);
    assertNull(underTest.get("prop1", String.class));
    assertEquals("def", underTest.get("prop1", "def"));
  }

  @Test
  void testSyntheticResourceWithoutResourceType() {
    Resource resource = new SyntheticResource(context.resourceResolver(), "/content/r1", null);

    ComponentPropertyResolver underTest = getComponentPropertyResolver(resource);
    assertNull(underTest.get("prop1", String.class));
    assertEquals("def", underTest.get("prop1", "def"));
  }

}
