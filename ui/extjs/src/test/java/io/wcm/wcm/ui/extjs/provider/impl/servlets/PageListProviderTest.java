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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import javax.servlet.http.HttpServletResponse;

import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.commons.predicate.PredicateProvider;

import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.ui.extjs.provider.AbstractPageProvider;

@ExtendWith(AemContextExtension.class)
class PageListProviderTest {

  private final AemContext context = new AemContext();

  @BeforeEach
  void setUp() throws Exception {
    context.create().page("/content/sample/en");
    context.create().page("/content/sample/en/page1", "", "title1");
    context.create().page("/content/sample/en/page2", "", "title2");
  }

  @Test
  void testWithPath() throws Exception {
    context.request().setParameterMap(ImmutableValueMap.of(AbstractPageProvider.RP_PATH, "/content/sample/en"));

    JSONArray result = getJsonResult();

    assertEquals(2, result.length());
    assertItem(result.getJSONObject(0), "/content/sample/en/page1", "title1");
    assertItem(result.getJSONObject(1), "/content/sample/en/page2", "title2");
  }

  @Test
  void testWithCurrentResource() throws Exception {
    context.currentResource(context.resourceResolver().getResource("/content/sample/en/jcr:content"));

    JSONArray result = getJsonResult();

    assertEquals(2, result.length());
    assertItem(result.getJSONObject(0), "/content/sample/en/page1", "title1");
    assertItem(result.getJSONObject(1), "/content/sample/en/page2", "title2");
  }

  @Test
  void testInvalidPath() throws Exception {
    context.request().setParameterMap(ImmutableValueMap.of(AbstractPageProvider.RP_PATH, "/content/sample/en/invalid/path"));
    assertNull(getJsonResult());
  }

  @Test
  void testWithPredicate() throws Exception {
    context.registerService(PredicateProvider.class, new DummyPredicateProvider());

    context.request().setParameterMap(ImmutableValueMap.of(AbstractPageProvider.RP_PATH, "/content/sample/en",
        AbstractPageProvider.RP_PREDICATE, DummyPredicateProvider.PREDICATE_PAGENAME_PAGE1));

    JSONArray result = getJsonResult();
    assertEquals(1, result.length());
    assertItem(result.getJSONObject(0), "/content/sample/en/page1", "title1");
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

  private void assertItem(JSONObject jsonObject, String path, String title) throws JSONException {
    assertEquals(path, jsonObject.get("value"));
    assertEquals(title, jsonObject.get("text"));
  }

}
