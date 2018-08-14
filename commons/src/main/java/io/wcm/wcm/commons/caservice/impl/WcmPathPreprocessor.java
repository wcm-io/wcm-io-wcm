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
package io.wcm.wcm.commons.caservice.impl;

import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;

import io.wcm.sling.commons.caservice.PathPreprocessor;
import io.wcm.wcm.commons.util.Path;

/**
 * Applies path rewrite operations of {@link Path#getOriginalPath(String, ResourceResolver)}
 * to path before they are matched to the patterns from Context-Aware service implementations.
 */
@Component(service = PathPreprocessor.class)
public class WcmPathPreprocessor implements PathPreprocessor {

  @SuppressWarnings("null")
  @Override
  public String apply(String path, ResourceResolver resourceResolver) {
    return Path.getOriginalPath(path, resourceResolver);
  }

}
