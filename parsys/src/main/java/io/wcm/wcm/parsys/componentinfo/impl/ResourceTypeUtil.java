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

import org.apache.commons.lang3.StringUtils;

/**
 * Helper methods for resource type path handling.
 * TODO: add unit tests
 */
public final class ResourceTypeUtil {

  private ResourceTypeUtil() {
    // utility methods only
  }

  /**
   * /apps prefix for resource types
   */
  public static final String APPS_PREFIX = "/apps/";

  /**
   * /libs prefix for resource types
   */
  public static final String LIBS_PREFIX = "/libs/";

  /**
   * Converts the resource type to an absolute path. If it does not start with "/", "/apps/" is prepended.
   * Otherwise it is returned unchanged.
   * @param pResourceType Resource type
   * @return Normalized resource type
   */
  public static String makeAbsolute(String pResourceType) {
    if (StringUtils.isEmpty(pResourceType)) {
      return pResourceType;
    }
    if (!StringUtils.startsWith(pResourceType, "/")) {
      return APPS_PREFIX + pResourceType;
    }
    else {
      return pResourceType;
    }
  }

  /**
   * Removes /apps/ or /libs/ prefix from resource type if present.
   * @param pResourceType Resource type
   * @return Resource type if present
   */
  public static String removePrefix(String pResourceType) {
    if (StringUtils.isEmpty(pResourceType)) {
      return pResourceType;
    }
    if (StringUtils.startsWith(pResourceType, APPS_PREFIX)) {
      return pResourceType.substring(APPS_PREFIX.length());
    }
    if (StringUtils.startsWith(pResourceType, LIBS_PREFIX)) {
      return pResourceType.substring(LIBS_PREFIX.length());
    }
    return pResourceType;
  }

}
