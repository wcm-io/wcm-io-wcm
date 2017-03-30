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
package io.wcm.wcm.ui.extjs.provider.impl.servlets;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.servlet.Servlet;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.util.Text;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.HttpConstants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import com.day.cq.commons.predicate.PredicateProvider;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageFilter;
import com.day.text.ISO9075;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.wcm.sling.commons.request.RequestParam;
import io.wcm.wcm.commons.contenttype.FileExtension;
import io.wcm.wcm.ui.extjs.provider.AbstractPageTreeProvider;

/**
 * Page tree provider for tree nodes filtered by their templates
 */
@Component(service = Servlet.class, immediate = true, property = {
    "sling.servlet.extensions=" + FileExtension.JSON,
    "sling.servlet.selectors=" + TemplateFilterPageTreeProvider.SELECTOR,
    "sling.servlet.resourceTypes=sling/servlet/default",
    "sling.servlet.methods=" + HttpConstants.METHOD_GET
})
@SuppressFBWarnings("SE_BAD_FIELD")
public class TemplateFilterPageTreeProvider extends AbstractPageTreeProvider {
  private static final long serialVersionUID = 1L;

  /**
   * Define one or multiple template paths to filter the page tree for.
   */
  static final String RP_TEMPLATE = "template";

  static final String SELECTOR = "wcm-io-wcm-ui-extjs-pagetree-templatefilter";

  @Reference(cardinality = ReferenceCardinality.OPTIONAL)
  private PredicateProvider predicateProvider;

  @Override
  protected PredicateProvider getPredicateProvider() {
    return predicateProvider;
  }

  @Override
  protected PageFilter getPageFilter(SlingHttpServletRequest request) {
    Resource rootResource = getRootResource(request);
    String[] templates = RequestParam.getMultiple(request, RP_TEMPLATE);

    Set<String> pagePathsToFollow = getPagePathsForTemplate(templates, rootResource.getPath(), request);

    return new PathPageFilter(pagePathsToFollow);
  }

  /**
   * Get paths for pages that use the given template
   *
   * @param templates
   * @param rootPath
   * @return a set of nodes that should be displayed in the tree
   */
  private Set<String> getPagePathsForTemplate(String[] templates, String rootPath, SlingHttpServletRequest request) {
    Set<String> pagePaths = new HashSet<>();

    if (templates != null && templates.length > 0) {
      try {
        NodeIterator nodes = searchNodesByTemplate(templates, rootPath, request);
        while (nodes.hasNext()) {
          Node node = nodes.nextNode();
          String path = StringUtils.removeEnd(node.getPath(), "/jcr:content");
          pagePaths.add(path);
        }
      }
      catch (RepositoryException ex) {
        log.warn("Seaching nodes by template failed.", ex);
      }
    }

    return pagePaths;
  }

  /**
   * Searches for page content nodes under the {@code pRootPath} with given
   * template It uses a XPATH query and return the node iterator of results.
   *
   * @param templates
   * @param rootPath
   * @return results node iterator
   * @throws RepositoryException
   */
  private NodeIterator searchNodesByTemplate(String[] templates, String rootPath, SlingHttpServletRequest request) throws RepositoryException {
    String queryString = "/jcr:root" + ISO9075.encodePath(rootPath) + "//*"
        + "[@cq:template='" + StringUtils.join(escapeXPathQueryExpressions(templates), "' or @cq:template='") + "']";
    QueryManager queryManager = request.getResourceResolver().adaptTo(Session.class).getWorkspace().getQueryManager();
    @SuppressWarnings("deprecation")
    Query query = queryManager.createQuery(queryString, Query.XPATH);
    QueryResult result = query.execute();
    return result.getNodes();
  }

  /**
   * @param expressions
   * @return array of escaped xPath query expressions
   */
  private String[] escapeXPathQueryExpressions(String[] expressions) {
    List<String> escapedExpressions = new ArrayList<>();
    for (String expr : expressions) {
      escapedExpressions.add(Text.escapeIllegalXpathSearchChars(expr));
    }

    return escapedExpressions.toArray(new String[escapedExpressions.size()]);
  }

  /**
   * page filter that allows only a set of configurable paths
   */
  private static class PathPageFilter extends PageFilter {

    private Set<String> pathsToFollow = new HashSet<>();

    PathPageFilter(Set<String> pagePathsToFollow) {
      if (pagePathsToFollow != null) {
        this.pathsToFollow = pagePathsToFollow;
      }
    }

    @Override
    public boolean includes(Page page) {
      boolean included = false;

      Iterator<String> pathsToFollowIter = pathsToFollow.iterator();
      while (!included && pathsToFollowIter.hasNext()) {
        String pathToFollow = pathsToFollowIter.next();
        included = StringUtils.startsWith(pathToFollow, page.getPath());
      }

      return included;
    }

  }

}
