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

import java.nio.charset.StandardCharsets;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Frequently used content types/mime types.
 * All mime types that that require encoding definition include UTF-8 as charset.
 */
@ProviderType
public final class ContentType {

  private ContentType() {
    // constants only
  }

  /**
   * HTML with UTF-8
   */
  public static final String HTML = "text/html;charset=" + StandardCharsets.UTF_8.name();

  /**
   * JSON with UTF-8
   */
  public static final String JSON = "application/json;charset=" + StandardCharsets.UTF_8.name();

  /**
   * Plain text with UTF-8
   */
  public static final String PLAINTEXT = "text/plain;charset=" + StandardCharsets.UTF_8.name();

  /**
   * CSS with UTF-8
   */
  public static final String CSS = "text/css;charset=" + StandardCharsets.UTF_8.name();

  /**
   * JavaScript with UTF-8
   */
  public static final String JAVASCRIPT = "text/javascript;charset=" + StandardCharsets.UTF_8.name();

  /**
   * XML (no charset, charset is defined within XML markup)
   */
  public static final String XML = "application/xml";

  /**
   * XHTML (no charset, charset is defined within XML markup)
   */
  public static final String XHTML = "application/xhtml+xml";

  /**
   * ZIP
   */
  public static final String ZIP = "application/zip";

  /**
   * Default binary content type
   */
  public static final String OCTET_STREAM = "application/octet-stream";

  /**
   * MIME type used for items that should initiate a "save-as" dialog in the browser.
   * (This is a non-standard MIME type by design to make sure the browser does not do anything with it).
   */
  public static final String DOWNLOAD = "application/x-download";

  /**
   * GIF image
   */
  public static final String GIF = "image/gif";

  /**
   * JPEG image
   */
  public static final String JPEG = "image/jpeg";

  /**
   * PNG image
   */
  public static final String PNG = "image/png";

  /**
   * Flash file
   */
  public static final String SWF = "application/x-shockwave-flash";

  /**
   * CSV (no charset included in mime type because charset can differ from platform to platform)
   */
  public static final String CSV = "text/csv";

  /**
   * PDF
   */
  public static final String PDF = "application/pdf";

  /**
   * SVG
   */
  public static final String SVG = "image/svg+xml";

  /**
   * TIFF
   */
  public static final String TIFF = "image/tiff";

  /**
   * WebP
   */
  public static final String WEBP = "image/webp";

}
