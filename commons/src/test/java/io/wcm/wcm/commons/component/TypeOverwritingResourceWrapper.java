/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2020 wcm.io
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

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceWrapper;

/**
 * Simulate behavior of SlingRequestDispatcher$TypeOverwritingResourceWrapper
 */
class TypeOverwritingResourceWrapper extends ResourceWrapper {

  private final String resourceType;

  TypeOverwritingResourceWrapper(Resource delegatee, String resourceType) {
    super(delegatee);
    this.resourceType = resourceType;
  }

  @Override
  public String getResourceType() {
    return resourceType;
  }

  /**
   * Overwrite this here because the wrapped resource will return null as
   * a super type instead of the resource type overwritten here
   */
  @Override
  public String getResourceSuperType() {
    return null;
  }

  @Override
  public boolean isResourceType(final String value) {
    return this.getResourceResolver().isResourceType(this, value);
  }

}
