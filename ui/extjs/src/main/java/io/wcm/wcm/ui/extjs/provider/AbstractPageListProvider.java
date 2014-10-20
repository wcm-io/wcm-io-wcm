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
import io.wcm.wcm.ui.extjs.provider.impl.servlets.util.PredicatePageFilter;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.ServletException;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONObject;
import org.osgi.annotation.versioning.ConsumerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageFilter;
import com.day.cq.wcm.api.components.ComponentContext;
import com.day.cq.wcm.commons.WCMUtils;

/**
 * Exports the list of child pages of the addressed resource in JSON format to the response.
 * This can be used by the <code>io.wcm.wcm.ui.form.Selection</code> widget.
 * Abstract implementation, some methods can be overwritten by sublcasses.
 */
@ConsumerType
public abstract class AbstractPageListProvider extends SlingSafeMethodsServlet {
  private static final long serialVersionUID = 1L;

  protected final Logger log = LoggerFactory.getLogger(getClass());

  /**
   * Parameter for specifying a predicate to filter the list of pages.
   */
  public static final String RP_PREDICATE = "predicate";

  @Override
  protected final void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
      throws ServletException, IOException {
    try {

      response.setContentType(ContentType.JSON);

      JSONArray pagelist = new JSONArray();
      ComponentContext componentContext = WCMUtils.getComponentContext(request);

      Iterator<Page> pages = componentContext.getPage().listChildren(getPageFilter(request));

      while (pages.hasNext()) {
        Page page = pages.next();

        JSONObject childItem = new JSONObject();
        childItem.put("value", page.getPath());
        childItem.put("text", page.getTitle());
        pagelist.put(childItem);
      }

      response.getWriter().write(pagelist.toString());
    }
    catch (Throwable ex) {
      log.error("Unexpected error, rethrow as servlet exception.", ex);
      throw new ServletException(ex);
    }
  }

  /**
   * Can be overridden by subclasses to filter page list via page filter
   *
   * @param request
   * @return Page filter or null if no filtering applies
   */
  protected PageFilter getPageFilter(SlingHttpServletRequest request) {

    // check for predicate filter
    String predicateName = RequestParam.get(request, RP_PREDICATE);
    if (StringUtils.isNotEmpty(predicateName)) {
      return new PredicatePageFilter(predicateName, request);
    }

    return null;
  }

}
