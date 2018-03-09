/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2018 wcm.io
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
package io.wcm.wcm.commons.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.util.Text;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.tenant.Tenant;
import org.osgi.annotation.versioning.ProviderType;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

/**
 * Handling of paths and absolute parents in AEM.
 * <p>
 * The methods implement special handling for AEM features:
 * <p>
 * <ul>
 * <li>Side-by-side version comparison (at <code>/content/versionhistory</code>)</li>
 * <li>Launches (at <code>/content/launches</code>)</li>
 * </ul>
 * Paths starting with one of these special paths are treated in a special way so code relying on the original path
 * structure still works.
 */
@ProviderType
public final class Path {

  /**
   * Path for storing version history for side-by-side comparison.
   */
  public static final String VERSION_HISTORY = "/content/versionhistory";

  private static final Pattern VERSION_HISTORY_PATTERN = Pattern.compile(VERSION_HISTORY + "/[^/]+(/.*)");
  private static final Pattern VERSION_HISTORY_TENANT_PATTERN = Pattern.compile(VERSION_HISTORY + "/[^/]+/[^/]+(/.*)");

  private Path() {
    // static methods only
  }

  /**
   * Get absolute parent of given path.
   * If the path is below <code>/content/versionhistory</code> or <code>/content/launches</code> the path level is
   * adjusted accordingly.
   * This is a replacement for {@link com.day.text.Text#getAbsoluteParent(String, int)}.
   * @param path Path
   * @param parentLevel Parent level
   * @param resourceResolver Resource resolver
   * @return Absolute parent path or empty string if path is invalid
   */
  public static String getAbsoluteParent(String path, int parentLevel, ResourceResolver resourceResolver) {
    if (parentLevel < 0) {
      return "";
    }
    int level = parentLevel + getParentLevelOffset(path, resourceResolver);
    return Text.getAbsoluteParent(path, level);
  }

  /**
   * Get absolute parent of given path.
   * If the path is below <code>/content/versionhistory</code> or <code>/content/launches</code> the path level is
   * adjusted accordingly.
   * This is a replacement for {@link Page#getAbsoluteParent(int)}.
   * @param page Page
   * @param parentLevel Parent level
   * @param resourceResolver Resource resolver
   * @return Absolute parent page or null if path is invalid
   */
  public static Page getAbsoluteParent(Page page, int parentLevel, ResourceResolver resourceResolver) {
    PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
    String absoluteParentPath = getAbsoluteParent(page.getPath(), parentLevel, resourceResolver);
    if (StringUtils.isEmpty(absoluteParentPath)) {
      return null;
    }
    return pageManager.getPage(absoluteParentPath);
  }

  /**
   * Gets level from parent use same logic (but reverse) as {@link #getAbsoluteParent(Page, int, ResourceResolver)}.
   * @param path Path
   * @return level &gt;= 0 if path is value, -1 if path is invalid
   */
  public static int getAbsoluteLevel(String path, ResourceResolver resourceResolver) {
    // TODO: respect version history etc.
    if (StringUtils.isEmpty(path) || StringUtils.equals(path, "/")) {
      return -1;
    }
    return StringUtils.countMatches(path, "/") - 1;
  }

  /**
   * Gets original path if the given path points to /content/versionhistory/*.
   * @param path Path
   * @param resourceResolver Resource resolver
   * @return Path without /content/versionhistory rewriting
   */
  public static String getOriginalPath(String path, ResourceResolver resourceResolver) {
    boolean isTenant = isTenant(resourceResolver);
    Matcher matcher = isTenant ? VERSION_HISTORY_TENANT_PATTERN.matcher(path) : VERSION_HISTORY_PATTERN.matcher(path);
    if (matcher.matches()) {
      return "/content" + matcher.group(1);
    }
    else {
      return path;
    }
  }

  /**
   * Calculates offset for parent level if path points ot /content/versionhistory.
   * @param path Path
   * @param resourceResolver Resource resolver
   * @return 0 or offset if in /content/versionhistory
   */
  private static int getParentLevelOffset(String path, ResourceResolver resourceResolver) {
    boolean isTenant = isTenant(resourceResolver);
    Matcher matcher = isTenant ? VERSION_HISTORY_TENANT_PATTERN.matcher(path) : VERSION_HISTORY_PATTERN.matcher(path);
    if (matcher.matches()) {
      return isTenant ? 3 : 2;
    }
    else {
      return 0;
    }
  }

  /**
   * Checks if a tenant is active for the current user.
   * @param resourceResolver Resource resolver
   * @return true if tenant is active
   */
  private static boolean isTenant(ResourceResolver resourceResolver) {
    Tenant tenant = resourceResolver.adaptTo(Tenant.class);
    return tenant != null;
  }

}
