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

import static org.apache.sling.api.resource.ResourceResolver.PROPERTY_RESOURCE_TYPE;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

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
import com.day.cq.wcm.api.policies.ContentPolicy;
import com.day.cq.wcm.api.policies.ContentPolicyManager;
import com.google.common.collect.ImmutableList;

import io.wcm.sling.commons.adapter.AdaptTo;

/**
 * Tries to resolve properties with or without inheritance from pages, content policies or component definitions.
 * <p>
 * The lookup takes place in:
 * </p>
 * <ol>
 * <li>Properties of the current page (including the parent pages if inheritance is enabled)</li>
 * <li>Properties from the content policy associated with the current resource</li>
 * <li>Properties defined on the component associated with the current resource (including super components if
 * inheritance is enabled)</li>
 * </ol>
 * <p>
 * By default, only option 3 is enabled (with inheritance).
 * </p>
 */
@ProviderType
public final class ComponentPropertyResolver {

  private ComponentPropertyResolution componentPropertiesResolution = ComponentPropertyResolution.RESOLVE_INHERIT;
  private ComponentPropertyResolution pagePropertiesResolution = ComponentPropertyResolution.IGNORE;
  private ComponentPropertyResolution contentPolicyResolution = ComponentPropertyResolution.IGNORE;
  private final Page currentPage;
  private final Component currentComponent;
  private final Resource resource;

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
    this(resource, false);
  }

  /**
   * Content resource associated with a component (resource type).
   * @param resource Content resource
   * @param ensureResourceType Ensure the given resource has a resource type.
   *          If this is not the case, try to find the closest parent resource which has a resource type.
   */
  public ComponentPropertyResolver(@NotNull Resource resource, boolean ensureResourceType) {
    Resource contextResource = null;
    if (ensureResourceType) {
      // find closest parent resource that has a resource type (and not nt:unstructured)
      contextResource = getResourceWithResourceType(resource);
    }
    if (contextResource == null) {
      contextResource = resource;
    }

    ResourceResolver resourceResolver = contextResource.getResourceResolver();
    PageManager pageManager = AdaptTo.notNull(resourceResolver, PageManager.class);
    this.currentPage = pageManager.getContainingPage(contextResource);
    ComponentManager componentManager = AdaptTo.notNull(resourceResolver, ComponentManager.class);
    this.currentComponent = componentManager.getComponentOfResource(contextResource);
    this.resource = contextResource;
  }

  private static @Nullable Resource getResourceWithResourceType(@Nullable Resource resource) {
    if (resource == null) {
      return null;
    }
    String resourceType = resource.getValueMap().get(PROPERTY_RESOURCE_TYPE, String.class);
    if (resourceType != null) {
      return resource;
    }
    return getResourceWithResourceType(resource.getParent());
  }

  /**
   * Content resource associated with a component (resource type).
   * @param wcmComponentContext WCM component context
   */
  public ComponentPropertyResolver(@NotNull ComponentContext wcmComponentContext) {
    this.currentPage = wcmComponentContext.getPage();
    this.currentComponent = wcmComponentContext.getComponent();
    this.resource = wcmComponentContext.getResource();
  }

  /**
   * Configure if properties should be resolved in component properties, and with or without inheritance.
   * Default mode is {@link ComponentPropertyResolution#RESOLVE_INHERIT}.
   * @param resolution Resolution mode
   * @return this
   */
  public ComponentPropertyResolver componentPropertiesResolution(@NotNull ComponentPropertyResolution resolution) {
    this.componentPropertiesResolution = resolution;
    return this;
  }

  /**
   * Configure if properties should be resolved in content page properties, and with or without inheritance.
   * Default mode is {@link ComponentPropertyResolution#IGNORE}.
   * @param resolution Resolution mode
   * @return this
   */
  public ComponentPropertyResolver pagePropertiesResolution(@NotNull ComponentPropertyResolution resolution) {
    this.pagePropertiesResolution = resolution;
    return this;
  }

  /**
   * Configure if properties should be resolved from content policies mapped for the given resource.
   * No explicit inheritance mode is supported, so {@link ComponentPropertyResolution#RESOLVE_INHERIT}
   * has the same effect as {@link ComponentPropertyResolution#RESOLVE} in this case.
   * Default mode is {@link ComponentPropertyResolution#IGNORE}.
   * @param resolution Resolution mode
   * @return this
   */
  public ComponentPropertyResolver contentPolicyResolution(@NotNull ComponentPropertyResolution resolution) {
    this.contentPolicyResolution = resolution;
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
      value = getContentPolicyProperty(name, type);
    }
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

  private @Nullable <T> T getContentPolicyProperty(@NotNull String name, @NotNull Class<T> type) {
    if (contentPolicyResolution == ComponentPropertyResolution.IGNORE || resource == null) {
      return null;
    }
    ContentPolicy policy = getPolicy(resource);
    if (policy != null) {
      return policy.getProperties().get(name, type);
    }
    return null;
  }

  /**
   * Get list of child resources.
   * @param name Child node name
   * @return List of child resources or null if not set.
   */
  public @Nullable Collection<Resource> getResources(@NotNull String name) {
    Collection<Resource> list = getPageResources(currentPage, name);
    if (list == null) {
      list = getContentPolicyResources(name);
    }
    if (list == null) {
      list = getComponentResources(currentComponent, name);
    }
    return list;
  }

  private @Nullable Collection<Resource> getComponentResources(@Nullable Component component, @NotNull String name) {
    if (componentPropertiesResolution == ComponentPropertyResolution.IGNORE || component == null) {
      return null;
    }
    Collection<Resource> result = getResources(component.getLocalResource(name));
    if (result == null && componentPropertiesResolution == ComponentPropertyResolution.RESOLVE_INHERIT) {
      result = getComponentResources(component.getSuperComponent(), name);
    }
    return result;
  }

  private @Nullable Collection<Resource> getPageResources(@Nullable Page page, @NotNull String name) {
    if (pagePropertiesResolution == ComponentPropertyResolution.IGNORE || page == null) {
      return null;
    }
    Collection<Resource> result = getResources(page.getContentResource(name));
    if (result == null && pagePropertiesResolution == ComponentPropertyResolution.RESOLVE_INHERIT) {
      result = getPageResources(page.getParent(), name);
    }
    return result;
  }

  private @Nullable Collection<Resource> getContentPolicyResources(@NotNull String name) {
    if (contentPolicyResolution == ComponentPropertyResolution.IGNORE || resource == null) {
      return null;
    }
    ContentPolicy policy = getPolicy(resource);
    if (policy != null) {
      Resource policyResource = policy.adaptTo(Resource.class);
      if (policyResource != null) {
        return getResources(policyResource.getChild(name));
      }
    }
    return null;
  }

  private @Nullable Collection<Resource> getResources(@Nullable Resource parent) {
    if (parent == null) {
      return null;
    }
    Collection<Resource> children = ImmutableList.copyOf(parent.getChildren());
    if (children.isEmpty()) {
      return null;
    }
    return children;
  }

  /**
   * Get content policy via policy manager. Please not that the signature to get policy from a resource
   * is only available in AEM 6.3+. We keep compiling against AEM 6.2 atm, but call the method via reflection
   * and accept that resolving the content policy only work with AEM 6.3.
   * @param resource Content resource
   * @return Policy or null
   */
  private static @Nullable ContentPolicy getPolicy(@NotNull Resource resource) {
    try {
      // TODO: remove this ugly hack once updated to AEM 6.3 API or above
      ContentPolicyManager policyManager = AdaptTo.notNull(resource.getResourceResolver(), ContentPolicyManager.class);
      Method getPolicyByResource = policyManager.getClass().getMethod("getPolicy", Resource.class);
      getPolicyByResource.setAccessible(true);
      return (ContentPolicy)getPolicyByResource.invoke(policyManager, resource);
    }
    catch (NoSuchMethodException ex) {
      // assume AEM 6.2 - content policy resolution not supported
      return null;
    }
    catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
      throw new RuntimeException("Unable to get content policy.", ex);
    }
  }

}
