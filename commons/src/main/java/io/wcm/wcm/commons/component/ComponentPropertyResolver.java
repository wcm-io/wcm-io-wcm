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
import org.apache.sling.api.resource.ResourceResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.components.Component;
import com.day.cq.wcm.api.components.ComponentContext;
import com.day.cq.wcm.api.components.ComponentManager;

import io.wcm.sling.commons.adapter.AdaptTo;

/**
 * Resolves properties set on component associated with the given resource or pages the current resource is contained
 * in, with or without inheritance in both cases.
 * By default, only component properties are resolved with inheritance.
 */
@ProviderType
public final class ComponentPropertyResolver {

  private ComponentPropertyResolution componentPropertiesResolution = ComponentPropertyResolution.RESOLVE_INHERIT;
  private ComponentPropertyResolution pagePropertiesResolution = ComponentPropertyResolution.IGNORE;
  private final Page currentPage;
  private final Component currentComponent;

  /**
   * Content resource associated with a component (resource type).
   * @param page Content page
   */
  public ComponentPropertyResolver(@NotNull Page page) {
    this(page.getContentResource());
  }

  /**
   * Content resource associated with a component (resource type).
   * @param resource Content resource
   */
  public ComponentPropertyResolver(@NotNull Resource resource) {
    ResourceResolver resourceResolver = resource.getResourceResolver();
    PageManager pageManager = AdaptTo.notNull(resourceResolver, PageManager.class);
    this.currentPage = pageManager.getContainingPage(resource);
    ComponentManager componentManager = AdaptTo.notNull(resourceResolver, ComponentManager.class);
    this.currentComponent = componentManager.getComponentOfResource(resource);
  }

  /**
   * Content resource associated with a component (resource type).
   * @param wcmComponentContext WCM component context
   */
  public ComponentPropertyResolver(@NotNull ComponentContext wcmComponentContext) {
    this.currentPage = wcmComponentContext.getPage();
    this.currentComponent = wcmComponentContext.getComponent();
  }

  /**
   * Configure if properties should be resolved in component properties, and with or without inheritance.
   * Default mode is {@link ComponentPropertyResolution#RESOLVE_INHERIT}.
   * @param resolution Resolution mode
   * @return this
   */
  public ComponentPropertyResolver componentPropertiesResolution(ComponentPropertyResolution resolution) {
    this.componentPropertiesResolution = resolution;
    return this;
  }

  /**
   * Configure if properties should be resolved in content page properties, and with or without inheritance.
   * Default mode is {@link ComponentPropertyResolution#IGNORE}.
   * @param resolution Resolution mode
   * @return this
   */
  public ComponentPropertyResolver pagePropertiesResolution(ComponentPropertyResolution resolution) {
    this.pagePropertiesResolution = resolution;
    return this;
  }

  /**
   * Get property.
   * @param name Property name
   * @param type Property type
   * @param <T> Parameter type
   * @return Property value or null if not set
   */
  public @Nullable <T> T get(@NotNull String name, @NotNull Class<T> type) {
    @Nullable
    T value = getPageProperty(currentPage, name, type);
    if (value == null) {
      value = getComponentProperty(currentComponent, name, type);
    }
    return value;
  }

  /**
   * Get property.
   * @param name Property name
   * @param defaultValue Default value
   * @param <T> Parameter type
   * @return Property value or default value if not set
   */
  public @NotNull <T> T get(@NotNull String name, @NotNull T defaultValue) {
    @Nullable
    @SuppressWarnings("unchecked")
    T value = get(name, (Class<T>)defaultValue.getClass());
    if (value != null) {
      return value;
    }
    else {
      return defaultValue;
    }
  }

  private @Nullable <T> T getComponentProperty(@Nullable Component component,
      @NotNull String name, @NotNull Class<T> type) {
    if (componentPropertiesResolution == ComponentPropertyResolution.IGNORE || component == null) {
      return null;
    }
    @Nullable
    T result = component.getProperties().get(name, type);
    if (result == null && componentPropertiesResolution == ComponentPropertyResolution.RESOLVE_INHERIT) {
      result = getComponentProperty(component.getSuperComponent(), name, type);
    }
    return result;
  }

  private @Nullable <T> T getPageProperty(@Nullable Page page,
      @NotNull String name, @NotNull Class<T> type) {
    if (pagePropertiesResolution == ComponentPropertyResolution.IGNORE || page == null) {
      return null;
    }
    @Nullable
    T result = page.getProperties().get(name, type);
    if (result == null && pagePropertiesResolution == ComponentPropertyResolution.RESOLVE_INHERIT) {
      result = getPageProperty(page.getParent(), name, type);
    }
    return result;
  }

}
