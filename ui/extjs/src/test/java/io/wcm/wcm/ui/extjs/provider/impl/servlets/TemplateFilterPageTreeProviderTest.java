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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.wcm.ui.extjs.provider.AbstractPageProvider;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.servlet.http.HttpServletResponse;

import org.apache.jackrabbit.commons.iterator.NodeIteratorAdapter;
import org.apache.sling.api.adapter.AdapterFactory;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TemplateFilterPageTreeProviderTest {

  private static final String TEMPLATE_1 = "/apps/app1/templates/template1";
  private static final String TEMPLATE_2 = "/apps/app1/templates/template2";

  @Rule
  public AemContext context = new AemContext();

  @Mock
  public AdapterFactory adapterFactory;
  @Mock
  public Session session;
  @Mock
  public Workspace workspace;
  @Mock
  public QueryManager queryManager;
  @Mock
  public Query query;
  @Mock
  public QueryResult queryResult;

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

    // simulate query manager
    when(adapterFactory.getAdapter(any(ResourceResolver.class), eq(Session.class))).thenReturn(session);
    when(session.getWorkspace()).thenReturn(workspace);
    when(workspace.getQueryManager()).thenReturn(queryManager);
    when(queryManager.createQuery(anyString(), anyString())).thenReturn(query);
    when(query.execute()).thenReturn(queryResult);
    context.registerService(AdapterFactory.class, adapterFactory);
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

  private void mockQueryResult(String... paths) throws RepositoryException {
    List<Node> nodes = new ArrayList<>();
    for (String path : paths) {
      Node node = mock(Node.class);
      when(node.getPath()).thenReturn(path);
      nodes.add(node);
    }
    when(queryResult.getNodes()).thenReturn(new NodeIteratorAdapter(nodes));
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
