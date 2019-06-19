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
class JSIncludeTest extends AbstractIncludeTest {

  @Test
  void testSingleNoAttributes() {
    context.request().setAttribute("categories", CATEGORY_SINGLE);
    JSInclude underTest = AdaptTo.notNull(context.request(), JSInclude.class);
    assertEquals("<script src=\"/etc/clientlibs/app1/clientlib1.min.js\"></script>\n", underTest.getInclude());
  }

  @Test
  void testSingleNoAttributesUnminified() {
    when(htmlLibraryManager.isMinifyEnabled()).thenReturn(false);
    context.request().setAttribute("categories", CATEGORY_SINGLE);
    JSInclude underTest = AdaptTo.notNull(context.request(), JSInclude.class);
    assertEquals("<script src=\"/etc/clientlibs/app1/clientlib1.js\"></script>\n", underTest.getInclude());
  }

  @Test
  void testSingleProxyNoAttributes() {
    context.request().setAttribute("categories", CATEGORY_SINGLE_PROXY);
    JSInclude underTest = AdaptTo.notNull(context.request(), JSInclude.class);
    assertEquals("<script src=\"/etc.clientlibs/app1/clientlibs/clientlib2_proxy.min.js\"></script>\n", underTest.getInclude());
  }

  @Test
  void testMultiNoAttributes() {
    context.request().setAttribute("categories", CATEGORIES_MULTIPLE);
    JSInclude underTest = AdaptTo.notNull(context.request(), JSInclude.class);
    assertEquals("<script src=\"/etc/clientlibs/app1/clientlib3.min.js\"></script>\n"
        + "<script src=\"/etc.clientlibs/app1/clientlibs/clientlib4_proxy.min.js\"></script>\n"
        + "<script src=\"/etc.clientlibs/app1/clientlibs/clientlib5_proxy.min.js\"></script>\n", underTest.getInclude());
  }

  @Test
  void testInvalidNoAttributes() {
    context.request().setAttribute("categories", CATEGORY_INVALID);
    JSInclude underTest = AdaptTo.notNull(context.request(), JSInclude.class);
    assertNull(underTest.getInclude());
  }

  @Test
  void testMissingCategoryNoAttributes() {
    JSInclude underTest = AdaptTo.notNull(context.request(), JSInclude.class);
    assertNull(underTest.getInclude());
  }

  @Test
  @SuppressWarnings("null")
  void testSingleNotAccessible() throws PersistenceException {
    context.request().setAttribute("categories", CATEGORY_SINGLE);
    // simulate non-accessible resource
    context.resourceResolver().delete(context.resourceResolver().getResource("/etc/clientlibs/app1/clientlib1"));
    JSInclude underTest = AdaptTo.notNull(context.request(), JSInclude.class);
    assertNull(underTest.getInclude());
  }

  @Test
  void testSingleValidAttributes() {
    context.request().setAttribute("categories", CATEGORY_SINGLE);
    context.request().setAttribute("async", true);
    context.request().setAttribute("crossorigin", "anonymous");
    context.request().setAttribute("defer", true);
    context.request().setAttribute("integrity", "value1");
    context.request().setAttribute("nomodule", true);
    context.request().setAttribute("nonce", "value2");
    context.request().setAttribute("referrerpolicy", "no-referrer");
    context.request().setAttribute("type", "module");
    JSInclude underTest = AdaptTo.notNull(context.request(), JSInclude.class);
    assertEquals("<script src=\"/etc/clientlibs/app1/clientlib1.min.js\" "
        + "async=\"true\" crossorigin=\"anonymous\" defer=\"true\" integrity=\"value1\" "
        + "nomodule nonce=\"value2\" referrerpolicy=\"no-referrer\" type=\"module\">"
        + "</script>\n", underTest.getInclude());
  }

  @Test
  void testSingleInvalidAttributes() {
    context.request().setAttribute("categories", CATEGORY_SINGLE);
    context.request().setAttribute("async", false);
    context.request().setAttribute("crossorigin", "invalid-string");
    context.request().setAttribute("defer", false);
    context.request().setAttribute("integrity", null);
    context.request().setAttribute("nomodule", false);
    context.request().setAttribute("nonce", null);
    context.request().setAttribute("referrerpolicy", "invalid-string");
    context.request().setAttribute("type", "invalid-string");
    JSInclude underTest = AdaptTo.notNull(context.request(), JSInclude.class);
    assertEquals("<script src=\"/etc/clientlibs/app1/clientlib1.min.js\"></script>\n", underTest.getInclude());
  }

  @Test
  void testMultiAttributes() {
    context.request().setAttribute("categories", CATEGORIES_MULTIPLE);
    context.request().setAttribute("async", true);
    context.request().setAttribute("nomodule", true);
    context.request().setAttribute("type", "text/javascript");
    JSInclude underTest = AdaptTo.notNull(context.request(), JSInclude.class);
    assertEquals("<script src=\"/etc/clientlibs/app1/clientlib3.min.js\" "
        + "async=\"true\" nomodule type=\"text/javascript\"></script>\n"
        + "<script src=\"/etc.clientlibs/app1/clientlibs/clientlib4_proxy.min.js\" "
        + "async=\"true\" nomodule type=\"text/javascript\"></script>\n"
        + "<script src=\"/etc.clientlibs/app1/clientlibs/clientlib5_proxy.min.js\" "
        + "async=\"true\" nomodule type=\"text/javascript\"></script>\n",
        underTest.getInclude());
  }

}
