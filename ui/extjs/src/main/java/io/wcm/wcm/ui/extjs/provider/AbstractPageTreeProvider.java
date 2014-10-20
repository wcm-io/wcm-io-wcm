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
package io.wcm.wcm.ui.extjs.provider;

import io.wcm.sling.commons.request.RequestParam;
import io.wcm.wcm.commons.contenttype.ContentType;
import io.wcm.wcm.ui.extjs.provider.impl.servlets.util.PageIterator;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.ServletException;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.osgi.annotation.versioning.ConsumerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageFilter;

/**
 * Exports the resource tree at the addressed resource in JSON format to the response.
 * This can be used by the <code>io.wcm.wcm.ui.form.BrowseField</code> widget.
 * Abstract implementation, some methods can be overwritten by sublcasses.
 */
@ConsumerType
public abstract class AbstractPageTreeProvider extends SlingSafeMethodsServlet {
  private static final long serialVersionUID = 1L;

  /**
   * Request parameter for passing the path of the root resource to list the children
   */
  public static final String RP_PATH = "path";

  protected final Logger log = LoggerFactory.getLogger(getClass());

  @Override
  protected final void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
      throws ServletException, IOException {

    response.setContentType(ContentType.JSON);

    // determine root resource
    Resource rootResource = getRootResource(request);

    try {
      JSONArray pages;
      if (rootResource != null) {
        PageFilter pageFilter = getPageFilter(request);
        pages = getPages(listChildren(rootResource, pageFilter), 0, pageFilter);
      }
      else {
        pages = new JSONArray();
      }
      response.getWriter().write(pages.toString());
    }
    catch (Throwable ex) {
      log.error("Unexpected error, rethrow as servlet exception.", ex);
      throw new ServletException(ex);
    }
  }

  /**
   * Generate JSON objects for pages.
   * @param pages Child page iterator
   * @return Page array
   * @throws JSONException
   */
  protected final JSONArray getPages(Iterator<Page> pages, int depth, PageFilter pageFilter) throws JSONException {
    JSONArray pagesArray = new JSONArray();

    while (pages.hasNext()) {
      Page page = pages.next();

      // map page attributes to JSON object
      JSONObject pageObject = getPage(page);
      if (pageObject != null) {

        // write children
        Iterator<Page> children = listChildren(page.adaptTo(Resource.class), pageFilter);
        if (!children.hasNext()) {
          pageObject.put("leaf", true);
        }
        else if (depth < getMaxDepth() - 1) {
          pageObject.put("children", getPages(children, depth + 1, pageFilter));
        }

        pagesArray.put(pageObject);
      }
    }

    return pagesArray;
  }

  /**
   * Lists children using custom page iterator.
   * @param parentResource Parent resource
   * @param pageFilter Page filter
   * @return Page iterator
   */
  protected final Iterator<Page> listChildren(Resource parentResource, PageFilter pageFilter) {
    return new PageIterator(parentResource.listChildren(), pageFilter);
  }

  /**
   * Determine root resource to list its children. (use resource for root page because root node does not have to be a
   * page but can be e.g. a nt:folder node)
   * @param request
   * @return Root resource or null if invalid resource was referenced
   */
  protected final Resource getRootResource(SlingHttpServletRequest request) {
    Resource rootResource = request.getResource();
    String path = RequestParam.get(request, RP_PATH);

    if (StringUtils.isNotEmpty(path)) {
      rootResource = request.getResourceResolver().getResource(path);
    }
    return rootResource;
  }

  /**
   * Generate JSON object for page
   * @param page Page
   * @return JSON object
   * @throws JSONException
   */
  protected final JSONObject getPage(Page page) throws JSONException {
    Resource resource = page.adaptTo(Resource.class);

    JSONObject pageObject = new JSONObject();

    // node name
    pageObject.put("name", page.getName());

    // node title text
    String title = page.getTitle();
    if (StringUtils.isEmpty(title)) {
      title = page.getName();
    }
    pageObject.put("text", title);

    // resource type
    pageObject.put("type", resource.getResourceType());

    // template
    String template = page.getProperties().get(NameConstants.PN_TEMPLATE, String.class);
    if (StringUtils.isNotEmpty(template)) {
      pageObject.put("template", template);
    }

    // css class for icon
    pageObject.put("cls", "page");

    return pageObject;
  }

  /**
   * Number of levels to fetch on each request
   * @return Number of levels
   */
  protected int getMaxDepth() {
    return 2;
  }

  /**
   * Can be overridden by subclasses to filter page tree/children via page
   * filter. This method is only called once per request.
   *
   * @param request
   * @return Page filter or null if no filtering applies
   */
  protected PageFilter getPageFilter(SlingHttpServletRequest request) {
    return null;
  }

}
