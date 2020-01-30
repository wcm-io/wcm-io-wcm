/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2019 wcm.io
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
package io.wcm.wcm.ui.granite.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.jetbrains.annotations.NotNull;
import org.osgi.annotation.versioning.ProviderType;

import com.adobe.granite.ui.components.ComponentHelper;
import com.adobe.granite.ui.components.Config;
import com.adobe.granite.ui.components.ExpressionHelper;
import com.day.text.Text;

/**
 * Helper class for path-based GraniteUI components to resolve the root path.
 * <p>
 * Resolution order for root path detection:
 * </p>
 * <ul>
 * <li>Reads configured root path from <code>rootPath</code> property</li>
 * <li>Calls the provided root path detector implementation to detect root path from context</li>
 * <li>Reads fallback root path from <code>fallbackRootPath</code> property</li>
 * <li>Uses fallback root path provided for this instance</li>
 * <li>Fallback to "/"</li>
 * </ul>
 * <p>
 * Additionally the root path is modified:
 * </p>
 * <ul>
 * <li>If an <code>appendPath</code> property is configured it is appended to the detected root path</li>
 * <li>Than it is checked if the root path is valid - if not the next-valid parent path is returned</li>
 * </ul>
 */
@ProviderType
public final class RootPathResolver {

  static final String PN_ROOT_PATH = "rootPath";
  static final String PN_APPEND_PATH = "appendPath";
  static final String PN_FALLBACK_PATH = "fallbackRootPath";
  static final String DEFAULT_FALLBACK_ROOT_PATH = "/";

  private final ComponentHelper cmp;
  private final Config cfg;
  private final ExpressionHelper ex;
  private final SlingHttpServletRequest request;
  private final ResourceResolver resourceResolver;

  private RootPathDetector rootPathDetector;
  private String fallbackRootPath = DEFAULT_FALLBACK_ROOT_PATH;

  /**
   * @param cmp Component helper
   * @param request Request
   */
  public RootPathResolver(@NotNull ComponentHelper cmp, @NotNull SlingHttpServletRequest request) {
    this.cmp = cmp;
    this.cfg = cmp.getConfig();
    this.ex = cmp.getExpressionHelper();
    this.request = request;
    this.resourceResolver = request.getResourceResolver();
  }

  /**
   * @param rootPathDetector For detecting root path from context
   */
  public void setRootPathDetector(@NotNull RootPathDetector rootPathDetector) {
    this.rootPathDetector = rootPathDetector;
  }

  /**
   * @param fallbackRootPath Fallback root path that is used if none is configured
   */
  public void setFallbackRootPath(@NotNull String fallbackRootPath) {
    this.fallbackRootPath = fallbackRootPath;
  }

  /**
   * Get the resolved and validated root path.
   * @return Root path.
   */
  public @NotNull String get() {
    // get configured or detected or fallback root path
    String rootPath = getRootPath();

    // append path if configured
    rootPath = appendPath(rootPath);

    // resolve to existing path
    return getExistingPath(rootPath);
  }

  /**
   * Get map of override properties for super component based on the wcm.io Granite UI Extensions PathField.
   * @return Path properties
   */
  public Map<String, Object> getOverrideProperties() {
    Map<String, Object> props = new HashMap<>();
    props.put(PN_ROOT_PATH, get());
    props.put(PN_APPEND_PATH, "");
    props.put(PN_FALLBACK_PATH, "");
    return props;
  }

  /**
   * @return Configured or detected root path or fallback path
   */
  private @NotNull String getRootPath() {

    // check for configured root path
    String rootPath = ex.getString(cfg.get(PN_ROOT_PATH, String.class));
    if (StringUtils.isNotBlank(rootPath)) {
      return rootPath;
    }

    // call root path detector
    if (rootPathDetector != null) {
      rootPath = rootPathDetector.detectRootPath(cmp, request);
      if (rootPath != null && StringUtils.isNotBlank(rootPath)) {
        return rootPath;
      }
    }

    // check for configured fallback path
    rootPath = ex.getString(cfg.get(PN_FALLBACK_PATH, String.class));
    if (StringUtils.isNotBlank(rootPath)) {
      return rootPath;
    }

    // fallback to default fallback path
    return fallbackRootPath;
  }

  /**
   * Appends the "appendPath" if configured.
   * @param rootPath Root path
   * @return Path with appendix
   */
  private @NotNull String appendPath(@NotNull String rootPath) {
    String appendPath = ex.getString(cfg.get(PN_APPEND_PATH, String.class));
    if (StringUtils.isBlank(appendPath)) {
      return rootPath;
    }
    StringBuilder combinedPath = new StringBuilder(rootPath);
    if (!StringUtils.startsWith(appendPath, "/")) {
      combinedPath.append("/");
    }
    combinedPath.append(appendPath);
    return combinedPath.toString();
  }

  /**
   * Make sure the root path exists. If it does not exist go up to parent hierarchy until it returns an
   * existing resource path.
   */
  @NotNull
  String getExistingPath(@NotNull String rootPath) {
    if (resourceResolver.getResource(rootPath) == null) {
      String parentPath = Text.getRelativeParent(rootPath, 1);
      if (StringUtils.isBlank(parentPath)) {
        return DEFAULT_FALLBACK_ROOT_PATH;
      }
      return getExistingPath(parentPath);
    }
    else {
      return rootPath;
    }
  }

}
