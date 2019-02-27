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
import org.jetbrains.annotations.NotNull;
import org.osgi.annotation.versioning.ProviderType;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

/**
 * Handling of paths and absolute parents in AEM.
 * <p>
 * The methods implement special handling for AEM features:
 * </p>
 * <ul>
 * <li>Side-by-side version comparison (at <code>/tmp/versionhistory</code> or
 * <code>/content/versionhistory</code>)</li>
 * <li>Launches (at <code>/content/launches</code>)</li>
 * </ul>
 * Paths starting with one of these special paths are treated in a special way so code relying on the original path
 * structure still works.
 */
@ProviderType
public final class Path {

  // VERSION_HISTORY_PATH is used since AEM 6.5, AEM 6.4.3, AEM 6.3.3.2, LEGACY_VERSION_HISTORY_PATH in the versions before
  private static final String VERSION_HISTORY_PATH = "/tmp/versionhistory";
  private static final String LEGACY_VERSION_HISTORY_PATH = "/content/versionhistory";

  private static final String LAUNCHES_PATH = "/content/launches";
  private static final String EXPERIENCE_FRAGMENTS_PATH = "/content/experience-fragments";

  private static final Pattern VERSION_HISTORY_PATTERN = Pattern.compile(VERSION_HISTORY_PATH + "/[^/]+/[^/]+(/.*)?");
  private static final Pattern LEGACY_VERSION_HISTORY_PATTERN = Pattern.compile(LEGACY_VERSION_HISTORY_PATH + "/[^/]+(/.*)?");
  private static final Pattern LEGACY_VERSION_HISTORY_TENANT_PATTERN = Pattern.compile(LEGACY_VERSION_HISTORY_PATH + "/[^/]+/[^/]+(/.*)?");
  private static final Pattern LAUNCHES_PATTERN = Pattern.compile(LAUNCHES_PATH + "/\\d+/\\d+/\\d+/[^/]+(/.*)?");

  private static final Pattern EXPERIENCE_FRAGMENTS_PATTERN = Pattern.compile("^" + EXPERIENCE_FRAGMENTS_PATH + "/.*$");

  private Path() {
    // static methods only
  }

  /**
   * Get absolute parent of given path.
   * If the path is a version history or launch path the path level is adjusted accordingly.
   * This is a replacement for {@link com.day.text.Text#getAbsoluteParent(String, int)}.
   * @param path Path
   * @param parentLevel Parent level
   * @param resourceResolver Resource resolver
   * @return Absolute parent path or empty string if path is invalid
   */
  public static String getAbsoluteParent(@NotNull String path, int parentLevel, @NotNull ResourceResolver resourceResolver) {
    if (parentLevel < 0) {
      return "";
    }
    int level = parentLevel + getAbsoluteLevelOffset(path, resourceResolver);
    return Text.getAbsoluteParent(path, level);
  }

  /**
   * Get absolute parent of given path.
   * If the path is a version history or launch path the path level is adjusted accordingly.
   * This is a replacement for {@link Page#getAbsoluteParent(int)}.
   * @param page Page
   * @param parentLevel Parent level
   * @param resourceResolver Resource resolver
   * @return Absolute parent page or null if path is invalid
   */
  @SuppressWarnings("null")
  public static Page getAbsoluteParent(@NotNull Page page, int parentLevel, @NotNull ResourceResolver resourceResolver) {
    PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
    String absoluteParentPath = getAbsoluteParent(page.getPath(), parentLevel, resourceResolver);
    if (StringUtils.isEmpty(absoluteParentPath)) {
      return null;
    }
    return pageManager.getPage(absoluteParentPath);
  }

  /**
   * Gets level from parent use same logic (but reverse) as {@link #getAbsoluteParent(Page, int, ResourceResolver)}.
   * If the path is a version history or launch path the original path is returned.
   * @param path Path
   * @param resourceResolver Resource resolver
   * @return level &gt;= 0 if path is valid, -1 if path is invalid
   */
  public static int getAbsoluteLevel(@NotNull String path, @NotNull ResourceResolver resourceResolver) {
    if (StringUtils.isEmpty(path) || StringUtils.equals(path, "/")) {
      return -1;
    }
    String originalPath = getOriginalPath(path, resourceResolver);
    return StringUtils.countMatches(originalPath, "/") - 1;
  }

  /**
   * Resolve original path if the path is a version history or launch path.
   * If the path does not point to any of these locations it is returned unchanged.
   * @param path Path
   * @param resourceResolver Resource resolver
   * @return Path that is not a version history or launch path
   */
  public static String getOriginalPath(@NotNull String path, @NotNull ResourceResolver resourceResolver) {
    if (StringUtils.isEmpty(path)) {
      return null;
    }
    Matcher versionHistoryMatcher = VERSION_HISTORY_PATTERN.matcher(path);
    if (versionHistoryMatcher.matches()) {
      return "/content" + versionHistoryMatcher.group(1);
    }
    Matcher legacyVersionHistoryMatcher = LEGACY_VERSION_HISTORY_PATTERN.matcher(path);
    if (legacyVersionHistoryMatcher.matches()) {
      if (isTenant(resourceResolver)) {
        legacyVersionHistoryMatcher = LEGACY_VERSION_HISTORY_TENANT_PATTERN.matcher(path);
      }
      if (legacyVersionHistoryMatcher.matches()) {
        return "/content" + legacyVersionHistoryMatcher.group(1);
      }
    }
    Matcher launchesMatcher = LAUNCHES_PATTERN.matcher(path);
    if (launchesMatcher.matches()) {
      return launchesMatcher.group(1);
    }
    return path;
  }

  /**
   * Calculates offset for absolute level if path is a version history or launch path.
   * @param path Path
   * @param resourceResolver Resource resolver
   * @return 0 or offset if a version history or launch path.
   */
  private static int getAbsoluteLevelOffset(@NotNull String path, @NotNull ResourceResolver resourceResolver) {
    Matcher versionHistoryMatcher = VERSION_HISTORY_PATTERN.matcher(path);
    if (versionHistoryMatcher.matches()) {
      return 3;
    }
    Matcher legacyVersionHistoryMatcher = LEGACY_VERSION_HISTORY_PATTERN.matcher(path);
    if (legacyVersionHistoryMatcher.matches()) {
      if (isTenant(resourceResolver)) {
        legacyVersionHistoryMatcher = LEGACY_VERSION_HISTORY_TENANT_PATTERN.matcher(path);
        if (legacyVersionHistoryMatcher.matches()) {
          return 3;
        }
      }
      return 2;
    }
    Matcher launchesMatcher = LAUNCHES_PATTERN.matcher(path);
    if (launchesMatcher.matches()) {
      return 6;
    }
    return 0;
  }

  /**
   * Checks if a tenant is active for the current user.
   * @param resourceResolver Resource resolver
   * @return true if tenant is active
   */
  private static boolean isTenant(@NotNull ResourceResolver resourceResolver) {
    Tenant tenant = resourceResolver.adaptTo(Tenant.class);
    return tenant != null;
  }

  /**
   * @param path Content path
   * @return true if content path is inside experience fragements path.
   */
  public static boolean isExperienceFragmentPath(@NotNull String path) {
    return EXPERIENCE_FRAGMENTS_PATTERN.matcher(path).matches();
  }

}
