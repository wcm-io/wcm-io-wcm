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
package io.wcm.wcm.ui.granite.components.pathfield;

import org.apache.sling.api.resource.Resource;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Represents a column item in the column view.
 */
@ProviderType
public final class ColumnItem {

  private final Resource resource;
  private String resourceType;
  private Boolean active;

  ColumnItem(Resource resource) {
    this.resource = resource;
  }

  public Resource getResource() {
    return this.resource;
  }

  public String getResourceType() {
    return this.resourceType;
  }

  ColumnItem resourceType(String value) {
    this.resourceType = value;
    return this;
  }

  public Boolean getActive() {
    return this.active;
  }

  ColumnItem active(Boolean value) {
    this.active = value;
    return this;
  }

  public String getItemId() {
    return resource.getPath();
  }

}
