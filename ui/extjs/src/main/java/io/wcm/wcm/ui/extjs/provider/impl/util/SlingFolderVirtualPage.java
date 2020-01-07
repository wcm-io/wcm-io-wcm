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

import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.util.Text;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import com.day.cq.commons.Filter;
import com.day.cq.tagging.Tag;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.Template;
import com.day.cq.wcm.api.WCMException;
import com.day.cq.wcm.commons.DeepResourceIterator;

/**
 * Virtual page implementation for handling sling:Folder and sling:OrderedFolder nodes as pages in {@link PageIterator}
 */
class SlingFolderVirtualPage implements Page {

  private final Resource resource;

  SlingFolderVirtualPage(Resource resource) {
    this.resource = resource;
  }

  @Override
  public String getDescription() {
    return null;
  }

  @SuppressWarnings({ "unchecked", "null" })
  @Override
  public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
    if (type == Resource.class) {
      return (AdapterType)resource;
    }
    else {
      return resource.adaptTo(type);
    }
  }

  @Override
  public boolean canUnlock() {
    return false;
  }

  @Override
  public Page getAbsoluteParent(int level) {
    String parentPath = Text.getAbsoluteParent(resource.getPath(), level - 1);
    Resource parentResource = resource.getResourceResolver().getResource(parentPath);
    if (parentResource != null) {
      return parentResource.adaptTo(Page.class);
    }
    return null;
  }

  @Override
  public Resource getContentResource() {
    return null;
  }

  @Override
  public Resource getContentResource(String path) {
    return null;
  }

  @Override
  public int getDepth() {
    if (StringUtils.equals("/", this.resource.getPath())) {
      return 0;
    }
    else {
      return StringUtils.countMatches(this.resource.getPath(), "/");
    }
  }

  @Override
  public Locale getLanguage(boolean ignoreContent) {
    return null;
  }

  @Override
  public Locale getLanguage() {
    return null;
  }

  @Override
  public Calendar getLastModified() {
    return null;
  }

  @Override
  public String getLastModifiedBy() {
    return null;
  }

  @Override
  public String getLockOwner() {
    return null;
  }

  @Override
  public String getName() {
    return resource.getName();
  }

  @Override
  public String getNavigationTitle() {
    return null;
  }

  @Override
  public Calendar getOffTime() {
    return null;
  }

  @Override
  public Calendar getOnTime() {
    return null;
  }

  @Override
  public PageManager getPageManager() {
    return resource.getResourceResolver().adaptTo(PageManager.class);
  }

  @Override
  public String getPageTitle() {
    return null;
  }

  @Override
  public Page getParent() {
    Resource parentResource = resource.getParent();
    if (parentResource == null) {
      return null;
    }
    return parentResource.adaptTo(Page.class);
  }

  @Override
  public Page getParent(int level) {
    String parentPath = Text.getRelativeParent(resource.getPath(), level);
    Resource parentResource = resource.getResourceResolver().getResource(parentPath);
    if (parentResource != null) {
      return parentResource.adaptTo(Page.class);
    }
    return null;
  }

  @Override
  public String getPath() {
    return resource.getPath();
  }

  @Override
  public ValueMap getProperties() {
    return ValueMap.EMPTY;
  }

  @Override
  public ValueMap getProperties(String path) {
    return null;
  }

  @Override
  public Tag[] getTags() {
    return new Tag[0];
  }

  @Override
  public Template getTemplate() {
    return null;
  }

  @Override
  public String getTitle() {
    return null;
  }

  @Override
  public String getVanityUrl() {
    return null;
  }

  @Override
  public boolean hasChild(String name) {
    return false;
  }

  @Override
  public boolean hasContent() {
    return false;
  }

  @Override
  public boolean isHideInNav() {
    return false;
  }

  @Override
  public boolean isLocked() {
    return false;
  }

  @Override
  public boolean isValid() {
    return false;
  }

  @Override
  public Iterator<Page> listChildren() {
    return listChildren(null);
  }

  @Override
  public Iterator<Page> listChildren(final Filter<Page> filter) {
    return listChildren(filter, false);
  }

  @Override
  public Iterator<Page> listChildren(final Filter<Page> filter, final boolean deep) {
    Iterator<Resource> resources;
    if (deep) {
      resources = new DeepResourceIterator(resource);
    }
    else {
      resources = resource.getResourceResolver().listChildren(resource);
    }

    // return filtered page iterator
    return new PageIterator(resources, filter);
  }

  @Override
  public void lock() throws WCMException {
    throw new UnsupportedOperationException();
  }

  @Override
  public long timeUntilValid() {
    return 0L;
  }

  @Override
  public void unlock() throws WCMException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Calendar getDeleted() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getDeletedBy() {
    throw new UnsupportedOperationException();
  }

}
