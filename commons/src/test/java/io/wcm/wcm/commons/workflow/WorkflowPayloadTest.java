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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.collection.ResourceCollection;
import com.adobe.granite.workflow.collection.ResourceCollectionManager;
import com.day.cq.dam.api.DamConstants;
import com.google.common.collect.ImmutableSet;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.commons.testcontext.AppAemContext;

@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
class WorkflowPayloadTest {

  private static final String DAM_ROOT = "/content/dam/wftest";

  private static final Set<String> ASSET_TYPE_FILTER = ImmutableSet.of(DamConstants.NT_DAM_ASSET);

  private static final String INVALID_PATH_1 = DAM_ROOT + "/invalid_resource_1";
  private static final String INVALID_PATH_2 = DAM_ROOT + "/invalid_resource_2";
  private static final String VALID_NON_RC_PATH = DAM_ROOT + "/valid_non_rc_resource";
  private static final String VALID_RC_PATH = DAM_ROOT + "/valid_rc_resource";

  private static final String VALID_RESOURCE_PATH_1 = DAM_ROOT + "/valid_resource_1";
  private static final String VALID_RESOURCE_PATH_2 = DAM_ROOT + "/valid_resource_2";

  private final AemContext context = AppAemContext.newAemContext(ResourceResolverType.JCR_MOCK);

  @Mock
  private Consumer<Resource> process;
  @Mock
  private WorkflowSession workflowSession;
  @Mock
  private ResourceCollectionManager resourceCollectionManager;

  private List<Node> collectionNodeList;

  @BeforeEach
  @SuppressWarnings("null")
  void setUp() {
    when(workflowSession.adaptTo(ResourceResolver.class)).thenReturn(context.resourceResolver());
    collectionNodeList = new ArrayList<>();
  }

  // Tests (process)

  @Test
  void processPayloadPath_emptyPath_noExceptionAndNoProcessing() {
    processPayloadPathWithDefaults(StringUtils.EMPTY);
    verifyNoMoreInteractions(process);
  }

  @Test
  void processPayloadPath_blankPath_noExceptionAndNoProcessing() {
    processPayloadPathWithDefaults("     ");
    verifyNoMoreInteractions(process);
  }

  @Test
  void processPayloadPath_invalidPath_noExceptionAndNoProcessing() {
    processPayloadPathWithDefaults(INVALID_PATH_1);
    verifyNoMoreInteractions(process);
  }

  @Test
  void processPayloadPath_validNonResourceCollectionPath_resourceProcessed() {
    // arrange
    String resourcePath = VALID_NON_RC_PATH;
    createNoResourceCollectionResource(resourcePath);

    // act
    processPayloadPathWithDefaults(resourcePath);

    // assert
    verifyResourcesProcessed(resourcePath);
  }

  @Test
  void processPayloadPath_validResourceCollectionPathWithoutResources_resourceProcessedDirectly() throws RepositoryException {
    // arrange
    String resourcePath = VALID_RC_PATH;
    createResourceCollectionResourceWithNodes(resourcePath);

    // act
    processPayloadPathWithDefaults(resourcePath);

    // assert
    verifyResourcesProcessed(resourcePath);
  }

  @Test
  void processPayloadPath_validResourceCollectionPathWithResources_onlyCollectionResourcesProcessed() throws RepositoryException {
    // arrange
    String resourcePath = VALID_RC_PATH;
    String[] rcResourcePaths = { VALID_RESOURCE_PATH_1, VALID_RESOURCE_PATH_2 };
    createResourceCollectionResourceWithNodes(resourcePath);
    initializeExpectedResourceCollectionPaths(rcResourcePaths);

    // act
    processPayloadPathWithDefaults(resourcePath);

    // assert
    verifyResourcesProcessed(rcResourcePaths);
  }

  @Test
  void processPayloadPath_validResourceCollectionPathWithSomeInvalidResources_onlyValidCollectionResourcesProcessed() throws RepositoryException {
    // arrange
    String resourcePath = VALID_RC_PATH;
    String[] rcResourcePaths = { VALID_RESOURCE_PATH_1, VALID_RESOURCE_PATH_2 };
    createResourceCollectionResourceWithNodes(resourcePath);
    initializeResourceCollectionEntryWithInvalidPath(INVALID_PATH_1);
    initializeExpectedResourceCollectionPaths(rcResourcePaths);
    initializeResourceCollectionEntryWithInvalidPath(INVALID_PATH_2);

    // act
    processPayloadPathWithDefaults(resourcePath);

    // assert
    verifyResourcesProcessed(rcResourcePaths);
  }

  // Private methods (Arrange)

  private void processPayloadPathWithDefaults(String path) {
    WorkflowPayload.process(path, ASSET_TYPE_FILTER, process, workflowSession, resourceCollectionManager);
  }

  private void createNodeWithPathAndAddToList(List<Node> list, String path) {
    Node node = createNoResourceCollectionResource(path).adaptTo(Node.class);
    list.add(node);
  }

  private void initializeExpectedResourceCollectionPaths(String... expectedPaths) {
    for (String path : expectedPaths) {
      createNodeWithPathAndAddToList(collectionNodeList, path);
    }
  }

  @SuppressWarnings("null")
  private void initializeResourceCollectionEntryWithInvalidPath(String path) throws RepositoryException {
    Node node = mock(Node.class);
    when(node.getPath()).thenReturn(path);
    collectionNodeList.add(node);
  }

  @SuppressWarnings("null")
  private void initializeResourceCollectionWithNodes() throws RepositoryException {
    ResourceCollection resourceCollection = mock(ResourceCollection.class);
    when(resourceCollection.list(any(String[].class))).thenReturn(collectionNodeList);
    when(resourceCollectionManager.createCollection(any(Node.class))).thenReturn(resourceCollection);
  }

  private Resource createNoResourceCollectionResource(String path) {
    return context.create().resource(path);
  }

  private void createResourceCollectionResource(String path) {
    context.create().resource(path, JcrConstants.JCR_PRIMARYTYPE, ResourceCollectionUtil.NT_RESOURCE_COLLECTION);
  }

  private void createResourceCollectionResourceWithNodes(String resourcePath) throws RepositoryException {
    createResourceCollectionResource(resourcePath);
    initializeResourceCollectionWithNodes();
  }

  // Private methods (Assert)

  private void verifyResourcesProcessed(String... expectedProcessedResourcePaths) {
    ArgumentCaptor<Resource> argumentCaptor = ArgumentCaptor.forClass(Resource.class);
    verify(process, times(expectedProcessedResourcePaths.length)).accept(argumentCaptor.capture());
    assertTrue(argumentCaptor.getAllValues().stream()
        .allMatch(r -> Arrays.stream(expectedProcessedResourcePaths).anyMatch(path -> path.equals(r.getPath()))));
  }

}
