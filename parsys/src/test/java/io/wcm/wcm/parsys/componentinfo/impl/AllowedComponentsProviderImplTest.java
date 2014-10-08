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

import static org.apache.sling.jcr.resource.JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.wcm.parsys.componentinfo.AllowedComponentsProvider;

import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.wcm.api.Page;

public class AllowedComponentsProviderImplTest {

  @Rule
  public AemContext context = new AemContext();

  private static final String CONTENT_ROOT_PATH = "/content/dummy";
  private static final String BASE_TEMPLATE = "/apps/dummy/templates/baseTemplate";
  private static final String BASE_PAGE_COMPONENT = "/apps/dummy/components/page/basePage";
  private static final String INHERITED_TEMPLATE = "/apps/dummy/templates/inheritedTemplate";
  private static final String INHERITED_PAGE_COMPONENT = "/apps/dummy/components/page/inheritedPage";

  private AllowedComponentsProvider underTest;

  @Before
  public void setUp() {

    context.registerInjectActivateService(new ParsysConfigManagerImpl());
    context.registerInjectActivateService(new AllowedComponentsProviderImpl());

    // mount template and page component definitions
    context.load().json("/parsys/baseTemplate.json", BASE_TEMPLATE);
    context.load().json("/parsys/basePageComponent.json", BASE_PAGE_COMPONENT);
    context.load().json("/parsys/inheritedTemplate.json", INHERITED_TEMPLATE);
    context.load().json("/parsys/inheritedPageComponent.json", INHERITED_PAGE_COMPONENT);

    // create pages with dummy content
    Page basePage = context.create().page(CONTENT_ROOT_PATH + "/page-1", BASE_TEMPLATE,
        ImmutableValueMap.of(SLING_RESOURCE_TYPE_PROPERTY, BASE_PAGE_COMPONENT));
    addDummyContent(basePage);

    Page inheritedPage = context.create().page(CONTENT_ROOT_PATH + "/page-2", INHERITED_TEMPLATE,
        ImmutableValueMap.of(SLING_RESOURCE_TYPE_PROPERTY, INHERITED_PAGE_COMPONENT));
    addDummyContent(inheritedPage);
    context.create().resource(inheritedPage.getContentResource().getPath() + "/special",
        ImmutableValueMap.of(SLING_RESOURCE_TYPE_PROPERTY, "/apps/dummy/components/parsys"));

    underTest = context.getService(AllowedComponentsProvider.class);
  }

  @Test
  public void testGetAllowedComponentsForParsys() {
    String contentParsys = CONTENT_ROOT_PATH + "/page-1/jcr:content/content";
    Set<String> allowedComponents = underTest.getAllowedComponents(contentParsys, context.resourceResolver());

    // null check
    assertNotNull("Allowed component for parsys cannot be null", allowedComponents);

    // positive tests
    assertTrue("Component 'comp1' must be allowed in " + contentParsys + ".",
        allowedComponents.contains("/apps/dummy/components/comp1"));
    assertTrue("Component 'comp2' must be allowed in " + contentParsys + ".",
        allowedComponents.contains("/apps/dummy/components/comp2"));
    assertTrue("Component 'linklist' must be allowed in " + contentParsys + ".",
        allowedComponents.contains("/apps/dummy/components/linklist"));
    assertTrue("Component 'container2col' must be allowed in " + contentParsys + ".",
        allowedComponents.contains("/apps/dummy/components/container2col"));

    // negative tests
    assertFalse("Component 'nestedComp2' should not be allowed in " + contentParsys + ".",
        allowedComponents.contains("/apps/dummy/components/nestedComp2"));
    assertFalse("Component 'comp3' should not be allowed in " + contentParsys + ".",
        allowedComponents.contains("/apps/dummy/components/comp3"));
  }

  @Test
  public void testGetAllowedComponentsForNestedParsys() {
    String nested2ColParsys = CONTENT_ROOT_PATH + "/page-1/jcr:content/content/2colContainer/items";
    Set<String> allowedComponents = underTest.getAllowedComponents(nested2ColParsys, context.resourceResolver());

    // null check
    assertNotNull("Allowed component for parsys cannot be null", allowedComponents);

    // positive tests
    assertTrue("Component 'nestedComp1' must be allowed in " + nested2ColParsys + ".",
        allowedComponents.contains("/apps/dummy/components/nestedComp1"));
    assertTrue("Component 'nestedComp2' must be allowed in " + nested2ColParsys + ".",
        allowedComponents.contains("/apps/dummy/components/nestedComp2"));
    assertTrue("Component 'linklist' must be allowed in " + nested2ColParsys + ".",
        allowedComponents.contains("/apps/dummy/components/linklist"));

    // negative tests
    assertFalse("Component 'comp1' should not be allowed in " + nested2ColParsys + ".",
        allowedComponents.contains("/apps/dummy/components/comp1"));
  }

  @Test
  public void testGetAllowedComponentsForNestedNestedParsys() {
    String linklist = CONTENT_ROOT_PATH + "/page-1/jcr:content/content/2colContainer/linklist/links";
    Set<String> allowedComponents = underTest.getAllowedComponents(linklist, context.resourceResolver());

    // null check
    assertNotNull("Allowed component for parsys cannot be null", allowedComponents);

    // positive tests
    assertTrue("Component 'linkItem' must be allowed in " + linklist + ".",
        allowedComponents.contains("/apps/dummy/components/linkItem"));

    // negative tests
    assertFalse("Component 'comp1' should not be allowed in " + linklist + ".",
        allowedComponents.contains("/apps/dummy/components/comp1"));
    assertFalse("Component 'nestedComp1' must be allowed in " + linklist + ".",
        allowedComponents.contains("/apps/dummy/components/nestedComp1"));
  }

  @Test
  public void testGetAllowedComponentsForTemplate() {
    Set<String> allowedComponents = underTest.getAllowedComponentsForTemplate(BASE_PAGE_COMPONENT, context.resourceResolver());

    // null check
    assertNotNull("Allowed components for template cannot be null", allowedComponents);

    // positive tests
    assertTrue("Component 'comp1' must be allowed in page " + BASE_PAGE_COMPONENT + ".",
        allowedComponents.contains("/apps/dummy/components/comp1"));
    assertTrue("Component 'nestedComp2' must be allowed in page " + BASE_PAGE_COMPONENT + ".",
        allowedComponents.contains("/apps/dummy/components/nestedComp2"));

    // negative tests
    assertFalse("Component 'comp3' should not be allowed in page " + BASE_PAGE_COMPONENT + ".",
        allowedComponents.contains("/apps/dummy/components/comp3"));

  }

  @Test
  public void testGetAllowedComponentsForInheritedParsys() {
    // ---- special parsys (not inherited) ----
    String specialParsys = CONTENT_ROOT_PATH + "/page-2/jcr:content/special";
    Set<String> allowedComponents = underTest.getAllowedComponents(specialParsys, context.resourceResolver());

    // null check
    assertNotNull("Allowed component for parsys cannot be null", allowedComponents);

    // positive tests
    assertTrue("Component 'specialComp1' must be allowed in " + specialParsys + ".",
        allowedComponents.contains("/apps/dummy/components/specialComp1"));
    assertTrue("Component 'specialText' must be allowed in " + specialParsys + ".",
        allowedComponents.contains("/apps/dummy/components/specialText"));

    // negative tests
    assertFalse("Component 'comp1' should not be allowed in " + specialParsys + ".",
        allowedComponents.contains("/apps/dummy/components/comp1"));
    assertFalse("Component 'nestedComp2' should not be allowed in " + specialParsys + ".",
        allowedComponents.contains("/apps/dummy/components/nestedComp2"));

    // ---- content parsys (inherited) ----
    String contentParsys = CONTENT_ROOT_PATH + "/page-2/jcr:content/content";
    Set<String> inheritedAllowedComponents = underTest.getAllowedComponents(contentParsys, context.resourceResolver());

    // null check
    assertNotNull("Allowed component for parsys cannot be null", inheritedAllowedComponents);

    // positive tests (inherited parsys config)
    assertTrue("Component 'comp1' must be allowed in " + contentParsys + ".",
        inheritedAllowedComponents.contains("/apps/dummy/components/comp1"));
    assertTrue("Component 'comp2' must be allowed in " + contentParsys + ".",
        inheritedAllowedComponents.contains("/apps/dummy/components/comp2"));
    assertTrue("Component 'linklist' must be allowed in " + contentParsys + ".",
        inheritedAllowedComponents.contains("/apps/dummy/components/linklist"));
    assertTrue("Component 'container2col' must be allowed in " + contentParsys + ".",
        inheritedAllowedComponents.contains("/apps/dummy/components/container2col"));

    // negative tests (inherited parsys config)
    assertFalse("Component 'nestedComp2' should not be allowed in " + contentParsys + ".",
        inheritedAllowedComponents.contains("/apps/dummy/components/nestedComp2"));
    assertFalse("Component 'comp3' should not be allowed in " + contentParsys + ".",
        inheritedAllowedComponents.contains("/apps/dummy/components/comp3"));

  }

  @Test
  public void testGetAllowedComponentsForInheritedTemplate() {
    Set<String> allowedComponents = underTest.getAllowedComponentsForTemplate(INHERITED_PAGE_COMPONENT, context.resourceResolver());

    // null check
    assertNotNull("Allowed components for template cannot be null", allowedComponents);

    // positive tests
    assertTrue("Component 'specialComp1' must be allowed in page " + INHERITED_PAGE_COMPONENT + ".",
        allowedComponents.contains("/apps/dummy/components/specialComp1"));

    // positive tests (inherited parsys config)
    assertTrue("Component 'comp1' must be allowed in page " + INHERITED_PAGE_COMPONENT + ".",
        allowedComponents.contains("/apps/dummy/components/comp1"));
    assertTrue("Component 'nestedComp2' must be allowed in page " + INHERITED_PAGE_COMPONENT + ".",
        allowedComponents.contains("/apps/dummy/components/nestedComp2"));

    // negative tests
    assertFalse("Component 'comp3' should not be allowed in page " + INHERITED_PAGE_COMPONENT + ".",
        allowedComponents.contains("/apps/dummy/components/comp3"));

  }

  private void addDummyContent(Page page) {
    String contentPath = page.getContentResource().getPath();

    // content parsys
    context.create().resource(contentPath + "/content",
        ImmutableValueMap.of(SLING_RESOURCE_TYPE_PROPERTY, "/apps/dummy/components/parsys"));

    // 2col-container (nested parsys )
    context.create().resource(contentPath + "/content/2colContainer",
        ImmutableValueMap.of(SLING_RESOURCE_TYPE_PROPERTY, "/apps/dummy/components/container2Col"));
    context.create().resource(contentPath + "/content/2colContainer/items");

    // link list (nested parsys)
    context.create().resource(contentPath + "/content/linklist",
        ImmutableValueMap.of(SLING_RESOURCE_TYPE_PROPERTY, "/apps/dummy/components/linklist"));
    context.create().resource(contentPath + "/content/linklist/links");
  }

}
