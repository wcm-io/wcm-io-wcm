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
package io.wcm.wcm.ui.granite.util;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;

import com.adobe.granite.ui.components.Value;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

/**
 * Helper methods for GraniteUI components.
 */
@ProviderType
public final class GraniteUi {

  static final String CREATEPAGEWITZARD_PROPERTIES_URI = "/mnt/overlay/wcm/core/content/sites/createpagewizard/properties.html";
  static final String CREATEPAGEWITZARD_URI = "/mnt/overlay/wcm/core/content/sites/createpagewizard.html";
  static final String HEADER_REFERER = "Referer";

  private GraniteUi() {
    // static methods only
  }

  /**
   * Current content resource
   * @param request Request
   * @return Current content resource or null
   */
  public static @Nullable Resource getContentResource(@NotNull HttpServletRequest request) {
    String contentPath = getContentPath(request);
    if (contentPath != null) {
      SlingHttpServletRequest slingRequest = (SlingHttpServletRequest)request;
      return slingRequest.getResourceResolver().getResource(contentPath);
    }
    return null;
  }

  /**
   * Get current content resource
   * If it does not exist, go up the content path and return the first resource that exists.
   * @param request Request
   * @return Current content resource or the first existing parent/ancestor.
   */
  public static @Nullable Resource getContentResourceOrParent(@NotNull HttpServletRequest request) {
    String contentPath = getContentPath(request);
    return getContentResourceOrParentFromPath((SlingHttpServletRequest)request, contentPath);
  }

  /**
   * Current content page. If the current resource does not exist the content page
   * of the next-existing parent resource is returned.
   * @param request Request
   * @return Current content page or null
   */
  @SuppressWarnings("null")
  public static @Nullable Page getContentPage(@NotNull HttpServletRequest request) {
    SlingHttpServletRequest slingRequest = (SlingHttpServletRequest)request;
    Resource contentResource = getContentResourceOrParent(request);
    if (contentResource != null) {
      PageManager pageManager = slingRequest.getResourceResolver().adaptTo(PageManager.class);
      return pageManager.getContainingPage(contentResource);
    }
    else {
      return null;
    }
  }

  /**
   * From the list of resource types get the first one that exists.
   * @param resourceResolver Resource resolver
   * @param resourceTypes ResourceTypes
   * @return Existing resource type
   */
  public static @Nullable String getExistingResourceType(@NotNull ResourceResolver resourceResolver, @NotNull String @NotNull... resourceTypes) {
    for (String path : resourceTypes) {
      if (resourceResolver.getResource(path) != null) {
        return path;
      }
    }
    return null;
  }

  /**
   * Current content path
   * @param request Request
   * @return Current content path or null
   */
  private static @Nullable String getContentPath(@NotNull HttpServletRequest request) {

    String contentPath = (String)request.getAttribute(Value.CONTENTPATH_ATTRIBUTE);

    // if we are currently in create page wizard try to extract content path from referer,
    // as it is not available via other ways
    if (!isValidContentPath(contentPath)) {
      if (StringUtils.contains(request.getRequestURI(), CREATEPAGEWITZARD_PROPERTIES_URI)) {
        String referer = request.getHeader(HEADER_REFERER);
        if (referer != null && StringUtils.contains(referer, CREATEPAGEWITZARD_URI)) {
          contentPath = StringUtils.substringAfter(referer, CREATEPAGEWITZARD_URI);
        }
      }
    }

    if (!isValidContentPath(contentPath)) {
      // fallback to suffix if CONTENTPATH_ATTRIBUTE is not set
      // (e.g. in inside a /libs/granite/ui/components/foundation/form/multifield component)
      contentPath = ((SlingHttpServletRequest)request).getRequestPathInfo().getSuffix();
    }

    if (!isValidContentPath(contentPath)) {
      // fallback to suffix item parameter in query string
      // (e.g. in inside a /libs/granite/ui/components/foundation/form/multifield component)
      contentPath = request.getParameter("item");
    }

    if (!isValidContentPath(contentPath)) {
      contentPath = null;
    }

    return contentPath;
  }

  private static boolean isValidContentPath(@Nullable String path) {
    if (path == null) {
      return false;
    }
    return StringUtils.startsWith(path, "/");
  }

  private static Resource getContentResourceOrParentFromPath(SlingHttpServletRequest slingRequest, String contentPath) {
    if (StringUtils.isNotEmpty(contentPath)) {
      Resource contentResource = slingRequest.getResourceResolver().getResource(contentPath);
      if (contentResource != null) {
        return contentResource;
      }
      else {
        return getContentResourceOrParentFromPath(slingRequest, ResourceUtil.getParent(contentPath));
      }
    }
    return null;
  }

}
