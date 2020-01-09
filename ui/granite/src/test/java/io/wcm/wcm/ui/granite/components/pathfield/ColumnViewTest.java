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
package io.wcm.wcm.ui.granite.components.pathfield;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.RequestDispatcher;

import org.apache.sling.api.request.RequestDispatcherOptions;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.servlethelpers.MockRequestDispatcherFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.adobe.granite.ui.components.ExpressionResolver;
import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.ResourceDataSource;
import com.google.common.collect.ImmutableList;

import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.ui.granite.testcontext.MockExpressionResolver;

@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
class ColumnViewTest {

  private final AemContext context = new AemContext();

  @Mock
  private ExpressionResolver expressionResolver;
  @Mock
  private RequestDispatcher requestDispatcher;

  @BeforeEach
  void setUp() {
    context.registerService(ExpressionResolver.class, new MockExpressionResolver());

    // simulate data source retrieval
    context.request().setRequestDispatcherFactory(new MockRequestDispatcherFactory() {
      @Override
      public RequestDispatcher getRequestDispatcher(Resource resource, RequestDispatcherOptions options) {
        String path = resource.getValueMap().get("path", String.class);
        if (path != null) {
          Resource pathRessource = context.resourceResolver().getResource(path);
          context.request().setAttribute(DataSource.class.getName(), new ResourceDataSource(pathRessource));
        }
        return requestDispatcher;
      }
      @Override
      public RequestDispatcher getRequestDispatcher(String path, RequestDispatcherOptions options) {
        Resource resource = context.resourceResolver().getResource(path);
        if (resource == null) {
          throw new RuntimeException("No resource: " + path);
        }
        return getRequestDispatcher(resource, options);
      }
    });

    context.create().resource("/content/site1");
    context.create().resource("/content/site1/en");
    context.create().resource("/content/site1/en/page1");
    context.create().resource("/content/site1/en/page1/page11");
    context.create().resource("/content/site1/en/page1/page11/page111");
    context.create().resource("/content/site1/en/page1/page11/page112");
    context.create().resource("/content/site1/en/page1/page12");
    context.create().resource("/content/site1/en/page2");
    context.create().resource("/content/site1/en/page2/page21");
  }

  @Test
  void testRoot() {
    List<Column> columns = getColumns(
        "rootPath", "/content/site1/en",
        "path", "/content/site1/en");
    assertEquals(1, columns.size());
    assertColumn(columns.get(0), "/content/site1/en",
        "/content/site1/en/page1",
        "/content/site1/en/page2");
  }

  @Test
  void testRoot_showRoot() {
    List<Column> columns = getColumns(
        "rootPath", "/content/site1/en",
        "path", "/content/site1/en",
        "showRoot", true);
    assertEquals(2, columns.size());
    assertColumn(columns.get(0), "parentof:/content/site1/en", "/content/site1/en");
    assertColumn(columns.get(1), "/content/site1/en",
        "/content/site1/en/page1",
        "/content/site1/en/page2");
  }

  @Test
  void testRoot_loadAncestors() {
    List<Column> columns = getColumns(
        "rootPath", "/content/site1/en",
        "path", "/content/site1/en",
        "loadAncestors", true);
    assertEquals(1, columns.size());
    assertColumn(columns.get(0), "/content/site1/en",
        "/content/site1/en/page1",
        "/content/site1/en/page2");
  }

  @Test
  void testRoot_showRoot_loadAncestors() {
    List<Column> columns = getColumns(
        "rootPath", "/content/site1/en",
        "path", "/content/site1/en",
        "showRoot", true,
        "loadAncestors", true);
    assertEquals(2, columns.size());
    assertColumn(columns.get(0), "parentof:/content/site1/en",
        "/content/site1/en");
    assertColumn(columns.get(1), "/content/site1/en",
        "/content/site1/en/page1",
        "/content/site1/en/page2");
  }

  @Test
  void testRoot_SizeLimit() {
    List<Column> columns = getColumns(
        "rootPath", "/content/site1/en",
        "path", "/content/site1/en",
        "size", 1);
    assertEquals(1, columns.size());
    assertColumn(columns.get(0), "/content/site1/en", true,
        "/content/site1/en/page1");
  }

  @Test
  void testPage11() {
    List<Column> columns = getColumns(
        "rootPath", "/content/site1/en",
        "path", "/content/site1/en/page1/page11");
    assertEquals(1, columns.size());
    assertColumn(columns.get(0), "/content/site1/en/page1/page11",
        "/content/site1/en/page1/page11/page111",
        "/content/site1/en/page1/page11/page112");
  }

  @Test
  void testPage11_showRoot() {
    List<Column> columns = getColumns(
        "rootPath", "/content/site1/en",
        "path", "/content/site1/en/page1/page11",
        "showRoot", true);
    assertEquals(1, columns.size());
    assertColumn(columns.get(0), "/content/site1/en/page1/page11",
        "/content/site1/en/page1/page11/page111",
        "/content/site1/en/page1/page11/page112");
  }

  @Test
  void testPage11_loadAncestors() {
    List<Column> columns = getColumns(
        "rootPath", "/content/site1/en",
        "path", "/content/site1/en/page1/page11",
        "loadAncestors", true);
    assertEquals(3, columns.size());
    assertLazyColumn(columns.get(0), "/content/site1/en", "/content/site1/en/page1");
    assertLazyColumn(columns.get(1), "/content/site1/en/page1", "/content/site1/en/page1/page11");
    assertColumn(columns.get(2), "/content/site1/en/page1/page11",
        "/content/site1/en/page1/page11/page111",
        "/content/site1/en/page1/page11/page112");
  }

  @Test
  void testPage11_showRoot_loadAncestors() {
    List<Column> columns = getColumns(
        "rootPath", "/content/site1/en",
        "path", "/content/site1/en/page1/page11",
        "showRoot", true,
        "loadAncestors", true);
    assertEquals(4, columns.size());
    assertColumn(columns.get(0), "parentof:/content/site1/en",
        "/content/site1/en");
    assertLazyColumn(columns.get(1), "/content/site1/en", "/content/site1/en/page1");
    assertLazyColumn(columns.get(2), "/content/site1/en/page1", "/content/site1/en/page1/page11");
    assertColumn(columns.get(3), "/content/site1/en/page1/page11",
        "/content/site1/en/page1/page11/page111",
        "/content/site1/en/page1/page11/page112");
  }

  @Test
  void testSite1() {
    List<Column> columns = getColumns(
        "rootPath", "/content/site1/en",
        "path", "/content/site1");
    assertEquals(1, columns.size());
    assertColumn(columns.get(0), "/content/site1/en",
        "/content/site1/en/page1",
        "/content/site1/en/page2");
  }

  @Test
  void testSite1_showRoot() {
    List<Column> columns = getColumns(
        "rootPath", "/content/site1/en",
        "path", "/content/site1",
        "showRoot", true);
    assertEquals(2, columns.size());
    assertColumn(columns.get(0), "parentof:/content/site1/en", "/content/site1/en");
    assertColumn(columns.get(1), "/content/site1/en",
        "/content/site1/en/page1",
        "/content/site1/en/page2");
  }

  @Test
  void testSite1_loadAncestors() {
    List<Column> columns = getColumns(
        "rootPath", "/content/site1/en",
        "path", "/content/site1",
        "loadAncestors", true);
    assertEquals(1, columns.size());
    assertColumn(columns.get(0), "/content/site1/en",
        "/content/site1/en/page1",
        "/content/site1/en/page2");
  }

  @Test
  void testSite1_showRoot_loadAncestors() {
    List<Column> columns = getColumns(
        "rootPath", "/content/site1/en",
        "path", "/content/site1",
        "showRoot", true,
        "loadAncestors", true);
    assertEquals(2, columns.size());
    assertColumn(columns.get(0), "parentof:/content/site1/en",
        "/content/site1/en");
    assertColumn(columns.get(1), "/content/site1/en",
        "/content/site1/en/page1",
        "/content/site1/en/page2");
  }


  // --- test helper methods ---

  private List<Column> getColumns(Object... props) {
    context.currentResource(context.create().resource("/apps/pathfield", props));
    context.create().resource("/apps/pathfield/datasource",
        "sling:resourceType", "/dummyDataSource");
    ColumnView underTest = AdaptTo.notNull(context.request(), ColumnView.class);
    return underTest.getColumns();
  }

  private static void assertColumn(Column column, String columnId, String... columnItemIds) {
    assertColumn(column, columnId, false, columnItemIds);
  }

  private static void assertColumn(Column column, String columnId, boolean hasMore, String... columnItemIds) {
    assertEquals(columnId, column.getColumnId(), "columnId");
    assertFalse(column.isLazy(), "lazy");
    assertEquals(hasMore, column.isHasMore(), "hasMore");
    assertNull(column.getActiveId(), "activeId");

    List<String> expectedItemIds = ImmutableList.copyOf(columnItemIds);
    List<String> actualItemIds = column.getItems().stream()
        .map(item -> item.getItemId())
        .collect(Collectors.toList());
    assertEquals(expectedItemIds, actualItemIds, "columnItems");
  }

  private static void assertLazyColumn(Column column, String columnId, String activeId) {
    assertEquals(columnId, column.getColumnId(), "columnId");
    assertTrue(column.isLazy(), "lazy");
    assertFalse(column.isHasMore(), "hasMore");
    assertEquals(activeId, column.getActiveId(), "activeId");
  }

}
