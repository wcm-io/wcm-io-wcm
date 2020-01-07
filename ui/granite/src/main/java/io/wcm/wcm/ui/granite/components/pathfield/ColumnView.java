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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.osgi.annotation.versioning.ProviderType;

import com.adobe.granite.ui.components.ComponentHelper;
import com.adobe.granite.ui.components.Config;
import com.adobe.granite.ui.components.ExpressionHelper;
import com.adobe.granite.ui.components.ds.DataSource;
import com.google.common.collect.ImmutableMap;

import io.wcm.wcm.ui.granite.pathfield.impl.util.DummyPageContext;
import io.wcm.wcm.ui.granite.resource.GraniteUiSyntheticResource;

/**
 * Model for customized columnview Granite UI component for path field.
 */
@Model(adaptables = SlingHttpServletRequest.class)
@ProviderType
public final class ColumnView {

  private static final String FALLBACK_ROOT_RESOURCE = "/";

  @SlingObject
  private SlingHttpServletRequest request;
  @SlingObject
  private SlingHttpServletResponse response;
  @SlingObject
  private ResourceResolver resourceResolver;

  private Resource currentResource;
  private final List<Column> columns = new ArrayList<>();

  @PostConstruct
  @SuppressWarnings("null")
  private void activate() {
    ComponentHelper cmp = new ComponentHelper(new DummyPageContext(request, response));
    Config cfg = cmp.getConfig();
    ExpressionHelper ex = cmp.getExpressionHelper();

    Integer size = ex.get(cfg.get("size", String.class), Integer.class);
    String itemResourceType = cfg.get("itemResourceType");
    boolean showRoot = cfg.get("showRoot", false);
    boolean loadAncestors = cfg.get("loadAncestors", false);

    // make sure we always have a valid root resource
    Resource rootResource = resourceResolver.getResource(ex.getString(cfg.get("rootPath", FALLBACK_ROOT_RESOURCE)));
    if (rootResource == null) {
      rootResource = resourceResolver.getResource(FALLBACK_ROOT_RESOURCE);
    }

    // if current resource is invalid or not same or descendant of root resource, set it to root resource
    String path = ex.getString(cfg.get("path", rootResource.getPath()));
    currentResource = resourceResolver.getResource(path);
    if (currentResource == null || !isSameResourceOrChild(rootResource, currentResource)) {
      currentResource = rootResource;
    }

    // generate column for root
    if (showRoot && (StringUtils.equals(currentResource.getPath(), rootResource.getPath()) || loadAncestors)) {
      columns.add(getRootColumn(rootResource, itemResourceType));
    }

    // generate columns for ancestors
    if (loadAncestors) {
      columns.addAll(getAncestorColumns(currentResource, rootResource));
    }

    // generate columns for items
    DataSource dataSource = getDataSource(cmp, currentResource);
    columns.add(getCurrentResourceColumn(dataSource, size, currentResource, itemResourceType));
  }

  private boolean isSameResourceOrChild(Resource rootResource, Resource resource) {
    if (StringUtils.equals(rootResource.getPath(), resource.getPath())) {
      return true;
    }
    else {
      return StringUtils.startsWith(resource.getPath(), rootResource.getPath() + "/");
    }
  }

  public Resource getCurrentResource() {
    return this.currentResource;
  }

  public List<Column> getColumns() {
    return this.columns;
  }

  /**
   * Get data source to list children of given resource.
   * @param cmp Component helper
   * @param resource Given resource
   * @return Data source
   */
  private DataSource getDataSource(ComponentHelper cmp, Resource resource) {
    try {
      /*
       * by default the path is read from request "path" parameter
       * here we overwrite it via a synthetic resource because the path may be overwritten by validation logic
       * to ensure the path is not beyond the configured root path
       */
      ValueMap overwriteProperties = new ValueMapDecorator(ImmutableMap.<String, Object>of("path", resource.getPath()));
      Resource dataSourceResourceWrapper = GraniteUiSyntheticResource.wrapMerge(request.getResource(), overwriteProperties);
      return cmp.getItemDataSource(dataSourceResourceWrapper);
    }
    catch (ServletException | IOException ex) {
      throw new RuntimeException("Unable to get data source.", ex);
    }
  }

  /**
   * Generate column for data source items for current resource.
   * @param dataSource Data source
   * @param size Size limit
   * @param currentResource Current resource
   * @param itemResourceType Item resource type
   * @return Column
   */
  private static Column getCurrentResourceColumn(DataSource dataSource, Integer size,
      Resource currentResource, String itemResourceType) {

    Iterator<Resource> items = dataSource.iterator();

    boolean hasMore = false;
    if (size != null) {
      List<Resource> list = new ArrayList<Resource>();
      while (items.hasNext() && list.size() < size) {
        list.add(items.next());
      }
      hasMore = items.hasNext();
      items = list.iterator();
    }

    Column column = new Column()
        .columnId(currentResource.getPath())
        .hasMore(hasMore)
        .metaElement(true);
    while (items.hasNext()) {
      Resource item = items.next();
      column.addItem(new ColumnItem(item)
          .resourceType(itemResourceType));
    }

    return column;
  }

  /**
   * Generate extra column representing the root resource.
   * @param rootResource Root resource
   * @param itemResourceType Item resource type
   * @return Column
   */
  private static Column getRootColumn(Resource rootResource, String itemResourceType) {
    /*
     * Put a special path for columnId to avoid having the same columnId with the next column to avoid breaking the contract of columnId.
     * The contract of columnId is that it should be a path of the current column, i.e. the path should be a path representing a parent.
     * e.g. When columnId = "/", then the column will show the children of this path, such as "/a", "/b".
     * So for showRoot scenario, if we want to show the item with path = "/", we need to generate the column having a columnId with value of the parent of "/".
     * Since the cannot have a parent of "/", then we decide to just use a special convention ("parentof:<path>") to indicate this.
     * Other component (e.g. `.granite-collection-navigator`) reading the columnId can then understand this convention and handle it accordingly.
     */
    String columnId = "parentof:" + rootResource.getPath();

    Column column = new Column()
        .columnId(columnId)
        .hasMore(false);
    column.addItem(new ColumnItem(rootResource)
        .resourceType(itemResourceType)
        .active(true));
    return column;
  }

  /**
   * Generate column for each ancestor.
   * @param currentResource Current resource
   * @param rootResource Root resource
   * @return Columns
   */
  private static List<Column> getAncestorColumns(Resource currentResource, Resource rootResource) {
    List<Column> columns = new ArrayList<>();
    List<Resource> ancestors = getAncestors(currentResource, rootResource);
    for (int i = 0; i < ancestors.size(); i++) {
      Resource r = ancestors.get(i);

      String activeId;
      if (i < ancestors.size() - 1) {
        activeId = ancestors.get(i + 1).getPath();
      }
      else {
        activeId = currentResource.getPath();
      }

      Column column = new Column()
          .columnId(r.getPath())
          .lazy(true)
          .activeId(activeId);
      columns.add(column);
    }
    return columns;
  }

  /**
   * Returns the ancestors of the current resource (inclusive) up to the root.
   * The result is ordered with the root as the first item.
   */
  private static List<Resource> getAncestors(Resource currentResource, Resource rootResource) {
    List<Resource> results = new ArrayList<Resource>();

    if (currentResource == null || rootResource == null || StringUtils.equals(currentResource.getPath(), rootResource.getPath())) {
      return results;
    }

    Resource parent = currentResource.getParent();
    while (parent != null) {
      results.add(0, parent);
      if (parent.getPath().equals(rootResource.getPath())) {
        break;
      }
      parent = parent.getParent();
    }
    return results;
  }

}
