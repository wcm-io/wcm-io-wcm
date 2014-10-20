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
package io.wcm.wcm.ui.extjs.provider.impl.servlets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.wcm.ui.extjs.provider.AbstractPageProvider;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.commons.json.JSONArray;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.commons.predicate.PredicateProvider;
import com.day.cq.wcm.api.Page;

public class PageListProviderTest {

  @Rule
  public AemContext context = new AemContext();

  @Before
  public void setUp() throws Exception {
    context.create().page("/content/sample/en");
    context.create().page("/content/sample/en/page1", "", "title1");
    context.create().page("/content/sample/en/page2", "", "title2");
  }

  @Test
  public void testListWithPath() throws Exception {
    context.request().setParameterMap(ImmutableValueMap.of(AbstractPageProvider.RP_PATH, "/content/sample/en"));

    JSONArray result = getJsonResult();
    assertEquals(2, result.length());

    assertEquals("/content/sample/en/page1", result.getJSONObject(0).get("value"));
    assertEquals("title1", result.getJSONObject(0).get("text"));

    assertEquals("/content/sample/en/page2", result.getJSONObject(1).get("value"));
    assertEquals("title2", result.getJSONObject(1).get("text"));
  }

  @Test
  public void testListCurrentResource() throws Exception {
    context.currentResource(context.resourceResolver().getResource("/content/sample/en/jcr:content"));

    JSONArray result = getJsonResult();
    assertEquals(2, result.length());

    assertEquals("/content/sample/en/page1", result.getJSONObject(0).get("value"));
    assertEquals("title1", result.getJSONObject(0).get("text"));

    assertEquals("/content/sample/en/page2", result.getJSONObject(1).get("value"));
    assertEquals("title2", result.getJSONObject(1).get("text"));
  }

  @Test
  public void testInvalidPath() throws Exception {
    context.request().setParameterMap(ImmutableValueMap.of(AbstractPageProvider.RP_PATH, "/content/sample/en/invalid/path"));
    assertNull(getJsonResult());
  }

  @Test
  public void testWithPredicate() throws Exception {
    final String PREDICATE_NAME = "mypredicate";
    context.registerService(PredicateProvider.class, new PredicateProvider() {
      @Override
      public Predicate getPredicate(String name) {
        if (StringUtils.equals(name, PREDICATE_NAME)) {
          return new Predicate() {
            @Override
            public boolean evaluate(Object object) {
              Page page = (Page)object;
              return StringUtils.equals(page.getName(), "page1");
            }
          };
        }
        return null;
      }
    });

    context.request().setParameterMap(ImmutableValueMap.of(AbstractPageProvider.RP_PATH, "/content/sample/en",
        AbstractPageProvider.RP_PREDICATE, PREDICATE_NAME));

    JSONArray result = getJsonResult();
    assertEquals(1, result.length());

    assertEquals("/content/sample/en/page1", result.getJSONObject(0).get("value"));
    assertEquals("title1", result.getJSONObject(0).get("text"));
  }

  private JSONArray getJsonResult() throws Exception {
    PageListProvider underTest = context.registerInjectActivateService(new PageListProvider());
    underTest.service(context.request(), context.response());
    if (context.response().getStatus() == HttpServletResponse.SC_OK) {
      return new JSONArray(context.response().getOutputAsString());
    }
    else {
      return null;
    }
  }

}
