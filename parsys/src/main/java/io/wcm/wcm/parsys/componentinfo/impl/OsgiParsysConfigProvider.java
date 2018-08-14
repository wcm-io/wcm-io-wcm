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
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.jcr.JcrConstants;
import com.google.common.collect.ImmutableSet;

import io.wcm.wcm.parsys.componentinfo.ParsysConfig;

/**
 * Factory configuration provider for OSGi parsys configuration.
 */
@Component(service = ParsysConfig.class, immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE, property = {
    "webconsole.configurationFactory.nameHint={pageComponentPath}"
})
@Designate(ocd = OsgiParsysConfigProvider.Config.class, factory = true)
@SuppressWarnings("null")
public final class OsgiParsysConfigProvider implements ParsysConfig {

  private static final Logger log = LoggerFactory.getLogger(OsgiParsysConfigProvider.class);

  static final int DEFAULT_PARENT_ANCESTOR_LEVEL = 1;

  @ObjectClassDefinition(name = "wcm.io Paragraph System Configuration Extension",
      description = "Extends configurations of allowed components for wcm.io paragraph systems.")
  @interface Config {

    @AttributeDefinition(name = "Page Component Path",
        description = "Resource type of the page component for this parsys config (required).",
        required = true)
    String pageComponentPath();

    @AttributeDefinition(name = "Path",
        description = "Parsys node name (e.g. 'content') or parsys path relative to page "
            + "(should start with 'jcr:content/'). Path will be ignored if a pattern is defined.")
    String path();

    @AttributeDefinition(name = "Path Pattern",
        description = "Regular expression that matches parsys path within the page, "
            + "e.g. '^jcr:content/.*$'. Leave empty if you want to use the Path property.")
    String pathPattern();

    @AttributeDefinition(name = "Allowed Children",
        description = "Resource types of the allowed components in this paragraph system")
    String[] allowedChildren();

    @AttributeDefinition(name = "Denied Children",
        description = "Resource types of the denied components in this paragraph system")
    String[] deniedChildren();

    @AttributeDefinition(name = "Allowed Parents",
        description = "(optional) Resource types of parsys parent components. "
            + "You can limit the context of parsys where child components can be added by configuratiion of allowed parent components.")
    String[] allowedParents();

    @AttributeDefinition(name = "Parent Ancestor Level",
        description = "(optional) Indicates the ancestor level, where allowed parents should match.",
        options = {
            @Option(value = "1", label = "Direct Parent (1)"),
            @Option(value = "2", label = "Grand Parent (2)")
    })
    int parentAncestorLevel() default DEFAULT_PARENT_ANCESTOR_LEVEL;

    @AttributeDefinition(name = "Inherit",
        description = "Inherit paragraph system configurations from super resource types.")
    boolean inherit() default true;

  }

  private String pageComponentPath;
  private Pattern pathPattern;
  private int parentAncestorLevel;
  private Set<String> allowedParents;
  private Set<String> allowedChildren;
  private Set<String> deniedChildren;
  private boolean inherit;

  @Override
  public @NotNull String getPageComponentPath() {
    return this.pageComponentPath;
  }

  @Override
  public Pattern getPathPattern() {
    return this.pathPattern;
  }

  @Override
  public int getParentAncestorLevel() {
    return this.parentAncestorLevel;
  }

  @Override
  public @NotNull Set<String> getAllowedParents() {
    return this.allowedParents;
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
  public boolean isInherit() {
    return this.inherit;
  }

  // --- SCR Integration ---

  @Activate
  private void activate(Config config) {

    this.pageComponentPath = config.pageComponentPath();
    this.parentAncestorLevel = config.parentAncestorLevel();

    // set path pattern if any
    String pathPatternString = config.pathPattern();
    String path = config.path();
    if (StringUtils.isNotEmpty(pathPatternString)) {
      this.pathPattern = Pattern.compile(pathPatternString);
    }
    // alternative: use path to build a pattern
    else if (StringUtils.isNotBlank(path)) {
      // path may also contain a simple node name
      if (!StringUtils.startsWith(path, JcrConstants.JCR_CONTENT + "/")) {
        path = JcrConstants.JCR_CONTENT + "/" + path; //NOPMD
      }
      this.pathPattern = Pattern.compile("^" + Pattern.quote(path) + "$");
    }

    // set allowed children
    Set<String> allowedChildrenSet = new HashSet<>();
    if (config.allowedChildren() != null) {
      for (String resourceType : config.allowedChildren()) {
        if (StringUtils.isNotBlank(resourceType)) {
          allowedChildrenSet.add(resourceType);
        }
      }
    }
    this.allowedChildren = ImmutableSet.copyOf(allowedChildrenSet);

    // set denied children
    Set<String> deniedChildrenSet = new HashSet<>();
    if (config.deniedChildren() != null) {
      for (String resourceType : config.deniedChildren()) {
        if (StringUtils.isNotBlank(resourceType)) {
          deniedChildrenSet.add(resourceType);
        }
      }
    }
    this.deniedChildren = ImmutableSet.copyOf(deniedChildrenSet);

    // set allowed parents
    Set<String> allowedParentsSet = new HashSet<>();
    if (config.allowedParents() != null) {
      for (String resourceType : config.allowedParents()) {
        if (StringUtils.isNotBlank(resourceType)) {
          allowedParentsSet.add(resourceType);
        }
      }
    }
    this.allowedParents = ImmutableSet.copyOf(allowedParentsSet);

    this.inherit = config.inherit();

    if (log.isDebugEnabled()) {
      log.debug(getClass().getSimpleName() + ": "
          + "pageComponentPath={}, "
          + "path={}, "
          + "pathPattern={}, "
          + "allowedChildren={}, "
          + "deniedChildren={}, "
          + "allowedParents={}, "
          + "parentAncestorLevel={},"
          + "inherit={}",
          new Object[] {
              this.pageComponentPath,
              path,
              this.pathPattern,
              this.allowedChildren,
              this.deniedChildren,
              this.allowedParents,
              this.parentAncestorLevel,
              this.inherit
          }
      );
    }

    // validation messages
    if (StringUtils.isBlank(this.pageComponentPath)) {
      log.warn("pageComponentPath cannot be null or empty. This configuration will be ignored.");
    }
    if (this.pathPattern == null) {
      log.warn("Path pattern cannot be null. Please set the property pathPattern or path.");
    }
  }

}
