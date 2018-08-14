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

import static io.wcm.wcm.parsys.ParsysNameConstants.NN_PARSYS_CONFIG;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.util.Text;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.jetbrains.annotations.NotNull;

import com.day.cq.commons.jcr.JcrConstants;
import com.google.common.collect.ImmutableSet;

import io.wcm.wcm.parsys.componentinfo.ParsysConfig;

/**
 * Reads paragraph system configuration from page component resource type definition in repository.
 * TODO: add caching for resolved parsys config from resource?
 */
final class ResourceParsysConfigProvider {

  private static final String NN_PATHS = "paths";
  private static final String PN_PATH = "path";
  private static final String PN_PATTERN = "pattern";
  private static final String PN_ALLOWEDCHILDREN = "allowedChildren";
  private static final String PN_DENIEDDCHILDREN = "deniedChildren";
  private static final String PN_ALLOWEDPARENTS = "allowedParents";
  private static final String PN_PARENTANCESTORLEVEL = "parentAncestorLevel";
  private static final String PN_INHERIT = "inherit";

  private final List<ParsysConfig> pathDefs;

  /**
   * @param pageComponentResource Page component resource
   */
  ResourceParsysConfigProvider(Resource pageComponentResource) {
    this.pathDefs = getPathDefs(pageComponentResource);
  }

  private static List<ParsysConfig> getPathDefs(Resource pageComponentResource) {
    List<ParsysConfig> pathDefs = new ArrayList<>();

    ResourceResolver resourceResolver = pageComponentResource.getResourceResolver();
    Resource pathsResource = resourceResolver.getResource(pageComponentResource, "./" + NN_PARSYS_CONFIG + "/" + NN_PATHS);
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
  @SuppressWarnings("null")
  private static class PathDef implements ParsysConfig {

    private final String pageComponentPath;
    private final Pattern pathPattern;
    private final Set<String> allowedChildren;
    private final Set<String> deniedChildren;
    private final Set<String> allowedParents;
    private final int parentAncestorLevel;
    private final boolean inheritFromSuperType;

    /**
     * @param pathDefResource Path definition resource
     * @param pageComponentPath resource type of page component
     */
    PathDef(Resource pathDefResource, String pageComponentPath) {
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

      // get allowed children/denied children/parents
      this.allowedChildren = ImmutableSet.copyOf(pathDefProps.get(PN_ALLOWEDCHILDREN, ArrayUtils.EMPTY_STRING_ARRAY));
      this.deniedChildren = ImmutableSet.copyOf(pathDefProps.get(PN_DENIEDDCHILDREN, ArrayUtils.EMPTY_STRING_ARRAY));
      this.allowedParents = ImmutableSet.copyOf(pathDefProps.get(PN_ALLOWEDPARENTS, ArrayUtils.EMPTY_STRING_ARRAY));

      // ancestor level
      this.parentAncestorLevel = pathDefProps.get(PN_PARENTANCESTORLEVEL, 1);

      // inherit from supertype
      this.inheritFromSuperType = pathDefProps.get(PN_INHERIT, true);

    }

    @Override
    public @NotNull String getPageComponentPath() {
      return this.pageComponentPath;
    }

    @Override
    public Pattern getPathPattern() {
      return this.pathPattern;
    }

    @Override
    public @NotNull Set<String> getAllowedChildren() {
      return this.allowedChildren;
    }

    @Override
    public @NotNull Set<String> getDeniedChildren() {
      return this.deniedChildren;
    }

    @Override
    public @NotNull Set<String> getAllowedParents() {
      return this.allowedParents;
    }

    @Override
    public int getParentAncestorLevel() {
      return this.parentAncestorLevel;
    }

    @Override
    public boolean isInherit() {
      return this.inheritFromSuperType;
    }

    @Override
    public String toString() {
      return this.pathPattern.toString() + ", "
          + "allowedChildren=[" + StringUtils.join(this.allowedChildren, ",") + "], "
          + "deniedChildren=[" + StringUtils.join(this.deniedChildren, ",") + "], "
          + "allowedParents=[" + StringUtils.join(this.allowedParents, ",") + "], "
          + "parentAncestorLevel=" + this.parentAncestorLevel + ","
          + "inheritFromSuperType=" + this.inheritFromSuperType;
    }

  }

}
