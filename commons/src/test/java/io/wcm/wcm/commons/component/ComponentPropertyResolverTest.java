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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.apache.sling.api.resource.Resource;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.components.ComponentContext;
import com.day.cq.wcm.commons.WCMUtils;

import io.wcm.testing.mock.aem.junit.AemContext;

public class ComponentPropertyResolverTest {

  @Rule
  public AemContext context = new AemContext();

  @Test
  public void testResourceWithoutResourceTypeWithoutPage() {
    Resource resource = context.create().resource("/content/r1");

    ComponentPropertyResolver underTest = new ComponentPropertyResolver(resource)
        .componentPropertiesResolution(ComponentPropertyResolution.RESOLVE_INHERIT)
        .pagePropertiesResolution(ComponentPropertyResolution.RESOLVE_INHERIT);
    assertNull(underTest.get("prop1", String.class));
    assertEquals("def", underTest.get("prop1", "def"));
  }

  @Test
  public void testResourceWithComponent() {
    Resource component = context.create().resource("/apps/app1/components/comp1",
        "prop1", "value1");
    Resource resource = context.create().resource("/content/r1",
        "sling:resourceType", component.getPath());

    ComponentPropertyResolver underTest = new ComponentPropertyResolver(resource);
    assertEquals("value1", underTest.get("prop1", String.class));
    assertEquals("value1", underTest.get("prop1", "def"));
  }

  @Test
  public void testResourceWithComponent_Ignore() {
    Resource component = context.create().resource("/apps/app1/components/comp1",
        "prop1", "value1");
    Resource resource = context.create().resource("/content/r1",
        "sling:resourceType", component.getPath());

    ComponentPropertyResolver underTest = new ComponentPropertyResolver(resource)
        .componentPropertiesResolution(ComponentPropertyResolution.IGNORE);
    assertNull(underTest.get("prop1", String.class));
    assertEquals("def", underTest.get("prop1", "def"));
  }

  @Test
  public void testResourceWithSuperComponent_Inheritance() {
    Resource superComponent = context.create().resource("/apps/app1/components/comp2",
        "prop1", "value1");
    Resource component = context.create().resource("/apps/app1/components/comp1",
        "sling:resourceSuperType", superComponent.getPath(),
        "prop2", "value2");
    Resource resource = context.create().resource("/content/r1",
        "sling:resourceType", component.getPath());

    ComponentPropertyResolver underTest = new ComponentPropertyResolver(resource);
    assertEquals("value1", underTest.get("prop1", String.class));
    assertEquals("value1", underTest.get("prop1", "def"));
    assertEquals("value2", underTest.get("prop2", String.class));
    assertEquals("value2", underTest.get("prop2", "def"));
  }

  @Test
  public void testResourceWithSuperComponent_NoInheritance() {
    Resource superComponent = context.create().resource("/apps/app1/components/comp2",
        "prop1", "value1");
    Resource component = context.create().resource("/apps/app1/components/comp1",
        "sling:resourceSuperType", superComponent.getPath(),
        "prop2", "value2");
    Resource resource = context.create().resource("/content/r1",
        "sling:resourceType", component.getPath());

    ComponentPropertyResolver underTest = new ComponentPropertyResolver(resource)
        .componentPropertiesResolution(ComponentPropertyResolution.RESOLVE);
    assertNull(underTest.get("prop1", String.class));
    assertEquals("def", underTest.get("prop1", "def"));
    assertEquals("value2", underTest.get("prop2", String.class));
    assertEquals("value2", underTest.get("prop2", "def"));
  }

  @Test
  public void testPage() {
    Page page = context.create().page("/content/page1", null,
        "prop1", "value1");
    Resource resource = context.create().resource(page, "r1");

    ComponentPropertyResolver underTest = new ComponentPropertyResolver(resource)
        .pagePropertiesResolution(ComponentPropertyResolution.RESOLVE_INHERIT);
    assertEquals("value1", underTest.get("prop1", String.class));
    assertEquals("value1", underTest.get("prop1", "def"));
  }

  @Test
  public void testPage_Inheritance() {
    context.create().page("/content/page1", null,
        "prop1", "value1");
    Page page2 = context.create().page("/content/page1/page2", null,
        "prop2", "value2");
    Resource resource = context.create().resource(page2, "r1");

    ComponentPropertyResolver underTest = new ComponentPropertyResolver(resource)
        .pagePropertiesResolution(ComponentPropertyResolution.RESOLVE_INHERIT);
    assertEquals("value1", underTest.get("prop1", String.class));
    assertEquals("value1", underTest.get("prop1", "def"));
    assertEquals("value2", underTest.get("prop2", String.class));
    assertEquals("value2", underTest.get("prop2", "def"));
  }

  @Test
  public void testPage_NoInheritance() {
    context.create().page("/content/page1", null,
        "prop1", "value1");
    Page page2 = context.create().page("/content/page1/page2", null,
        "prop2", "value2");
    Resource resource = context.create().resource(page2, "r1");

    ComponentPropertyResolver underTest = new ComponentPropertyResolver(resource)
        .pagePropertiesResolution(ComponentPropertyResolution.RESOLVE);
    assertNull(underTest.get("prop1", String.class));
    assertEquals("def", underTest.get("prop1", "def"));
    assertEquals("value2", underTest.get("prop2", String.class));
    assertEquals("value2", underTest.get("prop2", "def"));
  }

  @Test
  public void testPageAndComponent_Inheritance() {
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
        "sling:resourceType", component.getPath());

    ComponentPropertyResolver underTest = new ComponentPropertyResolver(resource)
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
  public void testPageAndComponent_Inheritance_ComponentContext() {
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
        "sling:resourceType", component.getPath());
    context.currentResource(resource);

    ComponentContext wcmComponentContext = WCMUtils.getComponentContext(context.request());
    ComponentPropertyResolver underTest = new ComponentPropertyResolver(wcmComponentContext)
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

}
