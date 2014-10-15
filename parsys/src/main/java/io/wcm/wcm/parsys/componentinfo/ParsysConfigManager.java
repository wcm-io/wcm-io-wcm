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

import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Collects all paragraph system configurations from different sources.
 */
@ProviderType
public interface ParsysConfigManager {

  /**
   * Get all paragraph system configurations for the given template/page component.
   * @param pageComponentPath Page component resource type
   * @param resolver Resource resolver
   * @return All configurations defined in any source
   */
  Iterable<ParsysConfig> getParsysConfigs(String pageComponentPath, ResourceResolver resolver);

  /**
   * Get all paragraph system configurations for a certain path inside a page with the given template/page component.
   * @param pageComponentPath Page component resource type
   * @param relativePath Relative path inside the page (starting with jcr:content, but without leading slash).
   * @param resolver Resource resolver
   * @return All configurations defined in any source
   */
  Iterable<ParsysConfig> getParsysConfigs(String pageComponentPath, String relativePath, ResourceResolver resolver);

}
