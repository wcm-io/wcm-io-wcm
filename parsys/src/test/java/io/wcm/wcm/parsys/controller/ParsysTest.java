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
package io.wcm.wcm.parsys.controller;

import static io.wcm.testing.mock.wcmio.sling.ContextPlugins.WCMIO_SLING;
import static io.wcm.wcm.parsys.ParsysNameConstants.PN_PARSYS_GENERATE_DEAFULT_CSS;
import static io.wcm.wcm.parsys.ParsysNameConstants.PN_PARSYS_NEWAREA_CSS;
import static io.wcm.wcm.parsys.ParsysNameConstants.PN_PARSYS_PARAGRAPH_CSS;
import static io.wcm.wcm.parsys.ParsysNameConstants.PN_PARSYS_PARAGRAPH_ELEMENT;
import static io.wcm.wcm.parsys.ParsysNameConstants.PN_PARSYS_PARAGRAPH_NODECORATION_WCMMODE;
import static io.wcm.wcm.parsys.ParsysNameConstants.PN_PARSYS_WRAPPER_CSS;
import static io.wcm.wcm.parsys.ParsysNameConstants.PN_PARSYS_WRAPPER_ELEMENT;
import static io.wcm.wcm.parsys.controller.Parsys.DEFAULT_ELEMENT_NAME;
import static io.wcm.wcm.parsys.controller.Parsys.FALLBACK_NEWAREA_RESOURCE_TYPE;
import static io.wcm.wcm.parsys.controller.Parsys.NEWAREA_CHILD_NAME;
import static io.wcm.wcm.parsys.controller.Parsys.NEWAREA_CSS_CLASS_NAME;
import static io.wcm.wcm.parsys.controller.Parsys.NEWAREA_RESOURCE_PATH;
import static io.wcm.wcm.parsys.controller.Parsys.NEWAREA_STYLE;
import static io.wcm.wcm.parsys.controller.Parsys.RA_PARSYS_PARENT_RESOURCE;
import static io.wcm.wcm.parsys.controller.Parsys.SECTION_DEFAULT_CLASS_NAME;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.sling.api.SlingConstants;
import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import com.adobe.cq.export.json.SlingModelFilter;
import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.WCMMode;

import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextBuilder;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.commons.component.impl.ComponentPropertyResolverFactoryImpl;
import io.wcm.wcm.parsys.controller.Parsys.Item;

@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
class ParsysTest {

  private static final String RESOURCE_TYPE_SAMPLE = "sample/components/parsys";
  private static final String SUPER_RESOURCE_TYPE_SAMPLE = "sample/components/superParsys";
  private static final String COMPONENT_PATH_1 = "sample/components/comp1";
  private static final String COMPONENT_PATH_2 = "sample/components/comp2";
  private static final String SUPERCOMPONENT_PATH = "sample/components/super";

  private final AemContext context = new AemContextBuilder()
      .plugin(WCMIO_SLING)
      .registerSlingModelsFromClassPath(false)
      .build();

  private Page page;
  private Resource parsysResource;
  private Resource par1Resource;
  private Resource par2Resource;

  @Mock(lenient = true)
  private SlingModelFilter slingModelFilter;

  @BeforeEach
  void setUp() {
    context.registerInjectActivateService(new ComponentPropertyResolverFactoryImpl());

    context.registerService(SlingModelFilter.class, slingModelFilter);
    when(slingModelFilter.filterChildResources(any())).then(new Answer<Iterable<Resource>>() {
      @Override
      public Iterable<Resource> answer(InvocationOnMock invocation) throws Throwable {
        return invocation.getArgument(0);
      }
    });

    context.addModelsForClasses(Parsys.class);

    page = context.create().page("/content/page1", "sample/templates/test1");
    parsysResource = context.create().resource(page.getContentResource().getPath() + "/parsys",
        "sling:resourceType", RESOURCE_TYPE_SAMPLE);
    par1Resource = context.create().resource(parsysResource.getPath() + "/par1",
        "sling:resourceType", COMPONENT_PATH_1,
        "valid", true);
    par2Resource = context.create().resource(parsysResource.getPath() + "/par2",
        "sling:resourceType", COMPONENT_PATH_2,
        "valid", false);

    context.currentResource(parsysResource);
  }

  @Test
  void testEditMode() {
    context.create().resource("/apps/" + RESOURCE_TYPE_SAMPLE);

    WCMMode.EDIT.toRequest(context.request());
    Parsys parsys = AdaptTo.notNull(context.request(), Parsys.class);

    assertFalse(parsys.isWrapperElement());
    assertNull(parsys.getWrapperCss());
    assertEquals(DEFAULT_ELEMENT_NAME, parsys.getWrapperElementName());

    List<Item> items = parsys.getItems();
    assertEquals(3, items.size());

    Item item1 = items.get(0);
    assertEquals(par1Resource.getPath(), item1.getResourcePath());
    assertNull(item1.getResourceType());
    assertNull(item1.getStyle());
    assertEquals(SECTION_DEFAULT_CLASS_NAME, item1.getCssClassName());
    assertEquals(DEFAULT_ELEMENT_NAME, item1.getElementName());
    assertFalse(item1.isNewArea());

    Item item2 = items.get(1);
    assertEquals(par2Resource.getPath(), item2.getResourcePath());
    assertNull(item2.getResourceType());
    assertNull(item2.getStyle());
    assertEquals(SECTION_DEFAULT_CLASS_NAME, item2.getCssClassName());
    assertEquals(DEFAULT_ELEMENT_NAME, item2.getElementName());
    assertFalse(item2.isNewArea());

    Item item3 = items.get(2);
    assertEquals(NEWAREA_RESOURCE_PATH, item3.getResourcePath());
    assertEquals(FALLBACK_NEWAREA_RESOURCE_TYPE, item3.getResourceType());
    assertEquals(NEWAREA_STYLE, item3.getStyle());
    assertEquals(NEWAREA_CSS_CLASS_NAME + " " + SECTION_DEFAULT_CLASS_NAME, item3.getCssClassName());
    assertEquals(DEFAULT_ELEMENT_NAME, item3.getElementName());
    assertTrue(item3.isNewArea());

    assertEquals(RESOURCE_TYPE_SAMPLE, parsys.getExportedType());
    assertArrayEquals(new String[] { par1Resource.getName(), par2Resource.getName() }, parsys.getExportedItemsOrder());
  }

  @Test
  void testEditMode_Customized() {
    context.create().resource("/apps/" + RESOURCE_TYPE_SAMPLE,
        PN_PARSYS_GENERATE_DEAFULT_CSS, false,
        PN_PARSYS_PARAGRAPH_CSS, "paracss",
        PN_PARSYS_NEWAREA_CSS, "newareacss",
        PN_PARSYS_PARAGRAPH_ELEMENT, "li",
        PN_PARSYS_WRAPPER_ELEMENT, "ul",
        PN_PARSYS_WRAPPER_CSS, "wrappercss");

    WCMMode.EDIT.toRequest(context.request());
    Parsys parsys = AdaptTo.notNull(context.request(), Parsys.class);

    assertTrue(parsys.isWrapperElement());
    assertEquals("wrappercss", parsys.getWrapperCss());
    assertEquals("ul", parsys.getWrapperElementName());

    List<Item> items = parsys.getItems();
    assertEquals(3, items.size());

    Item item1 = items.get(0);
    assertEquals(par1Resource.getPath(), item1.getResourcePath());
    assertNull(item1.getResourceType());
    assertNull(item1.getStyle());
    assertEquals("paracss", item1.getCssClassName());
    assertEquals("li", item1.getElementName());
    assertFalse(item1.isNewArea());

    Item item2 = items.get(1);
    assertEquals(par2Resource.getPath(), item2.getResourcePath());
    assertNull(item2.getResourceType());
    assertNull(item2.getStyle());
    assertEquals("paracss", item2.getCssClassName());
    assertEquals("li", item2.getElementName());
    assertFalse(item2.isNewArea());

    Item item3 = items.get(2);
    assertEquals(NEWAREA_RESOURCE_PATH, item3.getResourcePath());
    assertEquals(FALLBACK_NEWAREA_RESOURCE_TYPE, item3.getResourceType());
    assertNull(item3.getStyle());
    assertEquals(NEWAREA_CSS_CLASS_NAME + " newareacss", item3.getCssClassName());
    assertEquals("li", item3.getElementName());
    assertTrue(item3.isNewArea());
  }

  @Test
  void testEditMode_Customized_Inheritance() {
    context.create().resource("/apps/" + SUPERCOMPONENT_PATH,
        PN_PARSYS_GENERATE_DEAFULT_CSS, false,
        PN_PARSYS_PARAGRAPH_CSS, "paracss",
        PN_PARSYS_NEWAREA_CSS, "newareacss",
        PN_PARSYS_PARAGRAPH_ELEMENT, "li",
        PN_PARSYS_WRAPPER_ELEMENT, "ul",
        PN_PARSYS_WRAPPER_CSS, "wrappercss");
    context.create().resource("/apps/" + RESOURCE_TYPE_SAMPLE,
        "sling:resourceSuperType", SUPERCOMPONENT_PATH);

    WCMMode.EDIT.toRequest(context.request());
    Parsys parsys = AdaptTo.notNull(context.request(), Parsys.class);

    assertTrue(parsys.isWrapperElement());
    assertEquals("wrappercss", parsys.getWrapperCss());
    assertEquals("ul", parsys.getWrapperElementName());

    List<Item> items = parsys.getItems();
    assertEquals(3, items.size());

    Item item1 = items.get(0);
    assertEquals(par1Resource.getPath(), item1.getResourcePath());
    assertNull(item1.getResourceType());
    assertNull(item1.getStyle());
    assertEquals("paracss", item1.getCssClassName());
    assertEquals("li", item1.getElementName());
    assertFalse(item1.isNewArea());

    Item item2 = items.get(1);
    assertEquals(par2Resource.getPath(), item2.getResourcePath());
    assertNull(item2.getResourceType());
    assertNull(item2.getStyle());
    assertEquals("paracss", item2.getCssClassName());
    assertEquals("li", item2.getElementName());
    assertFalse(item2.isNewArea());

    Item item3 = items.get(2);
    assertEquals(NEWAREA_RESOURCE_PATH, item3.getResourcePath());
    assertEquals(FALLBACK_NEWAREA_RESOURCE_TYPE, item3.getResourceType());
    assertNull(item3.getStyle());
    assertEquals(NEWAREA_CSS_CLASS_NAME + " newareacss", item3.getCssClassName());
    assertEquals("li", item3.getElementName());
    assertTrue(item3.isNewArea());
  }

  @Test
  void testWcmDisabledMode() {
    context.create().resource("/apps/" + RESOURCE_TYPE_SAMPLE);
    WCMMode.DISABLED.toRequest(context.request());
    Parsys parsys = AdaptTo.notNull(context.request(), Parsys.class);

    List<Item> items = parsys.getItems();
    assertEquals(2, items.size());

    Item item1 = items.get(0);
    assertEquals(par1Resource.getPath(), item1.getResourcePath());
    assertNull(item1.getResourceType());
    assertEquals(SECTION_DEFAULT_CLASS_NAME, item1.getCssClassName());
    assertFalse(item1.isNewArea());

    Item item2 = items.get(1);
    assertEquals(par2Resource.getPath(), item2.getResourcePath());
    assertNull(item2.getResourceType());
    assertEquals(SECTION_DEFAULT_CLASS_NAME, item2.getCssClassName());
    assertFalse(item2.isNewArea());
  }

  @Test
  void testNewAreaResourceTypeFromCurrentComponent() {
    context.create().resource("/apps/" + RESOURCE_TYPE_SAMPLE);
    context.create().resource("/apps/" + RESOURCE_TYPE_SAMPLE + "/" + NEWAREA_CHILD_NAME);

    WCMMode.EDIT.toRequest(context.request());
    Parsys parsys = AdaptTo.notNull(context.request(), Parsys.class);
    List<Item> items = parsys.getItems();

    Item item3 = items.get(2);
    assertEquals("/apps/" + RESOURCE_TYPE_SAMPLE + "/" + NEWAREA_CHILD_NAME, item3.getResourceType());
  }

  @Test
  void testNewAreaResourceTypeFromSuperComponent() {
    context.create().resource("/apps/" + RESOURCE_TYPE_SAMPLE,
        SlingConstants.NAMESPACE_PREFIX + ":" + SlingConstants.PROPERTY_RESOURCE_SUPER_TYPE, SUPER_RESOURCE_TYPE_SAMPLE);

    context.create().resource("/apps/" + SUPER_RESOURCE_TYPE_SAMPLE);
    context.create().resource("/apps/" + SUPER_RESOURCE_TYPE_SAMPLE + "/" + NEWAREA_CHILD_NAME);

    WCMMode.EDIT.toRequest(context.request());
    Parsys parsys = AdaptTo.notNull(context.request(), Parsys.class);
    List<Item> items = parsys.getItems();

    Item item3 = items.get(2);
    assertEquals(SUPER_RESOURCE_TYPE_SAMPLE + "/" + NEWAREA_CHILD_NAME, item3.getResourceType());
  }

  @Test
  void testOtherParentParsysResource() {
    context.create().resource("/apps/" + RESOURCE_TYPE_SAMPLE);
    parsysResource = context.create().resource(page.getContentResource().getPath() + "/parsysOther");
    par1Resource = context.create().resource(parsysResource.getPath() + "/par1");

    context.request().setAttribute(RA_PARSYS_PARENT_RESOURCE, parsysResource);

    WCMMode.EDIT.toRequest(context.request());
    Parsys parsys = AdaptTo.notNull(context.request(), Parsys.class);

    List<Item> items = parsys.getItems();
    assertEquals(2, items.size());

    Item item1 = items.get(0);
    assertEquals(par1Resource.getPath(), item1.getResourcePath());
    assertNull(item1.getResourceType());
    assertEquals(SECTION_DEFAULT_CLASS_NAME, item1.getCssClassName());
    assertFalse(item1.isNewArea());

    Item item2 = items.get(1);
    assertEquals(NEWAREA_RESOURCE_PATH, item2.getResourcePath());
    assertEquals(FALLBACK_NEWAREA_RESOURCE_TYPE, item2.getResourceType());
    assertEquals(NEWAREA_CSS_CLASS_NAME + " " + SECTION_DEFAULT_CLASS_NAME, item2.getCssClassName());
    assertTrue(item2.isNewArea());
  }

  @Test
  void testComponentWithTagDecoration() {
    context.create().resource("/apps/" + RESOURCE_TYPE_SAMPLE);

    // prepare tag decoration for one component
    context.create().resource("/apps/sample/components/comp1/" + NameConstants.NN_HTML_TAG,
        NameConstants.PN_TAG_NAME, "article", "class", "css1");

    WCMMode.EDIT.toRequest(context.request());
    Parsys parsys = AdaptTo.notNull(context.request(), Parsys.class);

    List<Item> items = parsys.getItems();
    assertEquals(3, items.size());

    Item item1 = items.get(0);
    assertEquals(par1Resource.getPath(), item1.getResourcePath());
    assertNull(item1.getResourceType());
    assertEquals("css1 section", item1.getCssClassName());
    assertEquals("article", item1.getElementName());
    assertTrue(item1.isDecorate());
    assertFalse(item1.isNewArea());

    Item item2 = items.get(1);
    assertEquals(par2Resource.getPath(), item2.getResourcePath());
    assertNull(item2.getResourceType());
    assertEquals(SECTION_DEFAULT_CLASS_NAME, item2.getCssClassName());
    assertEquals(DEFAULT_ELEMENT_NAME, item2.getElementName());
    assertTrue(item2.isDecorate());
    assertFalse(item2.isNewArea());

    Item item3 = items.get(2);
    assertEquals(NEWAREA_RESOURCE_PATH, item3.getResourcePath());
    assertEquals(FALLBACK_NEWAREA_RESOURCE_TYPE, item3.getResourceType());
    assertEquals(NEWAREA_CSS_CLASS_NAME + " " + SECTION_DEFAULT_CLASS_NAME, item3.getCssClassName());
    assertEquals(DEFAULT_ELEMENT_NAME, item3.getElementName());
    assertTrue(item3.isDecorate());
    assertTrue(item3.isNewArea());
  }

  @Test
  void testComponentWithNoTagDecoration() {
    context.create().resource("/apps/" + RESOURCE_TYPE_SAMPLE,
        PN_PARSYS_PARAGRAPH_NODECORATION_WCMMODE, new String[] { "edit" });

    // prepare tag decoration for one component
    context.create().resource("/apps/sample/components/comp1/" + NameConstants.NN_HTML_TAG,
        NameConstants.PN_TAG_NAME, "article", "class", "css1");

    WCMMode.EDIT.toRequest(context.request());
    Parsys parsys = AdaptTo.notNull(context.request(), Parsys.class);

    List<Item> items = parsys.getItems();
    assertEquals(3, items.size());

    Item item1 = items.get(0);
    assertEquals(par1Resource.getPath(), item1.getResourcePath());
    assertNull(item1.getResourceType());
    assertEquals("css1 section", item1.getCssClassName());
    assertEquals("article", item1.getElementName());
    assertFalse(item1.isDecorate());
    assertFalse(item1.isNewArea());

    Item item2 = items.get(1);
    assertEquals(par2Resource.getPath(), item2.getResourcePath());
    assertNull(item2.getResourceType());
    assertEquals(SECTION_DEFAULT_CLASS_NAME, item2.getCssClassName());
    assertEquals(DEFAULT_ELEMENT_NAME, item2.getElementName());
    assertFalse(item2.isDecorate());
    assertFalse(item2.isNewArea());

    Item item3 = items.get(2);
    assertEquals(NEWAREA_RESOURCE_PATH, item3.getResourcePath());
    assertEquals(FALLBACK_NEWAREA_RESOURCE_TYPE, item3.getResourceType());
    assertEquals(NEWAREA_CSS_CLASS_NAME + " " + SECTION_DEFAULT_CLASS_NAME, item3.getCssClassName());
    assertEquals(DEFAULT_ELEMENT_NAME, item3.getElementName());
    assertTrue(item3.isDecorate());
    assertTrue(item3.isNewArea());
  }

  // --- following tests can be tested (and compiled) only with profile "test-with-latest-sling-models" ---
  //     (they require Sling Models API 1.3.0 or higher)
  /*
  @Test
  void testParagraphValidate_DisabledMode() {
    context.addModelsForClasses(Parsys.class, ParsysItemModel.class);

    context.create().resource("/apps/" + RESOURCE_TYPE_SAMPLE,
        PN_PARSYS_PARAGRAPH_VALIDATE, true);

    WCMMode.DISABLED.toRequest(context.request());
    Parsys parsys = AdaptTo.notNull(context.request(), Parsys.class);

    List<Item> items = parsys.getItems();
    assertEquals(1, items.size());

    Item item1 = items.get(0);
    assertEquals(par1Resource.getPath(), item1.getResourcePath());
    assertTrue(item1.isValid());
  }

  @Test
  void testParagraphValidate_DisabledMode_NoAdapter() {
    context.create().resource("/apps/" + RESOURCE_TYPE_SAMPLE,
        PN_PARSYS_PARAGRAPH_VALIDATE, true);

    WCMMode.DISABLED.toRequest(context.request());
    Parsys parsys = AdaptTo.notNull(context.request(), Parsys.class);

    List<Item> items = parsys.getItems();
    assertEquals(2, items.size());

    Item item1 = items.get(0);
    assertEquals(par1Resource.getPath(), item1.getResourcePath());
    assertTrue(item1.isValid());

    Item item2 = items.get(1);
    assertEquals(par2Resource.getPath(), item2.getResourcePath());
    assertTrue(item2.isValid());
  }

  @Test
  void testParagraphValidate_EditMode() {
    context.addModelsForClasses(Parsys.class, ParsysItemModel.class);

    context.create().resource("/apps/" + RESOURCE_TYPE_SAMPLE,
        PN_PARSYS_PARAGRAPH_VALIDATE, true);

    WCMMode.EDIT.toRequest(context.request());
    Parsys parsys = AdaptTo.notNull(context.request(), Parsys.class);

    List<Item> items = parsys.getItems();
    assertEquals(3, items.size());

    Item item1 = items.get(0);
    assertEquals(par1Resource.getPath(), item1.getResourcePath());
    assertTrue(item1.isValid());

    Item item2 = items.get(1);
    assertEquals(par2Resource.getPath(), item2.getResourcePath());
    assertFalse(item2.isValid());

    Item item3 = items.get(2);
    assertEquals(NEWAREA_RESOURCE_PATH, item3.getResourcePath());
    assertFalse(item3.isValid());
  }


  @Model(adaptables = Resource.class,
      adapters = { ParsysItemModel.class, ParsysItem.class },
      resourceType = { COMPONENT_PATH_1, COMPONENT_PATH_2 })
  public static class ParsysItemModel implements ParsysItem {

    @ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
    private boolean valid;

    @Override
    public boolean isValid() {
      return valid;
    }

  }
  */

}
