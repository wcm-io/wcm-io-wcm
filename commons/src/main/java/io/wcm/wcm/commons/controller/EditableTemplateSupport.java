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
package io.wcm.wcm.commons.controller;

import javax.annotation.PostConstruct;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.osgi.annotation.versioning.ProviderType;

import com.day.cq.wcm.api.components.ComponentContext;

import io.wcm.sling.models.annotations.AemObject;
import io.wcm.wcm.commons.util.EditableTemplate;

/**
 * Helper model to check status in editable templates.
 */
@Model(adaptables = SlingHttpServletRequest.class)
@ProviderType
public final class EditableTemplateSupport {

  @AemObject(injectionStrategy = InjectionStrategy.OPTIONAL)
  private ComponentContext componentContext;

  private boolean editRestricted;

  @PostConstruct
  private void activate() {
    if (componentContext != null) {
      this.editRestricted = EditableTemplate.isEditRestricted(componentContext);
    }
  }

  /**
   * @return true if editing of this component is forbidden in the editable template definition
   */
  public boolean isEditRestricted() {
    return this.editRestricted;
  }

}
