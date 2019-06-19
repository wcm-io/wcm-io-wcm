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
package io.wcm.wcm.ui.clientlibs.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import org.apache.sling.api.resource.PersistenceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CSSIncludeTest extends AbstractIncludeTest {

  @Test
  void testSingle() {
    context.request().setAttribute("categories", CATEGORY_SINGLE);
    CSSInclude underTest = AdaptTo.notNull(context.request(), CSSInclude.class);
    assertEquals("<link rel=\"stylesheet\" href=\"/etc/clientlibs/app1/clientlib1.min.css\" type=\"text/css\">\n",
        underTest.getInclude());
  }

  @Test
  void testSingleUnminified() {
    when(htmlLibraryManager.isMinifyEnabled()).thenReturn(false);
    context.request().setAttribute("categories", CATEGORY_SINGLE);
    CSSInclude underTest = AdaptTo.notNull(context.request(), CSSInclude.class);
    assertEquals("<link rel=\"stylesheet\" href=\"/etc/clientlibs/app1/clientlib1.css\" type=\"text/css\">\n",
        underTest.getInclude());
  }

  @Test
  void testSingleProxy() {
    context.request().setAttribute("categories", CATEGORY_SINGLE_PROXY);
    CSSInclude underTest = AdaptTo.notNull(context.request(), CSSInclude.class);
    assertEquals("<link rel=\"stylesheet\" href=\"/etc.clientlibs/app1/clientlibs/clientlib2_proxy.min.css\" type=\"text/css\">\n",
        underTest.getInclude());
  }

  @Test
  void testMulti() {
    context.request().setAttribute("categories", CATEGORIES_MULTIPLE);
    CSSInclude underTest = AdaptTo.notNull(context.request(), CSSInclude.class);
    assertEquals("<link rel=\"stylesheet\" href=\"/etc/clientlibs/app1/clientlib3.min.css\" type=\"text/css\">\n"
        + "<link rel=\"stylesheet\" href=\"/etc.clientlibs/app1/clientlibs/clientlib4_proxy.min.css\" type=\"text/css\">\n"
        + "<link rel=\"stylesheet\" href=\"/etc.clientlibs/app1/clientlibs/clientlib5_proxy.min.css\" type=\"text/css\">\n",
        underTest.getInclude());
  }

  @Test
  void testInvalid() {
    context.request().setAttribute("categories", CATEGORY_INVALID);
    CSSInclude underTest = AdaptTo.notNull(context.request(), CSSInclude.class);
    assertNull(underTest.getInclude());
  }

  @Test
  void testMissingCategoryNoAttributes() {
    CSSInclude underTest = AdaptTo.notNull(context.request(), CSSInclude.class);
    assertNull(underTest.getInclude());
  }

  @Test
  @SuppressWarnings("null")
  void testSingleNotAccessible() throws PersistenceException {
    context.request().setAttribute("categories", CATEGORY_SINGLE);
    // simulate non-accessible resource
    context.resourceResolver().delete(context.resourceResolver().getResource("/etc/clientlibs/app1/clientlib1"));
    CSSInclude underTest = AdaptTo.notNull(context.request(), CSSInclude.class);
    assertNull(underTest.getInclude());
  }

}
