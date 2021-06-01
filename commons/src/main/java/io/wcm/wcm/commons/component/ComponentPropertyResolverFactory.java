/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2020 wcm.io
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
package io.wcm.wcm.commons.component;

import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.osgi.annotation.versioning.ProviderType;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.components.ComponentContext;

/**
 * OSGi service for creating {@link ComponentPropertyResolver} instances.
 * Please make sure to close the instance obtained by this factory after usage.
 */
@ProviderType
public interface ComponentPropertyResolverFactory {

  /**
   * Lookup for content resource associated with the page component (resource type).
   * @param page Content page
   * @return Component property resolver
   */
  @NotNull
  ComponentPropertyResolver get(@NotNull Page page);

  /**
   * Lookup for content resource associated with a component (resource type).
   * @param resource Content resource
   * @return Component property resolver
   */
  @NotNull
  ComponentPropertyResolver get(@NotNull Resource resource);

  /**
   * Lookup for content resource associated with a component (resource type).
   * @param resource Content resource
   * @param ensureResourceType Ensure the given resource has a resource type.
   *          If this is not the case, try to find the closest parent resource which has a resource type.
   * @return Component property resolver
   */
  @NotNull
  ComponentPropertyResolver get(@NotNull Resource resource, boolean ensureResourceType);

  /**
   * Lookup with content resource associated with a component (resource type).
   * @param wcmComponentContext WCM component context
   * @return Component property resolver
   */
  @NotNull
  ComponentPropertyResolver get(@NotNull ComponentContext wcmComponentContext);

}
