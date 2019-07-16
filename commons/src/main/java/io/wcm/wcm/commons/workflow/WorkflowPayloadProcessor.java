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

import java.util.Set;
import java.util.function.Consumer;

import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.osgi.annotation.versioning.ConsumerType;

import com.adobe.granite.workflow.WorkflowSession;

/**
 * Helper method for workflow payload processing.
 */
@ConsumerType
public interface WorkflowPayloadProcessor {

  /**
   * Processes the payload path with the given processor.
   * If the payload represents a resource collection then only the resources from the resource collection with the
   * allowed resource types are processed.
   * @param payloadPath payload path to process
   * @param allowedResourceTypes allowed resource types to process
   * @param processor processes the resource(s)
   * @param workflowSession workflow session
   */
  void process(@NotNull String payloadPath,
      @NotNull Set<String> allowedResourceTypes,
      @NotNull Consumer<Resource> processor,
      @NotNull WorkflowSession workflowSession);

}
