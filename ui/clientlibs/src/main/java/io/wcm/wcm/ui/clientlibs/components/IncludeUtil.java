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
package io.wcm.wcm.ui.clientlibs.components;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.sling.api.resource.ResourceResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.adobe.granite.ui.clientlibs.ClientLibrary;
import com.adobe.granite.ui.clientlibs.HtmlLibraryManager;
import com.adobe.granite.ui.clientlibs.LibraryType;

/**
 * Helper methods for building client library includes.
 */
final class IncludeUtil {

  private IncludeUtil() {
    // static methods only
  }

  /**
   * @return Array of clientlib category names as specified in HTL script
   */
  public static @Nullable String[] toCategoryArray(Object categories) {
    String[] categoryArray = null;
    if (categories instanceof String) {
      categoryArray = new String[] { (String)categories };
    }
    else if (categories != null && categories.getClass().isArray()) {
      categoryArray = new String[Array.getLength(categories)];
      for (int i = 0; i < Array.getLength(categories); i++) {
        Object value = Array.get(categories, i);
        if (value == null) {
          value = "";
        }
        categoryArray[i] = value.toString();
      }
    }
    return categoryArray;
  }

  /**
   * Get all (external) library URLs for the src attributes of the script tag(s).
   * @param htmlLibraryManager HTML library manager
   * @param resourceResolver Resource resolver from current request
   * @param categoryArray Clientlib categories
   * @param libraryType Library type
   * @return List of Client Library URLs
   */
  @SuppressWarnings("null")
  public static @NotNull List<String> getLibraryUrls(@NotNull HtmlLibraryManager htmlLibraryManager,
      @NotNull ResourceResolver resourceResolver, @NotNull String[] categoryArray, @NotNull LibraryType libraryType) {
    return htmlLibraryManager.getLibraries(categoryArray, libraryType, false, true).stream()
        .map(library -> getLibraryUrl(resourceResolver, library, libraryType, htmlLibraryManager.isMinifyEnabled()))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  /**
   * Get (external) library URL respecting proxy mode and access rights to the client library.
   * @param resourceResolver Resource resolver from current request
   * @param library Library
   * @param libraryType Library type
   * @param minify Whether to minify the client library
   * @return Library URL or null
   */
  private static @Nullable String getLibraryUrl(@NotNull ResourceResolver resourceResolver,
      @NotNull ClientLibrary library, @NotNull LibraryType libraryType, boolean minify) {
    String path = library.getIncludePath(libraryType, minify);
    if (library.allowProxy() && (path.startsWith("/libs/") || path.startsWith("/apps/"))) {
      path = "/etc.clientlibs" + path.substring(5);
    }
    else if (resourceResolver.getResource(library.getPath()) == null) {
      // current render resourcer resolver has no access to the client library - ignore it
      path = null;
    }
    return path;
  }

}
