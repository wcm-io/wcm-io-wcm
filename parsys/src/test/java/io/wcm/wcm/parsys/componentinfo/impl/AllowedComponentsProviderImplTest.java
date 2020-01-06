/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
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
package io.wcm.wcm.parsys.componentinfo.impl;

import static io.wcm.testing.mock.wcmio.sling.ContextPlugins.WCMIO_SLING;
import static org.apache.sling.api.resource.ResourceResolver.PROPERTY_RESOURCE_TYPE;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.wcm.api.Page;

import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextBuilder;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.parsys.componentinfo.AllowedComponentsProvider;

@ExtendWith(AemContextExtension.class)
class AllowedComponentsProviderImplTest {

  private final AemContext context = new AemContextBuilder().plugin(WCMIO_SLING).build();

  private static final String CONTENT_ROOT_PATH = "/content/dummy";
  private static final String BASE_TEMPLATE = "/apps/dummy/templates/baseTemplate";
  private static final String BASE_PAGE_COMPONENT = "dummy/components/page/basePage";
  private static final String INHERITED_TEMPLATE = "/apps/dummy/templates/inheritedTemplate";
  private static final String INHERITED_PAGE_COMPONENT = "dummy/components/page/inheritedPage";

  private AllowedComponentsProvider underTest;

  private Page basePage;
  private Page inheritedPage;

  @BeforeEach
  void setUp() {
    context.registerInjectActivateService(new ParsysConfigManagerImpl());
    context.registerInjectActivateService(new AllowedComponentsProviderImpl());

    // mount template and page component definitions
    context.load().json("/parsys/baseTemplate.json", BASE_TEMPLATE);
    context.load().json("/parsys/basePageComponent.json", BASE_PAGE_COMPONENT);
    context.load().json("/parsys/inheritedTemplate.json", INHERITED_TEMPLATE);
    context.load().json("/parsys/inheritedPageComponent.json", INHERITED_PAGE_COMPONENT);

    // create pages with dummy content
    basePage = context.create().page(CONTENT_ROOT_PATH + "/page-1", BASE_TEMPLATE,
        ImmutableValueMap.of(PROPERTY_RESOURCE_TYPE, BASE_PAGE_COMPONENT));
    addDummyContent(basePage);

    inheritedPage = context.create().page(CONTENT_ROOT_PATH + "/page-2", INHERITED_TEMPLATE,
        ImmutableValueMap.of(PROPERTY_RESOURCE_TYPE, INHERITED_PAGE_COMPONENT));
    addDummyContent(inheritedPage);
    context.create().resource(inheritedPage.getContentResource().getPath() + "/special",
        ImmutableValueMap.of(PROPERTY_RESOURCE_TYPE, "dummy/components/parsys"));

    underTest = context.getService(AllowedComponentsProvider.class);
  }

  @Test
  void testGetAllowedComponentsForParsys() {
    String contentParsys = CONTENT_ROOT_PATH + "/page-1/jcr:content/content";
    Set<String> allowedComponents = underTest.getAllowedComponents(contentParsys, context.resourceResolver());

    // null check
    assertNotNull(allowedComponents, "Allowed component for parsys cannot be null");

    // positive tests
    assertTrue(allowedComponents.contains("dummy/components/comp1"),
        "Component 'comp1' must be allowed in " + contentParsys + ".");
    assertTrue(allowedComponents.contains("dummy/components/comp2"),
        "Component 'comp2' must be allowed in " + contentParsys + ".");
    assertTrue(allowedComponents.contains("dummy/components/linklist"),
        "Component 'linklist' must be allowed in " + contentParsys + ".");
    assertTrue(allowedComponents.contains("dummy/components/container2col"),
        "Component 'container2col' must be allowed in " + contentParsys + ".");

    // negative tests
    assertFalse(allowedComponents.contains("dummy/components/nestedComp2"),
        "Component 'nestedComp2' should not be allowed in " + contentParsys + ".");
    assertFalse(allowedComponents.contains("dummy/components/comp3"),
        "Component 'comp3' should not be allowed in " + contentParsys + ".");
  }

  @Test
  void testGetAllowedComponentsForNestedParsys() {
    String nested2ColParsys = CONTENT_ROOT_PATH + "/page-1/jcr:content/content/2colContainer/items";
    Set<String> allowedComponents = underTest.getAllowedComponents(nested2ColParsys, context.resourceResolver());

    // null check
    assertNotNull(allowedComponents,
        "Allowed component for parsys cannot be null");

    // positive tests
    assertTrue(allowedComponents.contains("dummy/components/nestedComp1"),
        "Component 'nestedComp1' must be allowed in " + nested2ColParsys + ".");
    assertTrue(allowedComponents.contains("dummy/components/nestedComp2"),
        "Component 'nestedComp2' must be allowed in " + nested2ColParsys + ".");
    assertTrue(allowedComponents.contains("dummy/components/linklist"),
        "Component 'linklist' must be allowed in " + nested2ColParsys + ".");

    // negative tests
    assertFalse(allowedComponents.contains("dummy/components/comp1"),
        "Component 'comp1' should not be allowed in " + nested2ColParsys + ".");
  }

  @Test
  void testGetAllowedComponentsForNestedNestedParsys() {
    String linklist = CONTENT_ROOT_PATH + "/page-1/jcr:content/content/2colContainer/linklist/links";
    Set<String> allowedComponents = underTest.getAllowedComponents(linklist, context.resourceResolver());

    // null check
    assertNotNull(allowedComponents,
        "Allowed component for parsys cannot be null");

    // positive tests
    assertTrue(allowedComponents.contains("dummy/components/linkItem"),
        "Component 'linkItem' must be allowed in " + linklist + ".");

    // negative tests
    assertFalse(allowedComponents.contains("dummy/components/comp1"),
        "Component 'comp1' should not be allowed in " + linklist + ".");
    assertFalse(allowedComponents.contains("dummy/components/nestedComp1"),
        "Component 'nestedComp1' must be allowed in " + linklist + ".");
  }

  @Test
  void testGetAllowedComponentsForTemplate() {
    Set<String> allowedComponents = underTest.getAllowedComponentsForTemplate(BASE_PAGE_COMPONENT, context.resourceResolver());

    // null check
    assertNotNull(allowedComponents,
        "Allowed components for template cannot be null");

    // positive tests
    assertTrue(allowedComponents.contains("dummy/components/comp1"),
        "Component 'comp1' must be allowed in page " + BASE_PAGE_COMPONENT + ".");
    assertTrue(allowedComponents.contains("dummy/components/nestedComp2"),
        "Component 'nestedComp2' must be allowed in page " + BASE_PAGE_COMPONENT + ".");

    // negative tests
    assertFalse(allowedComponents.contains("dummy/components/comp3"),
        "Component 'comp3' should not be allowed in page " + BASE_PAGE_COMPONENT + ".");

  }

  @Test
  void testGetAllowedComponentsForInheritedParsys() {
    // ---- special parsys (not inherited) ----
    String specialParsys = CONTENT_ROOT_PATH + "/page-2/jcr:content/special";
    Set<String> allowedComponents = underTest.getAllowedComponents(specialParsys, context.resourceResolver());

    // null check
    assertNotNull(allowedComponents,
        "Allowed component for parsys cannot be null");

    // positive tests
    assertTrue(allowedComponents.contains("dummy/components/specialComp1"),
        "Component 'specialComp1' must be allowed in " + specialParsys + ".");
    assertTrue(allowedComponents.contains("dummy/components/specialText"),
        "Component 'specialText' must be allowed in " + specialParsys + ".");

    // negative tests
    assertFalse(allowedComponents.contains("dummy/components/comp1"),
        "Component 'comp1' should not be allowed in " + specialParsys + ".");
    assertFalse(allowedComponents.contains("dummy/components/nestedComp2"),
        "Component 'nestedComp2' should not be allowed in " + specialParsys + ".");

    // ---- content parsys (inherited) ----
    String contentParsys = CONTENT_ROOT_PATH + "/page-2/jcr:content/content";
    allowedComponents = underTest.getAllowedComponents(contentParsys, context.resourceResolver());

    // null check
    assertNotNull(allowedComponents,
        "Allowed component for parsys cannot be null");

    // positive tests (inherited parsys config)
    assertTrue(allowedComponents.contains("dummy/components/comp1"),
        "Component 'comp1' must be allowed in " + contentParsys + ".");
    assertTrue(allowedComponents.contains("dummy/components/comp2"),
        "Component 'comp2' must be allowed in " + contentParsys + ".");
    assertTrue(allowedComponents.contains("dummy/components/comp2a"),
        "Component 'comp2a' must be allowed in " + contentParsys + ".");
    assertTrue(allowedComponents.contains("dummy/components/linklist"),
        "Component 'linklist' must be allowed in " + contentParsys + ".");

    // negative tests (inherited parsys config)
    assertFalse(allowedComponents.contains("dummy/components/nestedComp2"),
        "Component 'nestedComp2' should not be allowed in " + contentParsys + ".");
    assertFalse(allowedComponents.contains("dummy/components/container2col"),
        "Component 'container2col' must not be allowed in " + contentParsys + ".");
    assertFalse(allowedComponents.contains("dummy/components/comp3"),
        "Component 'comp3' should not be allowed in " + contentParsys + ".");

    // ---- linklist (inheritance canceled) ----
    String linklistParsys = CONTENT_ROOT_PATH + "/page-2/jcr:content/content/links";
    allowedComponents = underTest.getAllowedComponents(linklistParsys, context.resourceResolver());

    // positive tests
    assertTrue(allowedComponents.contains("dummy/components/comp2b"),
        "Component 'comp2b' must be allowed in " + linklistParsys + ".");

    // negative tests
    assertFalse(allowedComponents.contains("dummy/components/linkItem"),
        "Component 'linkItem' should not be allowed in " + linklistParsys + ".");

  }

  @Test
  void testGetAllowedComponentsForInheritedTemplate() {
    Set<String> allowedComponents = underTest.getAllowedComponentsForTemplate(INHERITED_PAGE_COMPONENT, context.resourceResolver());

    // null check
    assertNotNull(allowedComponents,
        "Allowed components for template cannot be null");

    // positive tests
    assertTrue(allowedComponents.contains("dummy/components/specialComp1"),
        "Component 'specialComp1' must be allowed in page " + INHERITED_PAGE_COMPONENT + ".");

    // positive tests (inherited parsys config)
    assertTrue(allowedComponents.contains("dummy/components/comp1"),
        "Component 'comp1' must be allowed in page " + INHERITED_PAGE_COMPONENT + ".");
    assertTrue(allowedComponents.contains("dummy/components/nestedComp2"),
        "Component 'nestedComp2' must be allowed in page " + INHERITED_PAGE_COMPONENT + ".");

    // negative tests
    assertFalse(allowedComponents.contains("dummy/components/comp3"),
        "Component 'comp3' should not be allowed in page " + INHERITED_PAGE_COMPONENT + ".");

  }

  @Test
  void testGetAllowedComponentsForParsys_NonexistingResource_ResourceType_Ancestor1() {
    String relativePath = "jcr:content/nonExistingResource";
    Set<String> allowedComponents = underTest.getAllowedComponents(basePage, relativePath,
        "dummy/components/parentWithAncestorLevel1", context.resourceResolver());

    // null check
    assertNotNull(allowedComponents,
        "Allowed component for parsys cannot be null");

    // positive tests
    assertTrue(allowedComponents.contains("dummy/components/nestedComp1"),
        "Component 'nestedComp1' must be allowed in " + relativePath + ".");
    assertTrue(allowedComponents.contains("dummy/components/nestedComp2"),
        "Component 'nestedComp2' must be allowed in " + relativePath + ".");
    assertTrue(allowedComponents.contains("dummy/components/nestedComp3"),
        "Component 'nestedComp3' must be allowed in " + relativePath + ".");

    // negative tests
    assertFalse(allowedComponents.contains("dummy/components/comp1"),
        "Component 'comp1' should not be allowed in " + relativePath + ".");
  }

  @Test
  void testGetAllowedComponentsForParsys_NonexistingResource_ResourceType_NoAncestor() {
    String relativePath = "jcr:content/nonExistingResource";
    Set<String> allowedComponents = underTest.getAllowedComponents(basePage, relativePath,
        "dummy/components/parentWithoutAncestorLevel", context.resourceResolver());

    // null check
    assertNotNull(allowedComponents,
        "Allowed component for parsys cannot be null");

    // positive tests
    assertTrue(allowedComponents.contains("dummy/components/nestedComp1"),
        "Component 'nestedComp1' must be allowed in " + relativePath + ".");
    assertTrue(allowedComponents.contains("dummy/components/nestedComp2"),
        "Component 'nestedComp2' must be allowed in " + relativePath + ".");

    // negative tests
    assertFalse(allowedComponents.contains("dummy/components/comp1"),
        "Component 'comp1' should not be allowed in " + relativePath + ".");
    assertFalse(allowedComponents.contains("dummy/components/nestedComp3"),
        "Component 'nestedComp3' should not be allowed in " + relativePath + ".");
  }


  private void addDummyContent(Page page) {
    String contentPath = page.getContentResource().getPath();

    // content parsys
    context.create().resource(contentPath + "/content",
        ImmutableValueMap.of(PROPERTY_RESOURCE_TYPE, "dummy/components/parsys"));

    // 2col-container (nested parsys)
    context.create().resource(contentPath + "/content/2colContainer",
        ImmutableValueMap.of(PROPERTY_RESOURCE_TYPE, "dummy/components/container2Col"));
    context.create().resource(contentPath + "/content/2colContainer/items");

    // link list (nested parsys)
    context.create().resource(contentPath + "/content/linklist",
        ImmutableValueMap.of(PROPERTY_RESOURCE_TYPE, "dummy/components/linklist"));
    context.create().resource(contentPath + "/content/linklist/links");
  }

}
