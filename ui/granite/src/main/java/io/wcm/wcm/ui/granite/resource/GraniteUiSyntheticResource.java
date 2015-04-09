/* Copyright (c) pro!vision GmbH. All rights reserved. */
package io.wcm.wcm.ui.granite.resource;

import io.wcm.sling.commons.resource.ImmutableValueMap;

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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Extended version of {@link SyntheticResource} that allows to pass an own value map and optional child resources.
 * Please note: Accessing child resources does only work when accessing {@link Resource#listChildren()}, and
 * not when calling the same method on resourceResolver. This breaks the contract of the resource API, but should
 * work at least for the GraniteUI implementation which seems to always use this method.
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
      String path, String resourceType,
      ValueMap props, Iterable<Resource> children) {
    super(resourceResolver, path, resourceType);
    this.props = props;
    this.children = Lists.newArrayList(children);
  }

  @SuppressWarnings("unchecked")
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
   * @param path Resource path
   * @param resourceType Resource type
   * @return Resource
   */
  public static Resource create(ResourceResolver resourceResolver, String path, String resourceType) {
    return create(resourceResolver, path, resourceType, ImmutableValueMap.of());
  }

  /**
   * Create synthetic resource.
   * @param resourceResolver Resource resolver
   * @param path Resource path
   * @param resourceType Resource type
   * @param valueMap Properties
   * @return Resource
   */
  public static Resource create(ResourceResolver resourceResolver, String path, String resourceType, ValueMap valueMap) {
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
  public static Resource wrap(Resource resource) {
    return wrap(resource, resource.getValueMap(), resource.getChildren());
  }

  /**
   * Wrap a real resource and create a synthetic resource out of it.
   * @param resource Real resource
   * @param valueMap Properties to use instead of the real properties
   * @return Resource
   */
  public static Resource wrap(Resource resource, ValueMap valueMap) {
    return wrap(resource, valueMap, resource.getChildren());
  }

  /**
   * Wrap a real resource and create a synthetic resource out of it.
   * Merges the given properties with the existing properties of the resource.
   * @param resource Real resource
   * @param valueMap Properties to be merged with the real properties
   * @return Resource
   */
  public static Resource wrapMerge(Resource resource, ValueMap valueMap) {
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
  public static Resource child(Resource parentResource, String name, String resourceType) {
    return child(parentResource, name, resourceType, ImmutableValueMap.of());
  }

  /**
   * Create synthetic resource child resource of the given parent resource.
   * @param parentResource Parent resource (has to be a {@link GraniteUiSyntheticResource} instance)
   * @param name Child resource name
   * @param resourceType Resource type
   * @param valueMap Properties
   * @return Resource
   */
  public static Resource child(Resource parentResource, String name, String resourceType, ValueMap valueMap) {
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
