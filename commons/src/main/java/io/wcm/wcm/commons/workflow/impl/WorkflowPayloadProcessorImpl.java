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
package io.wcm.wcm.commons.workflow.impl;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.collection.ResourceCollectionManager;

import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.wcm.commons.workflow.WorkflowPayloadProcessor;

/**
 * Helper method for workflow payload processing.
 */
@Component(service = WorkflowPayloadProcessor.class)
public final class WorkflowPayloadProcessorImpl implements WorkflowPayloadProcessor {

  @Reference
  private ResourceCollectionManager resourceCollectionManager;

  private static final Logger log = LoggerFactory.getLogger(WorkflowPayloadProcessorImpl.class);

  @Override
  public void process(@NotNull String payloadPath,
      @NotNull Set<String> allowedResourceTypes,
      @NotNull Consumer<Resource> processor,
      @NotNull WorkflowSession workflowSession) {
    // Get resource resolver explicitly from workflow session
    ResourceResolver resourceResolver = AdaptTo.notNull(workflowSession, ResourceResolver.class);

    Resource payloadResource = resourceResolver.getResource(payloadPath);
    if (null == payloadResource) {
      log.warn("Getting payload resource '{}' in workflow failed.", payloadPath);
      return;
    }

    // Retrieve resource paths from potential resource collection in payload
    List<String> resourcePathsToProcess = ResourceCollectionUtil.getResourcePathsFromResource(payloadResource,
        allowedResourceTypes, resourceCollectionManager);
    if (!resourcePathsToProcess.isEmpty()) {
      for (String resourcePathToProcess : resourcePathsToProcess) {
        processSingleResourcePath(resourcePathToProcess, processor, resourceResolver);
      }
    }
    else {
      // If no resource paths found then just process the payload resource directly
      processor.accept(payloadResource);
    }
  }

  private static void processSingleResourcePath(@NotNull String path,
      @NotNull Consumer<Resource> processor,
      @NotNull ResourceResolver resourceResolver) {
    Resource resource = resourceResolver.getResource(path);
    if (null == resource) {
      log.warn("Resource '{}' in payload of workflow does not exist.", path);
      return;
    }
    processor.accept(resource);
  }

}
