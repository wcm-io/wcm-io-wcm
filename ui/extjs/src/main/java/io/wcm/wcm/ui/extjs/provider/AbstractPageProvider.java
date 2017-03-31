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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.osgi.annotation.versioning.ConsumerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.commons.predicate.PredicateProvider;
import com.day.cq.wcm.api.PageFilter;

import io.wcm.sling.commons.request.RequestParam;
import io.wcm.wcm.commons.contenttype.ContentType;
import io.wcm.wcm.ui.extjs.provider.impl.util.PredicatePageFilter;

/**
 * Common functionality for {@link AbstractPageListProvider} and {@link AbstractPageTreeProvider}.
 */
@ConsumerType
public abstract class AbstractPageProvider extends SlingSafeMethodsServlet {
  private static final long serialVersionUID = 1L;

  /**
   * Request parameter for passing the path of the root resource to list the children
   */
  public static final String RP_PATH = "path";

  /**
   * Parameter for specifying a predicate to filter the list of pages.
   */
  public static final String RP_PREDICATE = "predicate";

  protected final Logger log = LoggerFactory.getLogger(getClass());

  /**
   * @return Predicate provider or null if none is available;
   */
  protected abstract PredicateProvider getPredicateProvider();

  @Override
  protected final void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
      throws ServletException, IOException {

    // determine root resource
    Resource rootResource = getRootResource(request);
    if (rootResource == null) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    response.setContentType(ContentType.JSON);

    try {
      PageFilter pageFilter = getPageFilter(request);
      JSONArray jsonContent = getJsonContent(rootResource, pageFilter);
      response.getWriter().write(jsonContent.toString());
    }
    catch (Throwable ex) {
      log.error("Unexpected error, rethrow as servlet exception.", ex);
      throw new ServletException(ex);
    }
  }

  /**
   * Render result of provider servlet as JSON to string writer.
   * @param rootResource Root resource
   * @param pageFilter Page filter
   * @return JSON array
   * @throws JSONException JSON exception
   */
  protected abstract JSONArray getJsonContent(Resource rootResource, PageFilter pageFilter) throws JSONException;

  /**
   * Determine root resource to list its children. (use resource for root page because root node does not have to be a
   * page but can be e.g. a nt:folder node)
   * @param request Request
   * @return Root resource or null if invalid resource was referenced
   */
  protected final Resource getRootResource(SlingHttpServletRequest request) {
    String path = RequestParam.get(request, RP_PATH);
    if (StringUtils.isEmpty(path) && request.getResource() != null) {
      path = request.getResource().getPath();
    }
    if (StringUtils.isNotEmpty(path)) {
      path = StringUtils.removeEnd(path, "/" + JcrConstants.JCR_CONTENT);
      return request.getResourceResolver().getResource(path);
    }
    return null;
  }

  /**
   * Can be overridden by subclasses to filter page list via page filter.
   * If not overridden it supports defining a page filter via "predicate" request attribute.
   * @param request Request
   * @return Page filter or null if no filtering applies
   */
  protected PageFilter getPageFilter(SlingHttpServletRequest request) {

    // check for predicate filter
    String predicateName = RequestParam.get(request, RP_PREDICATE);
    if (StringUtils.isNotEmpty(predicateName)) {
      PredicateProvider predicateProvider = getPredicateProvider();
      if (predicateProvider == null) {
        throw new RuntimeException("PredicateProvider service not available.");
      }
      Predicate predicate = predicateProvider.getPredicate(predicateName);
      if (predicate == null) {
        throw new RuntimeException("Predicate '" + predicateName + "' not available.");
      }
      return new PredicatePageFilter(predicate, true, true);
    }

    return null;
  }

}
