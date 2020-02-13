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

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ResourceUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.components.Component;
import com.day.cq.wcm.api.components.ComponentContext;
import com.day.cq.wcm.api.components.ComponentManager;
import com.day.cq.wcm.api.policies.ContentPolicy;
import com.day.cq.wcm.api.policies.ContentPolicyManager;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import io.wcm.sling.commons.adapter.AdaptTo;

/**
 * Tries to resolve properties with or without inheritance from pages, content policies or component definitions.
 * <p>
 * The lookup can take place in:
 * </p>
 * <ol>
 * <li>Properties of the current page (including the parent pages if inheritance is enabled)</li>
 * <li>Properties from the content policy associated with the current resource</li>
 * <li>Properties defined on the component associated with the current resource (including super components if
 * inheritance is enabled)</li>
 * </ol>
 * <p>
 * By default, only option 3 is enabled (with inheritance).
 * Please make sure to {@link #close()} instances of this class after usage.
 * </p>
 */
@ProviderType
public final class ComponentPropertyResolver implements AutoCloseable {

  private ComponentPropertyResolution componentPropertiesResolution = ComponentPropertyResolution.RESOLVE_INHERIT;
  private ComponentPropertyResolution pagePropertiesResolution = ComponentPropertyResolution.IGNORE;
  private ComponentPropertyResolution contentPolicyResolution = ComponentPropertyResolution.IGNORE;
  private final Page currentPage;
  private final Component currentComponent;
  private final Resource resource;
  private final ResourceResolverFactory resourceResolverFactory;
  private ResourceResolver componentsResourceResolver;
  private boolean initComponentsResourceResolverFailed;

  private static final String SERVICEUSER_SUBSERVICE = "component-properties";

  private static final Logger log = LoggerFactory.getLogger(ComponentPropertyResolver.class);

  /**
   * This constructor is for internal use only, please use {@link ComponentPropertyResolverFactory}.
   * @param page Content page
   * @param resourceResolverFactory Resource resolver factory
   */
  public ComponentPropertyResolver(@NotNull Page page,
      @Nullable ResourceResolverFactory resourceResolverFactory) {
    this(page.getContentResource(), resourceResolverFactory);
  }

  /**
   * This constructor is for internal use only, please use {@link ComponentPropertyResolverFactory}.
   * @param resource Content resource
   * @param resourceResolverFactory Resource resolver factory
   */
  public ComponentPropertyResolver(@NotNull Resource resource,
      @Nullable ResourceResolverFactory resourceResolverFactory) {
    this(resource, false, resourceResolverFactory);
  }

  /**
   * This constructor is for internal use only, please use {@link ComponentPropertyResolverFactory}.
   * @param resource Content resource
   * @param ensureResourceType Ensure the given resource has a resource type.
   *          If this is not the case, try to find the closest parent resource which has a resource type.
   * @param resourceResolverFactory Resource resolver factory
   */
  public ComponentPropertyResolver(@NotNull Resource resource, boolean ensureResourceType,
      @Nullable ResourceResolverFactory resourceResolverFactory) {
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
    if (hasResourceType(contextResource)) {
      ComponentManager componentManager = AdaptTo.notNull(resourceResolver, ComponentManager.class);
      this.currentComponent = componentManager.getComponentOfResource(contextResource);
    }
    else {
      this.currentComponent = null;
    }
    this.resource = contextResource;
    this.resourceResolverFactory = resourceResolverFactory;
  }

  /**
   * This constructor is for internal use only, please use {@link ComponentPropertyResolverFactory}.
   * @param wcmComponentContext WCM component context
   * @param resourceResolverFactory Resource resolver factory
   */
  public ComponentPropertyResolver(@NotNull ComponentContext wcmComponentContext,
      @Nullable ResourceResolverFactory resourceResolverFactory) {
    this.currentPage = wcmComponentContext.getPage();
    this.currentComponent = wcmComponentContext.getComponent();
    this.resource = wcmComponentContext.getResource();
    this.resourceResolverFactory = resourceResolverFactory;
  }

  /**
   * Lookup for content resource associated with the page component (resource type).
   * @param page Content page
   * @deprecated Please use {@link ComponentPropertyResolverFactory}.
   */
  @Deprecated
  public ComponentPropertyResolver(@NotNull Page page) {
    this(page, null);
  }

  /**
   * Lookup for content resource associated with a component (resource type).
   * @param resource Content resource
   * @deprecated Please use {@link ComponentPropertyResolverFactory}.
   */
  @Deprecated
  public ComponentPropertyResolver(@NotNull Resource resource) {
    this(resource, null);
  }

  /**
   * Lookup for content resource associated with a component (resource type).
   * @param resource Content resource
   * @param ensureResourceType Ensure the given resource has a resource type.
   *          If this is not the case, try to find the closest parent resource which has a resource type.
   * @deprecated Please use {@link ComponentPropertyResolverFactory}.
   */
  @Deprecated
  public ComponentPropertyResolver(@NotNull Resource resource, boolean ensureResourceType) {
    this(resource, ensureResourceType, null);
  }

  /**
   * Lookup with content resource associated with a component (resource type).
   * @param wcmComponentContext WCM component context
   * @deprecated Please use {@link ComponentPropertyResolverFactory}.
   */
  @Deprecated
  public ComponentPropertyResolver(@NotNull ComponentContext wcmComponentContext) {
    this(wcmComponentContext, null);
  }

  private static boolean hasResourceType(@NotNull Resource resource) {
    return StringUtils.isNotEmpty(resource.getResourceType());
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
    T result;
    if (StringUtils.contains(name, "/")) {
      // if a property in child resource is addressed get property value via local resource
      // because the map behind the getProperties() method does not support child resource access
      String childResourcePath = StringUtils.substringBeforeLast(name, "/");
      String localPropertyName = StringUtils.substringAfterLast(name, "/");
      Resource childResource = getLocalComponentResource(component, childResourcePath);
      result = ResourceUtil.getValueMap(childResource).get(localPropertyName, type);
    }
    else {
      result = component.getProperties().get(name, type);
    }
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
    Collection<Resource> result = getResources(getLocalComponentResource(component, name));
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
   * Get content policy via policy manager.
   * @param resource Content resource
   * @return Policy or null
   */
  private static @Nullable ContentPolicy getPolicy(@NotNull Resource resource) {
    ContentPolicyManager policyManager = AdaptTo.notNull(resource.getResourceResolver(), ContentPolicyManager.class);
    return policyManager.getPolicy(resource);
  }

  /**
   * Get local child resource for component, with a special handling for publish environments where
   * the local child resources for components below /apps are not accessible for everyone.
   * @param component Component
   * @param childResourcePath Child resource path
   * @return Resource or null
   */
  private @Nullable Resource getLocalComponentResource(@NotNull Component component,
      @NotNull String childResourcePath) {
    if (componentsResourceResolver == null
        && resourceResolverFactory != null
        && !initComponentsResourceResolverFailed) {
      try {
        componentsResourceResolver = resourceResolverFactory.getServiceResourceResolver(
            ImmutableMap.of(ResourceResolverFactory.SUBSERVICE, SERVICEUSER_SUBSERVICE));
      }
      catch (LoginException ex) {
        initComponentsResourceResolverFailed = true;
        if (log.isDebugEnabled()) {
          log.debug("Unable to get resource resolver for accessing local component resource, "
              + "please make sure to grant access to system user 'sling-scripting' for "
              + "bundle 'io.wcm.wcm.commons', subservice '{}'.", SERVICEUSER_SUBSERVICE, ex);
        }
      }
    }
    if (componentsResourceResolver != null) {
      String resourcePath = component.getPath() + "/" + childResourcePath;
      return componentsResourceResolver.getResource(resourcePath);
    }
    // fallback implementation for previous behavior - this will usually not work in publish instances
    return component.getLocalResource(childResourcePath);
  }

  @Override
  public void close() {
    if (componentsResourceResolver != null) {
      componentsResourceResolver.close();
    }
  }

}
