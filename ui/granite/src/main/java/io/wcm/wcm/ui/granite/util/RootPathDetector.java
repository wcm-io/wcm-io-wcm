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

import org.apache.sling.api.SlingHttpServletRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ConsumerType;

import com.adobe.granite.ui.components.ComponentHelper;

/**
 * Interface that can be implemented to provide custom logic to detect a root path
 * from current context if there was no explicit root path configured for the GraniteUI component.
 */
@ConsumerType
public interface RootPathDetector {

  /**
   * Detect root path from context of GraniteUI component.
   * @param cmp Granite UI Component Helper
   * @param request Request
   * @return Root path or null
   */
  @Nullable
  String detectRootPath(@NotNull ComponentHelper cmp, @NotNull SlingHttpServletRequest request);

}
