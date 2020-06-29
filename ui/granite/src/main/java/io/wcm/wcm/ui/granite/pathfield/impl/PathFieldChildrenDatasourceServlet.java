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
package io.wcm.wcm.ui.granite.pathfield.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.PredicateUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.iterators.FilterIterator;
import org.apache.commons.collections.iterators.TransformIterator;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.ui.components.Config;
import com.adobe.granite.ui.components.ExpressionHelper;
import com.adobe.granite.ui.components.ExpressionResolver;
import com.adobe.granite.ui.components.PagingIterator;
import com.adobe.granite.ui.components.ds.AbstractDataSource;
import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.EmptyDataSource;
import com.day.cq.commons.predicate.PredicateProvider;

import io.wcm.wcm.ui.granite.pathfield.impl.predicate.HideInternalContentPathsPredicate;
import io.wcm.wcm.ui.granite.pathfield.impl.util.DummyPageContext;
import io.wcm.wcm.ui.granite.pathfield.impl.util.PredicatedResourceWrapper;

/**
 * Servlet implementing the data source for the path field widget.
 */
@Component(service = Servlet.class)
@SlingServletResourceTypes(
    resourceTypes = PathFieldChildrenDatasourceServlet.RESOURCE_TYPE)
public class PathFieldChildrenDatasourceServlet extends SlingSafeMethodsServlet {
  private static final long serialVersionUID = 1L;

  static final String RESOURCE_TYPE = "wcm-io/wcm/ui/granite/components/form/pathfield/datasources/children";

  @Reference
  private ExpressionResolver expressionResolver;
  @Reference
  private PredicateProvider predicateProvider;

  private static final Logger log = LoggerFactory.getLogger(PathFieldChildrenDatasourceServlet.class);

  @Override
  @SuppressWarnings("null")
  protected void doGet(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response)
      throws ServletException, IOException {

    final ExpressionHelper ex = new ExpressionHelper(expressionResolver, new DummyPageContext(request, response));
    final Config cfg = new Config(request.getResource().getChild(Config.DATASOURCE));

    final String query = ex.getString(cfg.get("query", String.class));

    final String parentPath;
    final String searchName;

    if (query != null) {
      final String rootPath = ex.getString(cfg.get("rootPath", "/"));

      final int slashIndex = query.lastIndexOf('/');
      if (slashIndex < 0) {
        parentPath = rootPath;
        searchName = query.toLowerCase();
      }
      else if (!query.startsWith(rootPath)) {
        parentPath = rootPath;
        searchName = null;
      }
      else if (slashIndex == query.length() - 1) {
        parentPath = query;
        searchName = null;
      }
      else {
        parentPath = query.substring(0, slashIndex + 1);
        searchName = query.substring(slashIndex + 1).toLowerCase();
      }
    }
    else {
      parentPath = ex.getString(cfg.get("path", String.class));
      searchName = null;
    }

    final Resource parent = request.getResourceResolver().getResource(parentPath);

    final DataSource ds;
    if (parent == null) {
      ds = EmptyDataSource.instance();
    }
    else {
      final Integer offset = ex.get(cfg.get("offset", String.class), Integer.class);
      final Integer limit = ex.get(cfg.get("limit", String.class), Integer.class);
      final String itemResourceType = cfg.get("itemResourceType", String.class);
      final String[] filter = new String[] { ex.get(cfg.get("filter", "hierarchyNotFile"), String.class) };

      final Collection<Predicate> predicates = new ArrayList<>();
      predicates.add(new HideInternalContentPathsPredicate());
      predicates.addAll(toPredicates(filter));

      if (searchName != null) {
        final Pattern searchNamePattern = Pattern.compile(Pattern.quote(searchName), Pattern.CASE_INSENSITIVE);
        predicates.add(new Predicate() {
          @Override
          public boolean evaluate(Object obj) {
            Resource r = (Resource)obj;
            return searchNamePattern.matcher(r.getName()).lookingAt();
          }
        });
      }

      final Predicate predicate = PredicateUtils.allPredicate(predicates);
      final Transformer transformer = createTransformer(itemResourceType, predicate);

      DataSource datasource = new AbstractDataSource() {
        @Override
        @SuppressWarnings("unchecked")
        public Iterator<Resource> iterator() {
          List<Resource> list = IteratorUtils.toList(new FilterIterator(parent.listChildren(), predicate));

          // sort result set alphabetically - but only if parent node does not have orderable child nodes
          if (!isOrderableChildNodes(parent)) {
            Collections.sort(list, new Comparator<Resource>() {
              @Override
              public int compare(Resource r1, Resource r2) {
                return r1.getName().compareTo(r2.getName());
              }
            });
          }

          return new TransformIterator(new PagingIterator<>(list.iterator(), offset, limit), transformer);
        }
      };

      ds = datasource;
    }

    request.setAttribute(DataSource.class.getName(), ds);
  }

  private List<Predicate> toPredicates(@NotNull String[] filter) {
    if (filter == null) {
      return Collections.emptyList();
    }
    return Arrays.asList(filter).stream()
        .filter(Objects::nonNull)
        .map(item -> {
          Predicate predicate = predicateProvider.getPredicate(item);
          if (predicate != null) {
            return predicate;
          }
          else {
            log.warn("Unable to find predicate implementation for filter: {}", item);
            return null;
          }
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  private static Transformer createTransformer(final String itemResourceType, final Predicate predicate) {
    return new Transformer() {
      @Override
      public Resource transform(Object r) {
        return new PredicatedResourceWrapper((Resource)r, predicate) {
          @Override
          public String getResourceType() {
            if (itemResourceType == null) {
              return super.getResourceType();
            }
            return itemResourceType;
          }
        };
      }
    };
  }

  private static boolean isOrderableChildNodes(Resource resource) {
    Node node = resource.adaptTo(Node.class);
    if (node != null) {
      try {
        return node.getPrimaryNodeType().hasOrderableChildNodes();
      }
      catch (RepositoryException ex) {
        return false;
      }
    }
    return false;
  }

}
