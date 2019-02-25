/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2015 wcm.io
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
package io.wcm.wcm.parsys;

/**
 * Names used for the parsys.
 * <p>
 * Conventions:
 * </p>
 * <ul>
 * <li>NT_ prefix stands for "node type"</li>
 * <li>NN_ prefix stands for "node name"</li>
 * <li>PN_ prefix stands for "property name"</li>
 * <li>RP_ prefix stands for "request property"</li>
 * <li>RA_ prefix stands for "request attribute"</li>
 * </ul>
 */
public final class ParsysNameConstants {

  /**
   * Defines the resource-based allowed components.
   * To be used as name of a node in the parsys component defintion.
   */
  public static final String NN_PARSYS_CONFIG = "wcmio:parsysConfig";

  /**
   * Generates CSS classes by default: "section" on each paragraph/new area and "clear:both" on a new area.
   * This is a boolean property and defaults to true if not set.
   * To be used as property in the parsys component definition.
   */
  public static final String PN_PARSYS_GENERATE_DEAFULT_CSS = "wcmio:parsysGenerateDefaultCss";

  /**
   * Defines additional custom CSS classes to be set on each paragraph.
   * To be used as property in the parsys component definition.
   */
  public static final String PN_PARSYS_PARAGRAPH_CSS = "wcmio:parsysParagraphCss";

  /**
   * Defines additional custom CSS classes to be set on the new area.
   * To be used as property in the parsys component definition.
   */
  public static final String PN_PARSYS_NEWAREA_CSS = "wcmio:parsysNewAreaCss";

  /**
   * Sets the element name to be used for the element wrapping each pagraph component.
   * Defaults to "div" if not set.
   * To be used as property in the parsys component definition.
   */
  public static final String PN_PARSYS_PARAGRAPH_ELEMENT = "wcmio:parsysParagraphElement";

  /**
   * Can be set to a list of WCM modes. When one of the given WCM modes is active, the decoration tag
   * for the paragraph items is not rendered.
   * To be used as property in the parsys component definition.
   */
  public static final String PN_PARSYS_PARAGRAPH_NODECORATION_WCMMODE = "wcmio:parsysParagraphNoDecorationWcmMode";

  /**
   * Sets the element name to be used for the element wrapping the whole paragraph system.
   * If not set no wrapping element is generated for the paragraph system.
   * To be used as property in the parsys component definition.
   */
  public static final String PN_PARSYS_WRAPPER_ELEMENT = "wcmio:parsysWrapperElement";

  /**
   * Defines custom CSS classes to be set on the wrapper element.
   * To be used as property in the parsys component definition.
   */
  public static final String PN_PARSYS_WRAPPER_CSS = "wcmio:parsysWrapperCss";

  private ParsysNameConstants() {
    // constants only
  }

}
