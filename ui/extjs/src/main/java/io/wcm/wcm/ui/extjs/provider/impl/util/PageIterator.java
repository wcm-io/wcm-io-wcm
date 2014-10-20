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
package io.wcm.wcm.ui.extjs.provider.impl.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;

import com.day.cq.commons.Filter;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.Page;

/**
 * Implements an iterator that returns page objects.
 * If it hits a resource of with primary type sling:Folder or sling:OrderedFolder it supports them
 * as well returning a simulates page object for them.
 */
public final class PageIterator implements Iterator<Page> {

  /**
   * Next element available
   */
  private Page next;

  /**
   * Underlying resource iterator
   */
  private final Iterator<Resource> resources;

  /**
   * the filter to use
   */
  private final Filter<Page> pageFilter;

  /**
   * Creates a new iterator that is based on the given node iterator.
   * @param resources base iterator
   * @param pageFilter iteration filter
   */
  public PageIterator(Iterator<Resource> resources, Filter<Page> pageFilter) {
    this.resources = resources;
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
    while (resources.hasNext() && next == null) {
      Resource nextResource = resources.next();
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
