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
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.SyntheticResource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.day.cq.commons.jcr.JcrConstants;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Extended version of {@link SyntheticResource} that allows to pass an own value map and optional child resources.
 * Please note: Accessing child resources does only work when accessing {@link Resource#listChildren()}, and
 * not when calling the same method on resourceResolver. This breaks the contract of the resource API, but should
 * work at least for the Granite UI implementation which seems to always use this method.
 */
public final class GraniteUiSyntheticResource extends SyntheticResource {

  private final ValueMap props;
  private final List<Resource> children;

  private GraniteUiSyntheticResource(ResourceResolver resourceResolver,
      ResourceMetadata resourceMetadata, String resourceType,
      ValueMap props, Iterable<Resource> children) {
    super(resourceResolver, resourceMetadata, resourceType);
    this.props = props;
    this.children = Lists.newArrayList(children);
  }

  private GraniteUiSyntheticResource(ResourceResolver resourceResolver,
      String path,
      String resourceType,
      ValueMap props,
      Iterable<Resource> children) {
    super(resourceResolver, path, resourceType);
    this.props = props;
    this.children = Lists.newArrayList(children);
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
    return children.iterator();
  }

  @Override
  public Iterable<Resource> getChildren() {
    return children;
  }

  @Override
  public boolean hasChildren() {
    return children.iterator().hasNext();
  }

  @Override
  public Resource getChild(String relPath) {
    for (Resource resource : children) {
      // naive implementation that only covers the simplest-possible case to detect the correct child
      if (StringUtils.equals(resource.getName(), relPath)) {
        return resource;
      }
    }
    return super.getChild(relPath);
  }

  private void addChild(Resource child) {
    children.add(child);
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

}
