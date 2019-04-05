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

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.settings.SlingSettingsService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.sling.commons.request.RequestParam;
import io.wcm.sling.commons.resource.ResourceType;
import io.wcm.wcm.commons.contenttype.ContentType;
import io.wcm.wcm.commons.contenttype.FileExtension;
import io.wcm.wcm.commons.util.RunMode;
import io.wcm.wcm.parsys.componentinfo.AllowedComponentsProvider;

/**
 * Handles AJAX calls for updateComponentListHandler JS method to update list of allowed component lists dynamically.
 */
@Component(service = Servlet.class, immediate = true, property = {
    "sling.servlet.extensions=" + FileExtension.JSON,
    "sling.servlet.selectors=" + ParsysComponentsServlet.SELECTOR,
    "sling.servlet.resourceTypes=sling/servlet/default",
    "sling.servlet.methods=" + HttpConstants.METHOD_GET
})
public final class ParsysComponentsServlet extends SlingSafeMethodsServlet {
  private static final long serialVersionUID = 1L;

  static final String RP_PATH = "path";
  static final String RP_RESOURCE_TYPE = "resourceType";
  static final String SELECTOR = "wcmio-parsys-components";

  private static final Logger log = LoggerFactory.getLogger(ParsysComponentsServlet.class);

  @Reference
  private AllowedComponentsProvider allowedComponentsProvider;

  @Reference
  private SlingSettingsService slingSettings;

  private boolean enabled;

  @Activate
  @SuppressWarnings("deprecation")
  protected void activate(ComponentContext componentContext) {
    // Activate only in author mode
    enabled = !RunMode.disableIfNotAuthor(slingSettings.getRunModes(), componentContext, log);
  }

  @SuppressWarnings("null")
  @Override
  protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
    if (!enabled) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    ResourceResolver resolver = request.getResourceResolver();
    PageManager pageManager = AdaptTo.notNull(resolver, PageManager.class);
    Page currentPage = pageManager.getContainingPage(request.getResource());

    if (currentPage == null) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    long startTime = 0;
    if (log.isDebugEnabled()) {
      startTime = System.currentTimeMillis();
    }

    response.setContentType(ContentType.JSON);

    JSONArray allowedComponents = new JSONArray();

    String relativePath = RequestParam.get(request, RP_PATH);
    String resourceType = RequestParam.get(request, RP_RESOURCE_TYPE);
    if (StringUtils.isNotEmpty(relativePath)) {
      // get resource from paragraph system
      Set<String> allowed = allowedComponentsProvider.getAllowedComponents(currentPage, relativePath, resourceType, resolver);

      // create set with relative resource type paths
      Set<String> allowedComponentsRelative = new TreeSet<String>();
      for (String allowedResourceType : allowed) {
        allowedComponentsRelative.add(ResourceType.makeAbsolute(allowedResourceType, resolver));
      }

      allowedComponents = new JSONArray(allowedComponentsRelative);
    }

    response.getWriter().write(allowedComponents.toString());

    // output profiling info in DEBUG mode
    if (log.isDebugEnabled()) {
      long endTime = System.currentTimeMillis();
      long duration = endTime - startTime;
      log.debug("ParsysComponentsServlet for " + currentPage.getPath() + "/" + relativePath + " took " + duration + "ms");
    }
  }

}
