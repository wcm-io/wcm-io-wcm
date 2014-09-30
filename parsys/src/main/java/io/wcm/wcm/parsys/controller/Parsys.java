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

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
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

  static final String DECORATION_TAG_NAME = "div";
  static final String NEWAREA_RESOURCE_PATH = "./*";
  static final String NEWAREA_CSS_CLASS_NAME = "new";
  static final String NEWAREA_RESOURCE_TYPE_SUFFIX = "/newpar";

  @SlingObject
  private Resource currentResource;

  @AemObject
  private WCMMode wcmMode;

  @AemObject
  private ComponentContext componentContext;

  @RequestAttribute(optional = true)
  @Default(values = DECORATION_TAG_NAME)
  private String decorationTagName;

  private List<Item> items;

  @PostConstruct
  protected void activate() {
    items = new ArrayList<>();
    for (Resource childResource : currentResource.getChildren()) {
      items.add(createResourceItem(childResource));
    }
    if (wcmMode != WCMMode.DISABLED) {
      items.add(createNewAreaItem());
    }
  }

  private Item createResourceItem(Resource resource) {
    return new Item(resource.getPath(), null, this.decorationTagName, null, false);
  }

  private Item createNewAreaItem() {
    String newAreaResourceType = componentContext.getComponent().getPath() + NEWAREA_RESOURCE_TYPE_SUFFIX;
    return new Item(NEWAREA_RESOURCE_PATH, newAreaResourceType, this.decorationTagName, NEWAREA_CSS_CLASS_NAME, true);
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
    private final String decorationTagName;
    private final String cssClassName;
    private final boolean newArea;

    private Item(String resourcePath, String resourceType, String decorationTagName, String cssClassName, boolean newArea) {
      this.resourcePath = resourcePath;
      this.resourceType = resourceType;
      this.cssClassName = cssClassName;
      this.decorationTagName = decorationTagName;
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
     * @return Decoraction tag name
     */
    public String getDecorationTagName() {
      return this.decorationTagName;
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
