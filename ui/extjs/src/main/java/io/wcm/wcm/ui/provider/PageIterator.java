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
package io.wcm.wcm.ui.provider;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import com.day.cq.commons.Filter;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.Page;

/**
 * Implements an iterator that returns page objects.
 */
class PageIterator implements Iterator<Page> {

  /**
   * Next element available
   */
  private Page next;

  /**
   * Resource resolver for resolving page resources
   */
  private final ResourceResolver resourceResolver;

  /**
   * Underlying node iterator
   */
  private final NodeIterator nodes;

  /**
   * the filter to use
   */
  private final Filter<Page> pageFilter;

  /**
   * Creates a new iterator that is based on the given node iterator.
   * @param nodes base iterator
   * @param resourceResolver resource resolver
   * @param pageFilter iteration filter
   */
  public PageIterator(NodeIterator nodes, ResourceResolver resourceResolver, Filter<Page> pageFilter) {
    this.nodes = nodes;
    this.resourceResolver = resourceResolver;
    this.pageFilter = pageFilter;
    seek();
  }

  /**
   * Seeks the next available page
   * @return the previous element
   */
  private Page seek() {
    Page prev = next;
    next = null;
    while (nodes.hasNext() && next == null) {
      Node node = nodes.nextNode();
      try {
        Resource nextResource = resourceResolver.getResource(node.getPath());
        next = nextResource.adaptTo(Page.class);

        if (next == null) {
          // handle sling:Folder and sling:OrderedFolder as "virtual pages" to allow browsing pages below them
          String primaryType = nextResource.getValueMap().get(JcrConstants.JCR_PRIMARYTYPE, String.class);
          if (StringUtils.equals(primaryType, "sling:Folder") || StringUtils.equals(primaryType, "sling:OrderedFolder")) {
            next = new SlingFolderVirtualPage(nextResource);
          }
        }

        if (next != null && pageFilter != null && !pageFilter.includes(next)) {
          next = null;
        }
      }
      catch (RepositoryException e) {
        // skip
      }
    }
    return prev;
  }

  @Override
  public boolean hasNext() {
    return next != null;
  }

  @Override
  public Page next() {
    if (next == null) {
      throw new NoSuchElementException();
    }
    return seek();
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

}
