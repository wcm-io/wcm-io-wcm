/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2015 wcm.io
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
package io.wcm.wcm.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;

import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.wcm.commons.testcontext.AppTemplate;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("null")
class TemplateTest {

  @Mock
  private Page page;

  @BeforeEach
  void setUp() {
    when(page.getProperties()).thenReturn(ImmutableValueMap.of(NameConstants.PN_TEMPLATE, AppTemplate.TEMPLATE_1.getTemplatePath()));
  }

  @Test
  void testGetResourceTypeFromTemplatePath() {

    assertNull(null, Template.getResourceTypeFromTemplatePath(null));
    assertNull(null, Template.getResourceTypeFromTemplatePath(""));
    assertNull(null, Template.getResourceTypeFromTemplatePath("/apps"));

    assertEquals("app1/components/page/t1", Template.getResourceTypeFromTemplatePath("/apps/app1/templates/t1"));
    assertEquals("app1/components/page/t1", Template.getResourceTypeFromTemplatePath("/libs/app1/templates/t1"));
    assertEquals("aaa/app1/components/bbb/page/t1", Template.getResourceTypeFromTemplatePath("/apps/aaa/app1/templates/bbb/t1"));
    assertEquals("aaa/ddd/app1/components/bbb/ccc/page/t1", Template.getResourceTypeFromTemplatePath("/apps/aaa/ddd/app1/templates/bbb/ccc/t1"));

  }

  @Test
  void testIs_TemplatePathInfo() {
    assertFalse(Template.is(null, new TemplatePathInfo[0]));
    assertFalse(Template.is(page, new TemplatePathInfo[0]));
    assertFalse(Template.is(page, (TemplatePathInfo[])null));

    assertTrue(Template.is(page, AppTemplate.TEMPLATE_1, AppTemplate.TEMPLATE_2));
    assertFalse(Template.is(page, AppTemplate.TEMPLATE_3));
  }

  @Test
  void testIs_TemplatePath() {
    assertFalse(Template.is(null, new String[0]));
    assertFalse(Template.is(page, new String[0]));
    assertFalse(Template.is(page, (String[])null));

    assertTrue(Template.is(page, AppTemplate.TEMPLATE_1.getTemplatePath(), AppTemplate.TEMPLATE_2.getTemplatePath()));
    assertFalse(Template.is(page, AppTemplate.TEMPLATE_3.getTemplatePath()));
  }

  @Test
  void testForTemplatePath() {
    assertEquals(AppTemplate.TEMPLATE_1, Template.forTemplatePath(AppTemplate.TEMPLATE_1.getTemplatePath(), AppTemplate.values()));
    assertNull(Template.forTemplatePath("/apps/xxx/templates/yyy", AppTemplate.values()));
  }

  @Test
  void testForTemplatePath_Enum() {
    assertEquals(AppTemplate.TEMPLATE_1, Template.forTemplatePath(AppTemplate.TEMPLATE_1.getTemplatePath(), AppTemplate.class));
    assertNull(Template.forTemplatePath("/apps/xxx/templates/yyy", AppTemplate.class));
  }

  @Test
  void testForPage() throws Exception {
    assertEquals(AppTemplate.TEMPLATE_1, Template.forPage(page, AppTemplate.values()));
    assertNull(Template.forPage(null, AppTemplate.values()));
  }

  @Test
  void testForPage_Enum() throws Exception {
    assertEquals(AppTemplate.TEMPLATE_1, Template.forPage(page, AppTemplate.class));
    assertNull(Template.forPage(null, AppTemplate.class));
  }
}
