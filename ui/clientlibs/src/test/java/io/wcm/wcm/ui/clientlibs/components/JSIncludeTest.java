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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;

import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.xss.XSSAPI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;

import com.adobe.granite.ui.clientlibs.ClientLibrary;
import com.adobe.granite.ui.clientlibs.HtmlLibraryManager;
import com.adobe.granite.ui.clientlibs.LibraryType;
import com.google.common.collect.ImmutableList;

import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JSIncludeTest {

  private final AemContext context = new AemContext();

  private static final String CATEGORY_SINGLE = "single";
  private static final String CATEGORY_SINGLE_PROXY = "single_proxy";
  private static final String CATEGORY_INVALID = "invalid";
  private static final String[] CATEGORIES_MULTIPLE = new String[] { "multi1", "multi2" };

  @Mock
  private HtmlLibraryManager htmlLibraryManager;
  @Mock
  private XSSAPI xssApi;

  @BeforeEach
  void setUp() {
    context.registerService(HtmlLibraryManager.class, htmlLibraryManager);

    Collection<ClientLibrary> single = ImmutableList.of(clientlib("/etc/clientlibs/app1/clientlib1", false));
    Collection<ClientLibrary> singleProxy = ImmutableList.of(clientlib("/apps/app1/clientlibs/clientlib2_proxy", true));
    Collection<ClientLibrary> multiple = ImmutableList.of(clientlib("/etc/clientlibs/app1/clientlib3", false),
        clientlib("/apps/app1/clientlibs/clientlib4_proxy", true),
        clientlib("/libs/app1/clientlibs/clientlib5_proxy", true));

    when(htmlLibraryManager.getLibraries(new String[] { CATEGORY_SINGLE }, LibraryType.JS, false, false)).thenReturn(single);
    when(htmlLibraryManager.getLibraries(new String[] { CATEGORY_SINGLE_PROXY }, LibraryType.JS, false, false)).thenReturn(singleProxy);
    when(htmlLibraryManager.getLibraries(CATEGORIES_MULTIPLE, LibraryType.JS, false, false)).thenReturn(multiple);
    when(htmlLibraryManager.isMinifyEnabled()).thenReturn(true);

    context.registerService(XSSAPI.class, xssApi);
    when(xssApi.encodeForHTMLAttr(anyString())).thenAnswer(new Answer<String>() {
      @Override
      public String answer(InvocationOnMock invocation) throws Throwable {
        return invocation.getArgument(0);
      }
    });
  }

  private ClientLibrary clientlib(String path, boolean allowProxy) {
    @SuppressWarnings("null")
    ClientLibrary clientlib = mock(ClientLibrary.class);
    when(clientlib.getPath()).thenReturn(path);
    when(clientlib.getIncludePath(any(LibraryType.class), anyBoolean())).then(new Answer<String>() {
      @Override
      public String answer(InvocationOnMock invocation) throws Throwable {
        LibraryType libraryType = invocation.getArgument(0);
        boolean minify = invocation.getArgument(1);
        return path + (minify ? ".min" : "") + libraryType.extension;
      }
    });
    when(clientlib.allowProxy()).thenReturn(allowProxy);
    context.create().resource(path);
    return clientlib;
  }

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
    assertEquals("<script src=\"/etc/clientlibs/app1/clientlib3.min.js\"></script>\n" +
        "<script src=\"/etc.clientlibs/app1/clientlibs/clientlib4_proxy.min.js\"></script>\n" +
        "<script src=\"/etc.clientlibs/app1/clientlibs/clientlib5_proxy.min.js\"></script>\n", underTest.getInclude());
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
        + "async=\"true\" nomodule type=\"text/javascript\"></script>\n" +
        "<script src=\"/etc.clientlibs/app1/clientlibs/clientlib4_proxy.min.js\" "
        + "async=\"true\" nomodule type=\"text/javascript\"></script>\n" +
        "<script src=\"/etc.clientlibs/app1/clientlibs/clientlib5_proxy.min.js\" "
        + "async=\"true\" nomodule type=\"text/javascript\"></script>\n",
        underTest.getInclude());
  }

}
