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
package io.wcm.wcm.commons.contenttype;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;

import com.google.common.collect.ImmutableSet;

/**
 * Frequently used file extensions.
 */
@ProviderType
@SuppressWarnings("null")
public final class FileExtension {

  private FileExtension() {
    // constants only
  }

  /**
   * HTML
   */
  public static final @NotNull String HTML = "html";

  /**
   * HTML, configured as non-caching
   */
  public static final @NotNull String HTML_UNCACHED = "htx";

  /**
   * JSON
   */
  public static final @NotNull String JSON = "json";

  /**
   * Plain text
   */
  public static final @NotNull String PLAINTEXT = "txt";

  /**
   * CSS
   */
  public static final @NotNull String CSS = "css";

  /**
   * JavaScript
   */
  public static final @NotNull String JAVASCRIPT = "js";

  /**
   * XML
   */
  public static final @NotNull String XML = "xml";

  /**
   * XHTML
   */
  public static final @NotNull String XHTML = "html";

  /**
   * ZIP
   */
  public static final @NotNull String ZIP = "zip";

  /**
   * GIF image
   */
  public static final @NotNull String GIF = "gif";

  /**
   * JPEG image
   */
  public static final @NotNull String JPEG = "jpg";

  /**
   * PNG image
   */
  public static final @NotNull String PNG = "png";

  /**
   * Flash file
   */
  public static final @NotNull String SWF = "swf";

  /**
   * CSV
   */
  public static final @NotNull String CSV = "csv";


  /** all file extensions that will be displayed by an image tag */
  private static final @NotNull Set<String> IMAGE_FILE_EXTENSIONS = ImmutableSet.of(
      GIF,
      JPEG,
      PNG,
      "jpeg" // check for this alternative JEPG extension as well
      );

  /** all file extensions that will be displayed as flash */
  private static final @NotNull Set<String> FLASH_FILE_EXTENSIONS = ImmutableSet.of(
      SWF
      );

  /**
   * Check if the given file extension is a standard image format supported by web browsers and AEM Layer
   * implementations.
   * @param fileExtension File extension
   * @return true if image
   */
  public static boolean isImage(@Nullable String fileExtension) {
    if (StringUtils.isEmpty(fileExtension)) {
      return false;
    }
    return IMAGE_FILE_EXTENSIONS.contains(fileExtension.toLowerCase());
  }

  /**
   * @return Image file extensions for standard image formats supported by web browsers and AEM Layer implementations.
   */
  public static @NotNull Set<String> getImageFileExtensions() {
    return IMAGE_FILE_EXTENSIONS;
  }

  /**
   * Check if the given file extension is an flash.
   * @param fileExtension File extension
   * @return true if flash
   */
  public static boolean isFlash(@Nullable String fileExtension) {
    if (StringUtils.isEmpty(fileExtension)) {
      return false;
    }
    return FLASH_FILE_EXTENSIONS.contains(fileExtension.toLowerCase());
  }

  /**
   * @return Flash file extensions
   */
  public static @NotNull Set<String> getFlashFileExtensions() {
    return FLASH_FILE_EXTENSIONS;
  }

}
