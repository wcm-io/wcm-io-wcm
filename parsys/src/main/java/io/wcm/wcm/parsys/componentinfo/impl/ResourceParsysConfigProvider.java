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

import io.wcm.wcm.parsys.componentinfo.ParsysConfig;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.util.Text;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

import com.day.cq.commons.jcr.JcrConstants;
import com.google.common.collect.ImmutableSet;

/**
 * Reads paragraph system configuration from page component resource type definition in repository.
 * TODO: add caching for resolved parsys config from resource?
 */
final class ResourceParsysConfigProvider {

  private static final String NN_PARSYSCONFIG = "wcmio:parsysConfig";
  private static final String NN_PATHS = "paths";
  private static final String PN_PATH = "path";
  private static final String PN_PATTERN = "pattern";
  private static final String PN_ALLOWEDCHILDREN = "allowedChildren";
  private static final String PN_ALLOWEDPARENTS = "allowedParents";
  private static final String PN_PARENTANCESTORLEVEL = "parentAncestorLevel";

  private final List<ParsysConfig> pathDefs;

  /**
   * @param pageComponentResource Page component resource
   */
  public ResourceParsysConfigProvider(Resource pageComponentResource) {
    this.pathDefs = getPathDefs(pageComponentResource);
  }

  private static List<ParsysConfig> getPathDefs(Resource pageComponentResource) {
    List<ParsysConfig> pathDefs = new ArrayList<>();

    ResourceResolver resourceResolver = pageComponentResource.getResourceResolver();
    Resource pathsResource = resourceResolver.getResource(pageComponentResource, "./" + NN_PARSYSCONFIG + "/" + NN_PATHS);
    if (pathsResource != null) {
      Iterator<Resource> pathDefResources = resourceResolver.listChildren(pathsResource);
      while (pathDefResources.hasNext()) {
        Resource pathDefResource = pathDefResources.next();
        pathDefs.add(new PathDef(pathDefResource, pageComponentResource.getResourceType()));
      }
    }

    return pathDefs;
  }

  /**
   * @return All path definitions
   */
  public List<ParsysConfig> getPathDefs() {
    return this.pathDefs;
  }

  /**
   * Paragraph System configuration path definition.
   */
  private static class PathDef implements ParsysConfig {

    private final String pageComponentPath;
    private final Pattern pathPattern;
    private final Set<String> allowedChildren;
    private final Set<String> deniedChildren;
    private final Set<String> allowedParents;
    private final int parentAncestorLevel;

    /**
     * @param pathDefResource Path definition resource
     * @param pageComponentPath resource type of page component
     */
    public PathDef(Resource pathDefResource, String pageComponentPath) {
      this.pageComponentPath = pageComponentPath;
      ValueMap pathDefProps = pathDefResource.adaptTo(ValueMap.class);

      // resolve path/path pattern
      String path = pathDefProps.get(PN_PATH, String.class);
      String patternString = pathDefProps.get(PN_PATTERN, String.class);
      if (StringUtils.isNotEmpty(patternString)) {
        this.pathPattern = Pattern.compile(patternString);
      }
      else if (StringUtils.isNotBlank(path)) {
        this.pathPattern = Pattern.compile("^" + Pattern.quote(path) + "$");
      }
      else {
        String name = Text.getName(pathDefResource.getPath());
        this.pathPattern = Pattern.compile("^" + Pattern.quote(JcrConstants.JCR_CONTENT + "/" + name) + "$");
      }

      // get allowed children/parents
      String[] allowedChildrenArray = pathDefProps.get(PN_ALLOWEDCHILDREN, String[].class);
      Set<String> allowedChildrenSet = new HashSet<>();
      if (allowedChildrenArray != null) {
        for (String resourceType : allowedChildrenArray) {
          allowedChildrenSet.add(resourceType);
        }
      }
      this.allowedChildren = ImmutableSet.copyOf(allowedChildrenSet);
      String[] allowedParentsArray = pathDefProps.get(PN_ALLOWEDPARENTS, String[].class);
      Set<String> allowedParentsSet = new HashSet<>();
      if (allowedParentsArray != null) {
        for (String resourceType : allowedParentsArray) {
          allowedParentsSet.add(resourceType);
        }
      }
      this.allowedParents = ImmutableSet.copyOf(allowedParentsSet);

      // ancestor level
      this.parentAncestorLevel = pathDefProps.get(PN_PARENTANCESTORLEVEL, 1);

      // no denied children - this is supported only in OsgiParsysConfig
      this.deniedChildren = ImmutableSet.of();

    }

    @Override
    public String getPageComponentPath() {
      return this.pageComponentPath;
    }

    @Override
    public Pattern getPathPattern() {
      return this.pathPattern;
    }

    /**
     * @return All allowed child component resource types
     */
    @Override
    public Set<String> getAllowedChildren() {
      return this.allowedChildren;
    }

    @Override
    public Set<String> getDeniedChildren() {
      return this.deniedChildren;
    }

    /**
     * @return All allowed parent component resource types
     */
    @Override
    public Set<String> getAllowedParents() {
      return this.allowedParents;
    }

    /**
     * @return Parent ancestor level
     */
    @Override
    public int getParentAncestorLevel() {
      return this.parentAncestorLevel;
    }

    @Override
    public String toString() {
      return this.pathPattern.toString() + ", "
          + "children=[" + StringUtils.join(this.allowedChildren, ",") + "], "
          + "parents=[" + StringUtils.join(this.allowedParents, ",") + "], "
          + "parentAncestorLevel=" + this.parentAncestorLevel;
    }

  }

}
