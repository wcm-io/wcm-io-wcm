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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;

import org.apache.sling.xss.XSSAPI;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.adobe.granite.ui.clientlibs.ClientLibrary;
import com.adobe.granite.ui.clientlibs.HtmlLibraryManager;
import com.adobe.granite.ui.clientlibs.LibraryType;
import com.google.common.collect.ImmutableList;

import io.wcm.testing.mock.aem.junit5.AemContext;

abstract class AbstractIncludeTest {

  protected final AemContext context = new AemContext();

  protected static final String CATEGORY_SINGLE = "single";
  protected static final String CATEGORY_SINGLE_PROXY = "single_proxy";
  protected static final String CATEGORY_INVALID = "invalid";
  protected static final String[] CATEGORIES_MULTIPLE = new String[] { "multi1", "multi2" };

  @Mock
  protected HtmlLibraryManager htmlLibraryManager;
  @Mock
  protected XSSAPI xssApi;

  @BeforeEach
  void setUp() {
    context.registerService(HtmlLibraryManager.class, htmlLibraryManager);

    Collection<ClientLibrary> single = ImmutableList.of(clientlib("/etc/clientlibs/app1/clientlib1", false));
    Collection<ClientLibrary> singleProxy = ImmutableList.of(clientlib("/apps/app1/clientlibs/clientlib2_proxy", true));
    Collection<ClientLibrary> multiple = ImmutableList.of(clientlib("/etc/clientlibs/app1/clientlib3", false),
        clientlib("/apps/app1/clientlibs/clientlib4_proxy", true),
        clientlib("/libs/app1/clientlibs/clientlib5_proxy", true));

    when(htmlLibraryManager.getLibraries(eq(new String[] { CATEGORY_SINGLE }), any(LibraryType.class), anyBoolean(), anyBoolean()))
        .thenReturn(single);
    when(htmlLibraryManager.getLibraries(eq(new String[] { CATEGORY_SINGLE_PROXY }), any(LibraryType.class), anyBoolean(), anyBoolean()))
        .thenReturn(singleProxy);
    when(htmlLibraryManager.getLibraries(eq(CATEGORIES_MULTIPLE), any(LibraryType.class), anyBoolean(), anyBoolean()))
        .thenReturn(multiple);
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

}
