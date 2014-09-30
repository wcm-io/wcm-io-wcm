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

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.PropertyOption;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.jcr.JcrConstants;
import com.google.common.collect.ImmutableSet;

/**
 * Factory configuration provider for OSGi parsys configuration.
 * TODO: add unit tests
 */
@Component(immediate = true, metatype = true, configurationFactory = true, policy = ConfigurationPolicy.REQUIRE,
label = "wcm.io Paragraph System Configuration Extension",
description = "Extends configurations of allowed components for wcm.io paragraph systems.")
@Service(ParsysConfig.class)
public final class OsgiParsysConfigProvider implements ParsysConfig {

  private static final Logger log = LoggerFactory.getLogger(OsgiParsysConfigProvider.class);

  private static final int DEFAULT_PARENT_ANCESTOR_LEVEL = 1;

  @Property(label = "Page Component Path",
      description = "Resource type of the page component for this parsys config (required).")
  private static final String PROPERTY_PAGE_COMPONENT_PATH = "pageComponentPath";

  @Property(label = "Path",
      description = "Parsys node name (e.g. 'content') or parsys path relative to page "
          + "(should start with 'jcr:content/'). Path will be ignored if a pattern is defined.")
  private static final String PROPERTY_PATH = "path";

  @Property(label = "Path Pattern",
      description = "Regular expression that matches parsys path within the page, "
          + "e.g. '^jcr:content/.*$'. Leave empty if you want to use the Path property.")
  private static final String PROPERTY_PATH_PATTERN = "pathPattern";

  @Property(label = "Allowed Children",
      description = "Resource types of the allowed components in this paragraph system",
      cardinality = Integer.MAX_VALUE)
  private static final String PROPERTY_ALLOWED_CHILDREN = "allowedChildren";

  @Property(label = "Denied Children",
      description = "Resource types of the denied components in this paragraph system",
      cardinality = Integer.MAX_VALUE)
  private static final String PROPERTY_DENIED_CHILDREN = "deniedChildren";

  @Property(label = "Allowed Parents",
      description = "(optional) Resource types of parsys parent components. "
          + "You can limit the context of parsys where child components can be added by configuratiion of allowed parent components.",
          cardinality = Integer.MAX_VALUE)
  private static final String PROPERTY_ALLOWED_PARENTS = "allowedParents";

  @Property(label = "Parent Ancestor Level",
      description = "(optional) Indicates the ancestor level, where allowed parents should match.",
      intValue = DEFAULT_PARENT_ANCESTOR_LEVEL,
      options = {
      @PropertyOption(name = "1", value = "Direct Parent (1)"),
      @PropertyOption(name = "2", value = "Grand Parent (2)")
  })
  private static final String PROPERTY_PARENT_ANCESTOR_LEVEL = "parentAncestorLevel";

  private String pageComponentPath;
  private Pattern pathPattern;
  private int parentAncestorLevel;
  private Set<String> allowedParents;
  private Set<String> allowedChildren;
  private Set<String> deniedChildren;

  @Override
  public String getPageComponentPath() {
    return this.pageComponentPath;
  }

  @Override
  public Pattern getPattern() {
    return this.pathPattern;
  }

  @Override
  public int getParentAncestorLevel() {
    return this.parentAncestorLevel;
  }

  @Override
  public Set<String> getAllowedParents() {
    return this.allowedParents;
  }

  @Override
  public Set<String> getAllowedChildren() {
    return this.allowedChildren;
  }

  @Override
  public Set<String> getDeniedChildren() {
    return this.deniedChildren;
  }

  // --- SCR Integration ---

  protected void activate(ComponentContext pOsgiContext) {
    @SuppressWarnings("unchecked")
    final Dictionary<String, Object> props = pOsgiContext.getProperties();

    // read config properties
    this.pageComponentPath = PropertiesUtil.toString(props.get(PROPERTY_PAGE_COMPONENT_PATH), null);
    String path = PropertiesUtil.toString(props.get(PROPERTY_PATH), null);
    String patternString = PropertiesUtil.toString(props.get(PROPERTY_PATH_PATTERN), null);
    String[] allowedChildrenArray = PropertiesUtil.toStringArray(props.get(PROPERTY_ALLOWED_CHILDREN), null);
    String[] deniedChildrenArray = PropertiesUtil.toStringArray(props.get(PROPERTY_DENIED_CHILDREN), null);
    String[] allowedParentsArray = PropertiesUtil.toStringArray(props.get(PROPERTY_ALLOWED_PARENTS), null);
    this.parentAncestorLevel = PropertiesUtil.toInteger(props.get(PROPERTY_PARENT_ANCESTOR_LEVEL), DEFAULT_PARENT_ANCESTOR_LEVEL);

    // set path pattern if any
    if (StringUtils.isNotEmpty(patternString)) {
      this.pathPattern = Pattern.compile(patternString);
    }
    // alternative: use path to build a pattern
    else if (StringUtils.isNotBlank(path)) {
      // path may also contain a simple node name
      if (!StringUtils.startsWith(path, JcrConstants.JCR_CONTENT + "/")) {
        path = JcrConstants.JCR_CONTENT + "/" + path;
      }
      this.pathPattern = Pattern.compile("^" + Pattern.quote(path) + "$");
    }

    // set allowed children
    Set<String> allowedChildrenSet = new HashSet<>();
    if (allowedChildrenArray != null) {
      for (String resourceType : allowedChildrenArray) {
        if (StringUtils.isNotBlank(resourceType)) {
          allowedChildrenSet.add(ResourceTypeUtil.makeAbsolute(resourceType));
        }
      }
    }
    this.allowedChildren = ImmutableSet.copyOf(allowedChildrenSet);

    // set denied children
    Set<String> deniedChildrenSet = new HashSet<>();
    if (deniedChildrenArray != null) {
      for (String resourceType : deniedChildrenArray) {
        if (StringUtils.isNotBlank(resourceType)) {
          deniedChildrenSet.add(ResourceTypeUtil.makeAbsolute(resourceType));
        }
      }
    }
    this.deniedChildren = ImmutableSet.copyOf(deniedChildrenSet);

    // set allowed parents
    Set<String> allowedParentsSet = new HashSet<>();
    if (allowedParentsArray != null) {
      for (String resourceType : allowedParentsArray) {
        if (StringUtils.isNotBlank(resourceType)) {
          allowedParentsSet.add(ResourceTypeUtil.makeAbsolute(resourceType));
        }
      }
    }
    this.allowedParents = ImmutableSet.copyOf(allowedParentsSet);

    if (log.isDebugEnabled()) {
      log.debug(getClass().getSimpleName() + ": "
          + PROPERTY_PAGE_COMPONENT_PATH + "={}, "
          + PROPERTY_PATH + "={}, "
          + PROPERTY_PATH_PATTERN + "={}, "
          + PROPERTY_ALLOWED_CHILDREN + "={}, "
          + PROPERTY_DENIED_CHILDREN + "={}, "
          + PROPERTY_ALLOWED_PARENTS + "={}, "
          + PROPERTY_PARENT_ANCESTOR_LEVEL + "={}",
          new Object[] {
        this.pageComponentPath,
        path,
        this.pathPattern,
        this.allowedChildren,
        this.deniedChildren,
        this.allowedParents,
        this.parentAncestorLevel
      }
          );
    }

    // validation messages
    if (StringUtils.isBlank(this.pageComponentPath)) {
      log.warn(PROPERTY_PAGE_COMPONENT_PATH + " cannot be null or empty. This configuration will be ignored.");
    }
    if (this.pathPattern == null) {
      log.warn("Path pattern cannot be null. Please set the property " + PROPERTY_PATH_PATTERN + " or " + PROPERTY_PATH);
    }
  }

}
