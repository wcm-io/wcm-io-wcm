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
package io.wcm.wcm.commons.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.workflow.collection.ResourceCollection;
import com.adobe.granite.workflow.collection.ResourceCollectionManager;

/**
 * Helper methods for resource collection handling
 */
final class ResourceCollectionUtil {

  private static final Logger log = LoggerFactory.getLogger(ResourceCollectionUtil.class);

  static final String NT_RESOURCE_COLLECTION = "vlt:PackageDefinition";

  private ResourceCollectionUtil() {
    // static methods only
  }

  /**
   * Returns the resource paths from the given resource.
   * If the resource is not a resource collection then an empty list is returned.
   * @param resourceCollectionResource potential resource collection resource
   * @param allowedResourceTypes allowed resource types to return
   * @param resourceCollectionManager resource collection manager
   * @return resource paths or empty list
   */
  static @NotNull List<String> getResourcePathsFromResource(
      @NotNull Resource resourceCollectionResource,
      @NotNull Set<String> allowedResourceTypes,
      @NotNull ResourceCollectionManager resourceCollectionManager) {
    try {
      Node resourceCollectionNode = resourceCollectionResource.adaptTo(Node.class);
      ResourceCollection resourceCollection = getResourceCollection(resourceCollectionNode, resourceCollectionManager);
      return getResourcePathsFromCollection(resourceCollection, allowedResourceTypes);
    }
    catch (RepositoryException e) {
      log.warn("Retrieving resource collection from resource '{}' failed.", resourceCollectionResource.getPath(), e);
      return new ArrayList<>();
    }
  }

  private static @Nullable ResourceCollection getResourceCollection(
      @Nullable Node resourceCollectionNode,
      @NotNull ResourceCollectionManager resourceCollectionManager) throws RepositoryException {
    if (resourceCollectionNode == null) {
      return null;
    }

    if (resourceCollectionNode.isNodeType(NT_RESOURCE_COLLECTION)) {
      return resourceCollectionManager.createCollection(resourceCollectionNode);
    }

    // Search child node entries for resource collection
    NodeIterator nodeIterator = resourceCollectionNode.getNodes();
    while (nodeIterator.hasNext()) {
      Node childNode = nodeIterator.nextNode();
      ResourceCollection resourceCollection = getResourceCollection(childNode, resourceCollectionManager);
      if (resourceCollection != null) {
        return resourceCollection;
      }
    }

    return null;
  }

  private static @NotNull List<String> getResourcePathsFromCollection(
      @Nullable ResourceCollection resourceCollection,
      @NotNull Set<String> allowedResourceTypes) throws RepositoryException {
    List<String> paths = new ArrayList<>();
    if (resourceCollection == null) {
      return paths;
    }

    String[] allowsResourceTypesArray = allowedResourceTypes.toArray(new String[allowedResourceTypes.size()]);
    List<Node> members = resourceCollection.list(allowsResourceTypesArray);
    for (Node member : members) {
      String memberPath = member.getPath();
      paths.add(memberPath);
    }

    return paths;
  }

}
