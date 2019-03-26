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

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.google.common.collect.ImmutableSet;

import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.wcm.parsys.componentinfo.AllowedComponentsProvider;
import io.wcm.wcm.parsys.componentinfo.ParsysConfig;
import io.wcm.wcm.parsys.componentinfo.ParsysConfigManager;

/**
 * Detects allowed components for authoring for a given page/resource context.
 */
@Component(service = AllowedComponentsProvider.class, immediate = true)
public final class AllowedComponentsProviderImpl implements AllowedComponentsProvider {

  @Reference
  private ParsysConfigManager parsysConfigManager;

  /**
   * Get allowed components for given resource path
   * @param resourcePath Resource path inside content page
   * @return Set of component paths (absolute resource types)
   */
  @Override
  public @NotNull Set<String> getAllowedComponents(@NotNull String resourcePath, @NotNull ResourceResolver resolver) {
    PageManager pageManager = AdaptTo.notNull(resolver, PageManager.class);
    Page page = pageManager.getContainingPage(resourcePath);
    if (page == null && StringUtils.contains(resourcePath, "/" + JcrConstants.JCR_CONTENT)) {
      // if resource does not exist (e.g. inherited parsys) get page from resource path manually
      page = pageManager.getPage(StringUtils.substringBefore(resourcePath, "/" + JcrConstants.JCR_CONTENT));
    }
    if (page == null) {
      return ImmutableSet.of();
    }
    String relativePath = StringUtils.substringAfter(resourcePath, page.getPath() + "/");
    return getAllowedComponents(page, relativePath, null, resolver);
  }

  /**
   * Get allowed components for a specific resource path inside a page.
   * @param page Page
   * @param relativeResourcePath Relative resource path inside the page
   * @param resourceType Resource type of the paragraph system
   * @param resolver Resource resolver
   * @return Component paths
   */
  @Override
  public @NotNull Set<String> getAllowedComponents(@NotNull Page page, @NotNull String relativeResourcePath,
      @Nullable String resourceType, @NotNull ResourceResolver resolver) {
    Set<String> allowedComponents = new HashSet<>();
    Set<String> deniedComponents = new HashSet<>();

    String pageComponentPath = page.getContentResource().getResourceType();

    Iterable<ParsysConfig> parSysConfigs = parsysConfigManager.getParsysConfigs(pageComponentPath, relativeResourcePath, resolver);

    Resource parentResource = null;
    Resource grandParentResource = null;

    for (ParsysConfig pathDef : parSysConfigs) {

      boolean includePathDef = false;
      if (pathDef.getAllowedParents().size() == 0) {
        includePathDef = true;
      }
      else {
        String checkResourceType = null;
        if (pathDef.getParentAncestorLevel() == 1) {
          if (resourceType != null) {
            checkResourceType = resourceType;
          }
          else if (parentResource == null) {
            parentResource = resolver.getResource(page.getPath() + "/" + relativeResourcePath);
            if (parentResource != null) {
              checkResourceType = parentResource.getResourceType();
            }
          }
        }
        else if (pathDef.getParentAncestorLevel() == 2) {
          if (grandParentResource == null) {
            grandParentResource = resolver.getResource(page.getPath() + "/" + relativeResourcePath + "/..");
          }
          if (grandParentResource != null) {
            checkResourceType = grandParentResource.getResourceType();
          }
        }
        if (checkResourceType != null) {
          includePathDef = pathDef.getAllowedParents().contains(checkResourceType);
        }
      }

      if (includePathDef) {
        allowedComponents.addAll(pathDef.getAllowedChildren());
        deniedComponents.addAll(pathDef.getDeniedChildren());
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
  public @NotNull Set<String> getAllowedComponentsForTemplate(@NotNull String pageComponentPath, @NotNull ResourceResolver resolver) {
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
