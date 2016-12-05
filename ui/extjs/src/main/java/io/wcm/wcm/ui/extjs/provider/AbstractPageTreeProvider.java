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

import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.osgi.annotation.versioning.ConsumerType;

import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageFilter;

import io.wcm.wcm.ui.extjs.provider.impl.util.PageIterator;

/**
 * Exports the resource tree at the addressed resource in JSON format to the response.
 * This can be used by the <code>io.wcm.wcm.ui.form.BrowseField</code> widget.
 * Abstract implementation, some methods can be overwritten by subclasses.
 */
@ConsumerType
public abstract class AbstractPageTreeProvider extends AbstractPageProvider {
  private static final long serialVersionUID = 1L;

  @Override
  protected JSONArray getJsonContent(Resource rootResource, PageFilter pageFilter) throws JSONException {
    return getPages(listChildren(rootResource, pageFilter), 0, pageFilter);
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

}
