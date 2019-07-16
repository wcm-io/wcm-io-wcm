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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.adobe.granite.workflow.collection.ResourceCollection;
import com.adobe.granite.workflow.collection.ResourceCollectionManager;
import com.day.cq.dam.api.DamConstants;
import com.google.common.collect.ImmutableSet;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.commons.testcontext.AppAemContext;

@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
class ResourceCollectionUtilTest {

  private static final String CONTENT_ROOT = "/content/rctest";

  private static final Set<String> ASSET_TYPE_FILTER = ImmutableSet.of(DamConstants.NT_DAM_ASSET);

  private static final String TEST_RC_PATH = CONTENT_ROOT + "/rc_resource";
  private static final String TEST_NO_RC_PATH = CONTENT_ROOT + "/no_rc_resource";

  private static final String EXPECTED_PATH_1 = CONTENT_ROOT + "/expected_path_1";
  private static final String EXPECTED_PATH_2 = CONTENT_ROOT + "/expected_path_2";

  private final AemContext context = AppAemContext.newAemContext(ResourceResolverType.JCR_MOCK);

  private List<Node> collectionNodeList;

  @Mock
  private ResourceCollectionManager resourceCollectionManager;

  @BeforeEach
  void setUp() {
    collectionNodeList = new ArrayList<>();
  }

  // Tests (getResourcePathsFromResource)

  @Test
  void getResourcePathsFromResource_noResourceCollection_empty() {
    // arrange
    Resource resource = createNoResourceCollectionResource();

    // act
    List<String> paths = getResourcePathsFromResource(resource);

    // assert
    assertTrue(paths.isEmpty());
  }

  @Test
  void getResourcePathsFromResource_emptyResourceCollection_empty() {
    // arrange
    Resource resource = createResourceCollectionResource();

    // act
    List<String> paths = getResourcePathsFromResource(resource);

    // assert
    assertTrue(paths.isEmpty());
  }

  @Test
  void getResourcePathsFromResource_resourceCollectionWithNullNode_emptyNoException() {
    // arrange
    Resource resource = createResourceCollectionResourceWithNullNode();

    // act
    List<String> paths = getResourcePathsFromResource(resource);

    // assert
    assertTrue(paths.isEmpty());
  }

  @Test
  void getResourcePathsFromResource_exceptionOnRcList_emptyNoException() throws RepositoryException {
    // arrange
    Resource resource = createResourceCollectionResourceWithExceptionOnList();

    // act
    List<String> paths = getResourcePathsFromResource(resource);

    // assert
    assertTrue(paths.isEmpty());
  }

  @Test
  void getResourcePathsFromResource_directResourceCollectionWithNodeList_expectedResourcePaths() throws RepositoryException {
    // arrange
    Resource resource = createResourceCollectionResourceWithNodes();
    initializeExpectedPaths();

    // act
    List<String> paths = getResourcePathsFromResource(resource);

    // assert
    assertExpectedPaths(paths);
  }

  @Test
  void getResourcePathsFromResource_indirectResourceCollectionWithNodeList_expectedResourcePaths() throws RepositoryException {
    // arrange
    Resource resource = createIndirectResourceCollectionResourceWithNodes();
    initializeExpectedPaths();

    // act
    List<String> paths = getResourcePathsFromResource(resource);

    // assert
    assertExpectedPaths(paths);
  }

  // Private methods (Arrange)

  @SuppressWarnings("null")
  private void initializeResourceCollectionWithNodes() throws RepositoryException {
    ResourceCollection resourceCollection = mock(ResourceCollection.class);
    when(resourceCollection.list(ArgumentMatchers.eq(ASSET_TYPE_FILTER.toArray(new String[ASSET_TYPE_FILTER.size()]))))
        .thenReturn(collectionNodeList);
    when(resourceCollectionManager.createCollection(any(Node.class))).thenReturn(resourceCollection);
  }

  private Resource createResourceCollectionResource(String path) {
    return context.create().resource(path, JcrConstants.JCR_PRIMARYTYPE, ResourceCollectionUtil.NT_RESOURCE_COLLECTION);
  }

  private Resource createResourceCollectionResource() {
    return createResourceCollectionResource(TEST_RC_PATH);
  }

  private Resource createNoResourceCollectionResource(String path) {
    return context.create().resource(path);
  }

  private Resource createNoResourceCollectionResource() {
    return createNoResourceCollectionResource(TEST_NO_RC_PATH);
  }

  @SuppressWarnings("null")
  private Resource createResourceCollectionResourceWithNullNode() {
    Resource resource = spy(createResourceCollectionResource());
    when(resource.adaptTo(Node.class)).thenReturn(null);
    return resource;
  }

  @SuppressWarnings("null")
  private Resource createResourceCollectionResourceWithExceptionOnList() throws RepositoryException {
    Resource resource = createResourceCollectionResource();
    ResourceCollection resourceCollection = mock(ResourceCollection.class);
    when(resourceCollection.list(any(String[].class))).thenThrow(new RepositoryException());
    when(resourceCollectionManager.createCollection(any(Node.class))).thenReturn(resourceCollection);
    return resource;
  }

  private Resource createResourceCollectionResourceWithNodes() throws RepositoryException {
    Resource resource = createResourceCollectionResource();
    initializeResourceCollectionWithNodes();
    return resource;
  }

  private Resource createIndirectResourceCollectionResourceWithNodes() throws RepositoryException {
    Resource resource = createNoResourceCollectionResource();
    createNoResourceCollectionResource(resource.getPath() + "/no_rc_child");
    createResourceCollectionResource(resource.getPath() + "/rc_child");
    initializeResourceCollectionWithNodes();
    return resource;
  }

  @SuppressWarnings("null")
  private void addNodeWithPath(List<Node> list, String path) throws RepositoryException {
    Node node = mock(Node.class);
    when(node.getPath()).thenReturn(path);
    list.add(node);
  }

  private void initializeExpectedPaths() throws RepositoryException {
    addNodeWithPath(collectionNodeList, EXPECTED_PATH_1);
    addNodeWithPath(collectionNodeList, EXPECTED_PATH_2);
  }

  // Private methods (Act)

  private @NotNull List<String> getResourcePathsFromResource(Resource resource) {
    return ResourceCollectionUtil.getResourcePathsFromResource(resource, ASSET_TYPE_FILTER, resourceCollectionManager);
  }

  // Private methods (Assert)

  private void assertExpectedPaths(List<String> paths) {
    assertEquals(2, paths.size());
    assertEquals(EXPECTED_PATH_1, paths.get(0));
    assertEquals(EXPECTED_PATH_2, paths.get(1));
  }

}
