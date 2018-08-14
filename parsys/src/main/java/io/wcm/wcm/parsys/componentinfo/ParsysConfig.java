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
package io.wcm.wcm.parsys.componentinfo;

import java.util.Set;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ConsumerType;

/**
 * Configuration of allowed components for a path inside a page of a specific template/page component.
 */
@ConsumerType
public interface ParsysConfig {

  /**
   * Resource type of the page component for which this parsys configuration is defined.
   * @return resource type of page component
   */
  @NotNull
  String getPageComponentPath();

  /**
   * Path pattern for parsys.
   * The pattern should match paths relative to the jcr:content node of a page,
   * including this node but without leading slash.<br>
   * Example: {@code ^jcr:content/(?!megaFlyout).*$}
   * @return parsys path pattern
   */
  @Nullable
  Pattern getPathPattern();

  /**
   * @return parent ancestor level (can be 1 or 2)
   */
  int getParentAncestorLevel();

  /**
   * @return resource types of allowed parent components
   */
  @NotNull
  Set<String> getAllowedParents();

  /**
   * @return resource types of allowed child components
   */
  @NotNull
  Set<String> getAllowedChildren();

  /**
   * @return resource types of denied child components
   */
  @NotNull
  Set<String> getDeniedChildren();

  /**
   * @return if true, parsys configurations from super types are inherited and merged.
   */
  default boolean isInherit() {
    return true;
  }

}
