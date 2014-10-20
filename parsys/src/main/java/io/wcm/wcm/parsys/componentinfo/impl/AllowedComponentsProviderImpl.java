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
package io.wcm.wcm.parsys.componentinfo.impl;

import io.wcm.wcm.parsys.componentinfo.AllowedComponentsProvider;
import io.wcm.wcm.parsys.componentinfo.ParsysConfig;
import io.wcm.wcm.parsys.componentinfo.ParsysConfigManager;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.google.common.collect.ImmutableSet;

/**
 * Detects allowed components for authoring for a given page/resource context.
 */
@Component(immediate = true)
@Service(AllowedComponentsProvider.class)
public final class AllowedComponentsProviderImpl implements AllowedComponentsProvider {

  @Reference
  private ParsysConfigManager parsysConfigManager;

  /**
   * Get allowed components for given resource path
   * @param resourcePath Resource path inside content page
   * @return Set of component paths (absolute resource types)
   */
  @Override
  public Set<String> getAllowedComponents(String resourcePath, ResourceResolver resolver) {
    Set<String> allowedComponents = new HashSet<>();
    Set<String> deniedComponents = new HashSet<>();

    PageManager pageManager = resolver.adaptTo(PageManager.class);
    Page page = pageManager.getContainingPage(resourcePath);
    if (page == null && StringUtils.contains(resourcePath, "/" + JcrConstants.JCR_CONTENT)) {
      // if resource does not exist (e.g. inherited parsys) get page from resource path manually
      page = pageManager.getPage(StringUtils.substringBefore(resourcePath, "/" + JcrConstants.JCR_CONTENT));
    }
    if (page != null) {
      String pageComponentPath = page.getContentResource().getResourceType();
      String relativePath = resourcePath.substring(page.getPath().length() + 1);

      Iterable<ParsysConfig> parSysConfigs = parsysConfigManager.getParsysConfigs(pageComponentPath, relativePath, resolver);

      Resource parentResource = null;
      Resource grandParentResource = null;

      for (ParsysConfig pathDef : parSysConfigs) {

        boolean includePathDef = false;
        if (pathDef.getAllowedParents().size() == 0) {
          includePathDef = true;
        }
        else {
          Resource checkResource = null;
          if (pathDef.getParentAncestorLevel() == 1) {
            if (parentResource == null) {
              parentResource = resolver.getResource(resourcePath);
            }
            checkResource = parentResource;
          }
          if (pathDef.getParentAncestorLevel() == 2) {
            if (grandParentResource == null) {
              grandParentResource = resolver.getResource(resourcePath + "/..");
            }
            checkResource = grandParentResource;
          }
          if (checkResource != null) {
            String resourceType = ResourceTypeUtil.makeAbsolute(checkResource.getResourceType());
            includePathDef = pathDef.getAllowedParents().contains(resourceType);
          }
        }

        if (includePathDef) {
          allowedComponents.addAll(pathDef.getAllowedChildren());
          deniedComponents.addAll(pathDef.getDeniedChildren());
        }

      }

    }

    // filter out denied components
    allowedComponents.removeAll(deniedComponents);

    return allowedComponents;
  }

  /**
   * Get all allowed components for a template (not respecting any path constraints)
   * @param pageComponentPath Path of template's page component
   * @return Set of component paths (absolute resource types)
   */
  @Override
  public Set<String> getAllowedComponentsForTemplate(String pageComponentPath, ResourceResolver resolver) {
    Resource pageComponentResource = resolver.getResource(pageComponentPath);
    if (pageComponentResource != null) {
      Iterable<ParsysConfig> parSysConfigs = parsysConfigManager.getParsysConfigs(pageComponentResource.getPath(), resolver);

      SortedSet<String> allowedChildren = new TreeSet<>();
      for (ParsysConfig parSysConfig : parSysConfigs) {
        allowedChildren.addAll(parSysConfig.getAllowedChildren());
      }

      return allowedChildren;
    }
    // fallback
    return ImmutableSet.of();
  }

}
