/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
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
package io.wcm.wcm.parsys.controller;

import io.wcm.sling.models.annotations.AemObject;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.RequestAttribute;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;

import com.day.cq.wcm.api.WCMMode;
import com.day.cq.wcm.api.components.ComponentContext;

/**
 * Controller for paragraph system.
 * Unlike the AEM-builtin paragraph systems this parsys does not support column controls or iparsys inheritance,
 * but is only a simple paragraph system which allows full control about the markup generated for the child resources
 * and the new area.
 */
@Model(adaptables = SlingHttpServletRequest.class)
public final class Parsys {

  static final String RA_PARSYS_PARENT_RESOURCE = "parsysParentResource";
  static final String NEWAREA_RESOURCE_PATH = "./*";
  static final String NEWAREA_CSS_CLASS_NAME = "new";
  static final String NEWAREA_CHILD_NAME = "newpar";
  static final String FALLBACK_NEWAREA_RESOURCE_TYPE = "/apps/wcm-io/wcm/parsys/components/parsys/newpar";

  /**
   * Allows to override the resource which children are iterated to display the parsys.
   */
  @RequestAttribute(name = RA_PARSYS_PARENT_RESOURCE, optional = true)
  private Resource parsysParentResource;

  @SlingObject
  private Resource currentResource;
  @SlingObject
  private ResourceResolver resolver;
  @AemObject
  private WCMMode wcmMode;
  @AemObject
  private ComponentContext componentContext;

  private List<Item> items;

  @PostConstruct
  protected void activate() {
    items = new ArrayList<>();
    if (parsysParentResource == null) {
      parsysParentResource = currentResource;
    }
    for (Resource childResource : parsysParentResource.getChildren()) {
      items.add(createResourceItem(childResource));
    }
    if (wcmMode != WCMMode.DISABLED) {
      items.add(createNewAreaItem());
    }
  }

  private Item createResourceItem(Resource resource) {
    return new Item(resource.getPath(), null, null, false);
  }

  private Item createNewAreaItem() {
    String newAreaResourceType = getNewAreaResourceType(componentContext.getComponent().getPath());
    return new Item(NEWAREA_RESOURCE_PATH, newAreaResourceType, NEWAREA_CSS_CLASS_NAME, true);
  }

  /**
   * Get resource type for new area - from current parsys component or from a supertype component.
   * @param componentPath Component path
   * @return Resource type (never null)
   */
  private String getNewAreaResourceType(String componentPath) {
    Resource componentResource = resolver.getResource(componentPath);
    if (componentResource != null) {
      if (componentResource.getChild(NEWAREA_CHILD_NAME) != null) {
        return componentPath + "/" + NEWAREA_CHILD_NAME;
      }
      String resourceSuperType = componentResource.getResourceSuperType();
      if (StringUtils.isNotEmpty(resourceSuperType)) {
        return getNewAreaResourceType(resourceSuperType);
      }
    }
    return FALLBACK_NEWAREA_RESOURCE_TYPE;
  }

  /**
   * @return Paragraph system items
   */
  public List<Item> getItems() {
    return this.items;
  }


  /**
   * Paragraph system item.
   */
  public static final class Item {

    private final String resourcePath;
    private final String resourceType;
    private final String cssClassName;
    private final boolean newArea;

    private Item(String resourcePath, String resourceType, String cssClassName, boolean newArea) {
      this.resourcePath = resourcePath;
      this.resourceType = resourceType;
      this.cssClassName = cssClassName;
      this.newArea = newArea;
    }

    /**
     * @return Resource path
     */
    public String getResourcePath() {
      return this.resourcePath;
    }

    /**
     * @return Resource type
     */
    public String getResourceType() {
      return this.resourceType;
    }

    /**
     * @return CSS classes
     */
    public String getCssClassName() {
      return this.cssClassName;
    }

    public boolean isNewArea() {
      return this.newArea;
    }

  }

}
