/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2020 wcm.io
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
package com.adobe.granite.ui.components.impl;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestDispatcherOptions;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.jetbrains.annotations.Nullable;

import com.adobe.granite.ui.components.Config;
import com.adobe.granite.ui.components.ExpressionHelper;
import com.adobe.granite.ui.components.ExpressionResolver;
import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.EmptyDataSource;
import com.adobe.granite.ui.components.ds.ResourceDataSource;
import com.day.cq.i18n.I18n;

/*
 * Partial dummy implementation to get ComponentHelper running in unit test despite of missing impl class.
 */
public final class BaseComponentHelper {

  protected SlingScriptHelper sling;
  protected SlingHttpServletRequest request;
  protected SlingHttpServletResponse response;

  private I18n i18n;
  private ExpressionHelper ex;
  private Config config;

  public BaseComponentHelper(SlingScriptHelper sling, SlingHttpServletRequest request,
      SlingHttpServletResponse response) {
    this.sling = sling;
    this.request = request;
    this.response = response;
  }

  public SlingScriptHelper getSling() {
    return sling;
  }

  public SlingHttpServletRequest getRequest() {
    return request;
  }

  public SlingHttpServletResponse getResponse() {
    return response;
  }

  public I18n getI18n() {
    if (i18n == null) {
      i18n = new I18n(request);
    }
    return i18n;
  }

  public ExpressionHelper getExpressionHelper() {
    if (ex == null) {
      ExpressionResolver service = sling.getService(ExpressionResolver.class);
      if (service == null) {
        throw new RuntimeException("ExpressionResolver not available");
      }
      ex = new ExpressionHelper(service, request);
    }
    return ex;
  }

  public Config getConfig() {
    if (config == null) {
      config = new Config(request.getResource());
    }
    return config;
  }

  public DataSource getItemDataSource() throws ServletException, IOException {
    return getItemDataSource(getRequest().getResource());
  }

  public DataSource getItemDataSource(Resource resource) throws ServletException, IOException {
    Resource items = resource.getChild(Config.ITEMS);
    if (items != null) {
      return new ResourceDataSource(items);
    }
    Resource datasource = resource.getChild(Config.DATASOURCE);
    if (datasource != null) {
      DataSource ds = asDataSource(datasource, resource);
      if (ds == null) {
        throw new RuntimeException("DataSource is null.");
      }
      return ds;
    }
    return EmptyDataSource.instance();
  }

  public DataSource asDataSource(Resource datasource) throws ServletException, IOException {
    return asDataSource(datasource, null);
  }

  public DataSource asDataSource(Resource datasource, Resource context)
      throws ServletException, IOException {
    Resource contextResource = context;
    if (datasource == null) {
      return null;
    }
    if (contextResource == null) {
      contextResource = datasource;
    }
    DataSource ds = fetchData(contextResource, getResourceType(datasource), DataSource.class);
    return ds != null ? ds : EmptyDataSource.instance();
  }

  @SuppressWarnings("unchecked")
  private @Nullable <T> T fetchData(Resource resource, String resourceType, Class<T> type) throws ServletException, IOException {
    if (resourceType == null) {
      return null;
    }
    SlingHttpServletRequest req = getRequest();
    try {
      RequestDispatcher dispatcher = req.getRequestDispatcher(resource, new RequestDispatcherOptions(resourceType));
      if (dispatcher != null) {
        dispatcher.include(req, response);
        return (T)req.getAttribute(type.getName());
      }
      return null;
    }
    finally {
      req.removeAttribute(type.getName());
    }
  }

  @SuppressWarnings("null")
  private static String getResourceType(Resource resource) {
    return new Config(resource).get(ResourceResolver.PROPERTY_RESOURCE_TYPE, String.class);
  }

}
