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

import com.adobe.granite.ui.components.Value;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

/**
 * Helper methods for GraniteUI components.
 */
public final class GraniteUi {

  private GraniteUi() {
    // static methods only
  }

  /**
   * Current content resource
   * @param request Request
   * @return Current content resource or null
   */
  public static Resource getContentResource(HttpServletRequest request) {
    SlingHttpServletRequest slingRequest = (SlingHttpServletRequest)request;
    String contentPath = (String)request.getAttribute(Value.CONTENTPATH_ATTRIBUTE);
    if (contentPath != null) {
      return slingRequest.getResourceResolver().getResource(contentPath);
    }
    // fallback to suffix if CONTENTPATH_ATTRIBUTE is not set
    // (e.g. in inside a /libs/granite/ui/components/foundation/form/multifield component)
    contentPath = ((SlingHttpServletRequest)request).getRequestPathInfo().getSuffix();
    if (StringUtils.isNotEmpty(contentPath)) {
      return slingRequest.getResourceResolver().getResource(contentPath);
    }
    return null;
  }

  /**
   * Current content page
   * @param request Request
   * @return Current content page or null
   */
  public static Page getContentPage(HttpServletRequest request) {
    SlingHttpServletRequest slingRequest = (SlingHttpServletRequest)request;
    Resource contentResource = getContentResource(request);
    if (contentResource != null) {
      PageManager pageManager = slingRequest.getResourceResolver().adaptTo(PageManager.class);
      return pageManager.getContainingPage(contentResource);
    }
    else {
      return null;
    }
  }

}
