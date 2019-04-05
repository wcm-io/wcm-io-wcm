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
package io.wcm.wcm.commons.util;

import static com.day.cq.commons.jcr.JcrConstants.JCR_CONTENT;
import static com.day.cq.commons.jcr.JcrConstants.JCR_PRIMARYTYPE;
import static com.day.cq.wcm.api.NameConstants.NT_TEMPLATE;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.jetbrains.annotations.NotNull;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.Template;
import com.day.cq.wcm.api.components.ComponentContext;

/**
 * Helper methods for editable AEM templates.
 */
public final class EditableTemplate {

  static final String NN_EDITABLE_TEMPLATE_INITIAL = "initial";
  static final String NN_EDITABLE_TEMPLATE_STRUCTURE = "structure";
  static final String NN_EDITABLE_TEMPLATE_POLICIES = "policies";

  static final String PN_EDITABLE = "editable";

  private EditableTemplate() {
    // static methods only
  }

  /**
   * Checks if editing of the given component in edit mode is forbidden due to a lock in the template editor
   * for this component.
   * @param wcmComponentContext WCM component context
   * @return true if editing the component is forbidden
   */
  public static boolean isEditRestricted(@NotNull ComponentContext wcmComponentContext) {
    Page page = wcmComponentContext.getPage();
    if (page == null) {
      return false;
    }
    ResourceResolver resourceResolver = wcmComponentContext.getResource().getResourceResolver();
    Template template = page.getTemplate();

    if (template == null || isPageInTemplateDefinition(page) || !isEditableTemplate(template)) {
      return false;
    }

    // check if the current resource path already points to a structure path in the editable template
    String resourcePath = wcmComponentContext.getResource().getPath();
    String templateStructurePath = template.getPath() + "/" + NN_EDITABLE_TEMPLATE_STRUCTURE + "/" + JCR_CONTENT;
    String resourcePathInStructure = null;
    if (StringUtils.startsWith(resourcePath, templateStructurePath + "/")) {
      resourcePathInStructure = resourcePath;
    }
    else {
      // resource path points to the current page, build structure path from path in page
      String relativePathInPage = StringUtils.substringAfter(resourcePath, page.getContentResource().getPath());
      if (StringUtils.isNotEmpty(relativePathInPage)) {
        resourcePathInStructure = templateStructurePath + relativePathInPage;
      }
    }
    if (resourcePathInStructure == null) {
      return false;
    }

    Resource resourceInStructure = resourceResolver.getResource(resourcePathInStructure);
    if (resourceInStructure == null) {
      return false;
    }
    return !resourceInStructure.getValueMap().get(PN_EDITABLE, false);
  }

  /**
   * Checks if the given template is an editable template.
   * @param template TEmplate
   * @return true if template is editable
   */
  private static boolean isEditableTemplate(@NotNull Template template) {
    Resource resource = template.adaptTo(Resource.class);
    if (resource != null) {
      return resource.getChild(NN_EDITABLE_TEMPLATE_INITIAL) != null
          && resource.getChild(NN_EDITABLE_TEMPLATE_STRUCTURE) != null
          && resource.getChild(NN_EDITABLE_TEMPLATE_POLICIES) != null;
    }
    return false;
  }

  /**
   * Checks if the given page is part of the editable template definition itself.
   * @param page Page
   * @return true if page is part of template definition.
   */
  private static boolean isPageInTemplateDefinition(Page page) {
    Resource resource = page.adaptTo(Resource.class);
    if (resource != null) {
      Resource parent = resource.getParent();
      if (parent != null) {
        return StringUtils.equals(NT_TEMPLATE, parent.getValueMap().get(JCR_PRIMARYTYPE, String.class));
      }
    }
    return false;
  }

}
