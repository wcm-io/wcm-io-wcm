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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletResponse;

import org.apache.sling.api.resource.ResourceResolver;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.day.cq.wcm.api.Page;
import com.google.common.collect.ImmutableSortedSet;

import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.wcm.commons.util.RunMode;
import io.wcm.wcm.parsys.componentinfo.AllowedComponentsProvider;

@RunWith(MockitoJUnitRunner.class)
public class ParsysComponentsServletTest {

  private static final String PAGE_PATH = "/content/sample/page1";
  private static final String LOCAL_PATH = "jcr:content/sample";

  @Rule
  public AemContext context = new AemContext();

  @Mock
  private AllowedComponentsProvider allowedComponentsProvider;

  private Page currentPage;

  @Before
  public void setUp() {
    currentPage = context.create().page(PAGE_PATH);
    context.currentPage(currentPage);

    context.request().setParameterMap(ImmutableValueMap.of(ParsysComponentsServlet.RP_PATH, LOCAL_PATH));

    when(allowedComponentsProvider.getAllowedComponents(
        eq(PAGE_PATH + "/" + LOCAL_PATH), any(ResourceResolver.class)))
        .thenReturn(ImmutableSortedSet.of("sample/components/comp1", "sample/components/comp2"));

    context.registerService(AllowedComponentsProvider.class, allowedComponentsProvider);
  }

  @Test
  public void testJsonResult() throws Exception {
    context.runMode(RunMode.AUTHOR);
    ParsysComponentsServlet underTest = new ParsysComponentsServlet();
    context.registerInjectActivateService(underTest);

    underTest.service(context.request(), context.response());
    assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());
    assertEquals("[\"sample/components/comp1\",\"sample/components/comp2\"]", context.response().getOutputAsString());
  }

  @Test
  public void testJsonResultNoRequestParam() throws Exception {
    context.runMode(RunMode.AUTHOR);
    ParsysComponentsServlet underTest = new ParsysComponentsServlet();
    context.registerInjectActivateService(underTest);

    context.request().setParameterMap(ImmutableValueMap.of());

    underTest.service(context.request(), context.response());
    assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());
    assertEquals("[]", context.response().getOutputAsString());
  }

  @Test
  public void testJsonResultPublish() throws Exception {
    context.runMode(RunMode.PUBLISH);
    ParsysComponentsServlet underTest = new ParsysComponentsServlet();
    context.registerInjectActivateService(underTest);

    underTest.service(context.request(), context.response());
    assertEquals(HttpServletResponse.SC_NOT_FOUND, context.response().getStatus());
  }

}
