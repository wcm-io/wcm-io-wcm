/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 - 2015 wcm.io
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
package io.wcm.wcm.ui.granite.resource;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.SyntheticResource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;

import com.day.cq.commons.jcr.JcrConstants;
import com.google.common.collect.ImmutableList;

/**
 * Extended version of {@link SyntheticResource} that allows to pass an own value map and optional child resources.
 * Please note: Accessing child resources does only work when accessing {@link Resource#listChildren()}, and
 * not when calling the same method on resourceResolver. This breaks the contract of the resource API, but should
 * work at least for the Granite UI implementation which seems to always use this method.
 */
@ProviderType
public final class GraniteUiSyntheticResource extends SyntheticResource {

  private final ValueMap props;
  private final Map<String, Resource> children;

  private GraniteUiSyntheticResource(ResourceResolver resourceResolver,
      ResourceMetadata resourceMetadata, String resourceType,
      ValueMap props, Iterable<Resource> children) {
    super(resourceResolver, resourceMetadata, resourceType);
    this.props = props;
    this.children = childrenMap(children);
  }

  private GraniteUiSyntheticResource(ResourceResolver resourceResolver,
      String path,
      String resourceType,
      ValueMap props,
      Iterable<Resource> children) {
    super(resourceResolver, path, resourceType);
    this.props = props;
    this.children = childrenMap(children);
  }

  private static Map<String, Resource> childrenMap(Iterable<Resource> children) {
    Map<String, Resource> result = new LinkedHashMap<>();
    children.forEach(resource -> result.put(resource.getName(), resource));
    return result;
  }

  @SuppressWarnings({ "unchecked", "null" })
  @Override
  public <Type> Type adaptTo(Class<Type> type) {
    if (ValueMap.class.equals(type)) {
      return (Type)props;
    }
    else {
      return super.adaptTo(type);
    }
  }

  @Override
  public Iterator<Resource> listChildren() {
    return children.values().iterator();
  }

  @Override
  public Iterable<Resource> getChildren() {
    return children.values();
  }

  @Override
  public boolean hasChildren() {
    return !children.isEmpty();
  }

  @Override
  public Resource getChild(String relPath) {
    // naive implementation that only covers the simplest-possible case to detect the correct child
    Resource child = children.get(relPath);
    if (child != null) {
      return child;
    }
    return super.getChild(relPath);
  }

  private void addChild(Resource child) {
    children.put(child.getName(), child);
  }

  /**
   * Create synthetic resource.
   * @param resourceResolver Resource resolver
   * @param valueMap Properties
   * @return Resource
   */
  public static Resource create(@NotNull ResourceResolver resourceResolver, @NotNull ValueMap valueMap) {
    return create(resourceResolver, null, JcrConstants.NT_UNSTRUCTURED, valueMap);
  }

  /**
   * Create synthetic resource.
   * @param resourceResolver Resource resolver
   * @param path Resource path
   * @param resourceType Resource type
   * @return Resource
   */
  public static Resource create(@NotNull ResourceResolver resourceResolver, @Nullable String path, @NotNull String resourceType) {
    return create(resourceResolver, path, resourceType, ValueMap.EMPTY);
  }

  /**
   * Create synthetic resource.
   * @param resourceResolver Resource resolver
   * @param path Resource path
   * @param resourceType Resource type
   * @param valueMap Properties
   * @return Resource
   */
  public static Resource create(@NotNull ResourceResolver resourceResolver, @Nullable String path, @NotNull String resourceType,
      @NotNull ValueMap valueMap) {
    return new GraniteUiSyntheticResource(resourceResolver,
        path,
        resourceType,
        valueMap,
        ImmutableList.<Resource>of());
  }

  /**
   * Wrap a real resource and create a synthetic resource out of it.
   * @param resource Real resource
   * @return Resource
   */
  public static Resource wrap(@NotNull Resource resource) {
    return wrap(resource, resource.getValueMap(), resource.getChildren());
  }

  /**
   * Wrap a real resource and create a synthetic resource out of it.
   * @param resource Real resource
   * @param valueMap Properties to use instead of the real properties
   * @return Resource
   */
  public static Resource wrap(@NotNull Resource resource, @NotNull ValueMap valueMap) {
    return wrap(resource, valueMap, resource.getChildren());
  }

  /**
   * Wrap a real resource and create a synthetic resource out of it.
   * Merges the given properties with the existing properties of the resource.
   * @param resource Real resource
   * @param valueMap Properties to be merged with the real properties
   * @return Resource
   */
  public static Resource wrapMerge(@NotNull Resource resource, @NotNull ValueMap valueMap) {
    Map<String, Object> mergedProperties = new HashMap<>();
    mergedProperties.putAll(resource.getValueMap());
    mergedProperties.putAll(valueMap);
    return wrap(resource, new ValueMapDecorator(mergedProperties), resource.getChildren());
  }

  private static Resource wrap(Resource resource, ValueMap valueMap, Iterable<Resource> children) {
    return new GraniteUiSyntheticResource(resource.getResourceResolver(),
        resource.getResourceMetadata(),
        resource.getResourceType(),
        valueMap,
        children);
  }

  /**
   * Create synthetic resource child resource of the given parent resource.
   * @param parentResource Parent resource (has to be a {@link GraniteUiSyntheticResource} instance)
   * @param name Child resource name
   * @param resourceType Resource type
   * @return Resource
   */
  public static Resource child(@NotNull Resource parentResource, @NotNull String name, @NotNull String resourceType) {
    return child(parentResource, name, resourceType, ValueMap.EMPTY);
  }

  /**
   * Create synthetic resource child resource of the given parent resource.
   * @param parentResource Parent resource (has to be a {@link GraniteUiSyntheticResource} instance)
   * @param name Child resource name
   * @param resourceType Resource type
   * @param valueMap Properties
   * @return Resource
   */
  public static Resource child(@NotNull Resource parentResource, @NotNull String name, @NotNull String resourceType,
      @NotNull ValueMap valueMap) {
    Resource child = new GraniteUiSyntheticResource(parentResource.getResourceResolver(),
        parentResource.getPath() + "/" + name,
        resourceType,
        valueMap,
        ImmutableList.<Resource>of());
    if (parentResource instanceof GraniteUiSyntheticResource) {
      ((GraniteUiSyntheticResource)parentResource).addChild(child);
    }
    else {
      throw new IllegalArgumentException("Resource is not a GraniteUiSyntheticResource.");
    }
    return child;
  }

  /**
   * Copy the given source resource as synthetic child under the target parent resource, including all children.
   * @param targetParent Target parent resource
   * @param source Source resource
   */
  public static void copySubtree(@NotNull Resource targetParent, @NotNull Resource source) {
    Resource targetChild = GraniteUiSyntheticResource.child(targetParent, source.getName(), source.getResourceType(), source.getValueMap());
    for (Resource sourceChild : source.getChildren()) {
      copySubtree(targetChild, sourceChild);
    }
  }

}
