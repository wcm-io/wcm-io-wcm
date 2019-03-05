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
package io.wcm.wcm.commons.component;

import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;

import com.day.cq.wcm.api.components.Component;
import com.day.cq.wcm.api.components.ComponentManager;

import io.wcm.sling.commons.adapter.AdaptTo;

/**
 * Resolves properties set on component associated with the given resource.
 * Super components of the component are taken into account as well.
 */
@ProviderType
public final class ComponentPropertyResolver {

  private final Component component;

  /**
   * Content resource associated with a component (resource type).
   * @param resource Content resource
   */
  public ComponentPropertyResolver(Resource resource) {
    ComponentManager componentManager = AdaptTo.notNull(resource.getResourceResolver(), ComponentManager.class);
    this.component = componentManager.getComponentOfResource(resource);
  }

  /**
   * Get property.
   * @param name Property name
   * @param type Property type
   * @param <T> Parameter type
   * @return Property value or null if not set
   */
  public @Nullable <T> T get(@NotNull String name, @NotNull Class<T> type) {
    return getForComponent(component, name, type);
  }

  /**
   * Get property.
   * @param name Property name
   * @param defaultValue Default value
   * @param <T> Parameter type
   * @return Property value or default value if not set
   */
  public <T> T get(@NotNull String name, @NotNull T defaultValue) {
    return getForComponent(component, name, defaultValue);
  }

  private static @Nullable <T> T getForComponent(@Nullable Component component,
      @NotNull String name, @NotNull Class<T> type) {
    if (component == null) {
      return null;
    }
    @Nullable
    T result = component.getProperties().get(name, type);
    if (result != null) {
      return result;
    }
    else {
      return getForComponent(component.getSuperComponent(), name, type);
    }
  }

  private static <T> T getForComponent(@Nullable Component component,
      @NotNull String name, @NotNull T defaultValue) {
    if (component == null) {
      return defaultValue;
    }
    @SuppressWarnings("unchecked")
    @Nullable
    T result = component.getProperties().get(name, (Class<T>)defaultValue.getClass());
    if (result != null) {
      return result;
    }
    else {
      return getForComponent(component.getSuperComponent(), name, defaultValue);
    }
  }

}
