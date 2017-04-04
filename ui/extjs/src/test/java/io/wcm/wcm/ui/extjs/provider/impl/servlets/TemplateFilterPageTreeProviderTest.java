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

import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.servlet.http.HttpServletResponse;

import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.testing.mock.jcr.MockJcr;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.wcm.ui.extjs.provider.AbstractPageProvider;

@RunWith(MockitoJUnitRunner.class)
public class TemplateFilterPageTreeProviderTest {

  private static final String TEMPLATE_1 = "/apps/app1/templates/template1";
  private static final String TEMPLATE_2 = "/apps/app1/templates/template2";

  @Rule
  public AemContext context = new AemContext(ResourceResolverType.JCR_MOCK);

  @Before
  public void setUp() throws Exception {
    context.create().page("/content/sample/en");
    context.create().page("/content/sample/en/page1", TEMPLATE_1, "title1");
    context.create().page("/content/sample/en/page1/page11", TEMPLATE_2, "title11");
    context.create().page("/content/sample/en/page1/page11/page111", TEMPLATE_1, "title111");
    context.create().page("/content/sample/en/page1/page11/page112", TEMPLATE_1, "title111");
    context.create().page("/content/sample/en/page1/page12", TEMPLATE_2, "title12");
    context.create().page("/content/sample/en/page1/page12/page121", TEMPLATE_2, "title121");
    context.create().page("/content/sample/en/page1/page12/page122", TEMPLATE_2, "title122");
    context.create().page("/content/sample/en/page2", TEMPLATE_1, "title2");
  }

  @Test
  public void testWithOutTemplates() throws Exception {
    context.request().setParameterMap(ImmutableValueMap.of(AbstractPageProvider.RP_PATH, "/content/sample/en"));

    JSONArray result = getJsonResult();
    assertEquals(0, result.length());
  }

  @Test
  public void testWithTemplate1() throws Exception {
    context.request().setParameterMap(ImmutableValueMap.of(AbstractPageProvider.RP_PATH, "/content/sample/en",
        TemplateFilterPageTreeProvider.RP_TEMPLATE, new String[] {
        TEMPLATE_1
    }));

    // simulate query result
    mockQueryResult("/content/sample/en/page1",
        "/content/sample/en/page1/page11/page111",
        "/content/sample/en/page1/page11/page112",
        "/content/sample/en/page2");

    JSONArray result = getJsonResult();

    assertEquals(2, result.length());
    assertItem(result.getJSONObject(0), "page1", "title1", TEMPLATE_1, false);
    assertItem(result.getJSONObject(1), "page2", "title2", TEMPLATE_1, true);

    JSONArray page1children = result.getJSONObject(0).getJSONArray("children");
    assertEquals(1, page1children.length());

    assertItem(page1children.getJSONObject(0), "page11", "title11", TEMPLATE_2, false);
    assertNull(page1children.getJSONObject(0).optJSONArray("children"));
  }

  @Test
  public void testWithTemplate1an2() throws Exception {
    context.request().setParameterMap(ImmutableValueMap.of(AbstractPageProvider.RP_PATH, "/content/sample/en",
        TemplateFilterPageTreeProvider.RP_TEMPLATE, new String[] {
        TEMPLATE_1,
        TEMPLATE_2
    }));

    // simulate query result
    mockQueryResult("/content/sample/en/page1",
        "/content/sample/en/page1/page11",
        "/content/sample/en/page1/page11/page111",
        "/content/sample/en/page1/page11/page112",
        "/content/sample/en/page1/page12",
        "/content/sample/en/page1/page12/page121",
        "/content/sample/en/page1/page12/page122",
        "/content/sample/en/page2");

    JSONArray result = getJsonResult();

    assertEquals(2, result.length());
    assertItem(result.getJSONObject(0), "page1", "title1", TEMPLATE_1, false);
    assertItem(result.getJSONObject(1), "page2", "title2", TEMPLATE_1, true);

    JSONArray page1children = result.getJSONObject(0).getJSONArray("children");
    assertEquals(2, page1children.length());

    assertItem(page1children.getJSONObject(0), "page11", "title11", TEMPLATE_2, false);
    assertNull(page1children.getJSONObject(0).optJSONArray("children"));
    assertItem(page1children.getJSONObject(1), "page12", "title12", TEMPLATE_2, false);
    assertNull(page1children.getJSONObject(1).optJSONArray("children"));
  }

  private JSONArray getJsonResult() throws Exception {
    TemplateFilterPageTreeProvider underTest = context.registerInjectActivateService(new TemplateFilterPageTreeProvider());
    underTest.service(context.request(), context.response());
    if (context.response().getStatus() == HttpServletResponse.SC_OK) {
      return new JSONArray(context.response().getOutputAsString());
    }
    else {
      return null;
    }
  }

  private void mockQueryResult(String... paths) {
    List<String> resultPaths = ImmutableList.copyOf(paths);
    List<Node> resultNodes = Lists.transform(resultPaths, new Function<String, Node>() {
      @Override
      public Node apply(String path) {
        return context.resourceResolver().getResource(path).adaptTo(Node.class);
      }
    });
    MockJcr.setQueryResult(context.resourceResolver().adaptTo(Session.class), resultNodes);
  }

  private void assertItem(JSONObject jsonObject, String name, String title, String template,
      boolean leaf) throws JSONException {
    assertEquals(name, jsonObject.get("name"));
    assertEquals(title, jsonObject.get("text"));
    assertEquals("cq:Page", jsonObject.get("type"));
    assertEquals(template, jsonObject.get("template"));
    assertEquals("page", jsonObject.get("cls"));
    assertEquals(leaf, jsonObject.optBoolean("leaf"));
  }

}
