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

import static io.wcm.wcm.parsys.ParsysNameConstants.PN_PARSYS_GENERATE_DEAFULT_CSS;
import static io.wcm.wcm.parsys.ParsysNameConstants.PN_PARSYS_NEWAREA_CSS;
import static io.wcm.wcm.parsys.ParsysNameConstants.PN_PARSYS_PARAGRAPH_CSS;
import static io.wcm.wcm.parsys.ParsysNameConstants.PN_PARSYS_PARAGRAPH_ELEMENT;
import static io.wcm.wcm.parsys.ParsysNameConstants.PN_PARSYS_PARAGRAPH_NODECORATION_WCMMODE;
import static io.wcm.wcm.parsys.ParsysNameConstants.PN_PARSYS_PARAGRAPH_VALIDATE;
import static io.wcm.wcm.parsys.ParsysNameConstants.PN_PARSYS_WRAPPER_CSS;
import static io.wcm.wcm.parsys.ParsysNameConstants.PN_PARSYS_WRAPPER_ELEMENT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.RequestAttribute;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.factory.ModelClassException;
import org.apache.sling.models.factory.ModelFactory;
import org.jetbrains.annotations.NotNull;
import org.osgi.annotation.versioning.ProviderType;

import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.export.json.ContainerExporter;
import com.adobe.cq.export.json.ExporterConstants;
import com.adobe.cq.export.json.SlingModelFilter;
import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.WCMMode;
import com.day.cq.wcm.api.components.Component;
import com.day.cq.wcm.api.components.ComponentContext;
import com.day.cq.wcm.api.components.ComponentManager;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableMap;

import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.sling.models.annotations.AemObject;
import io.wcm.wcm.commons.component.ComponentPropertyResolution;
import io.wcm.wcm.commons.component.ComponentPropertyResolver;
import io.wcm.wcm.commons.component.ComponentPropertyResolverFactory;
import io.wcm.wcm.parsys.ParsysItem;

/**
 * Controller for paragraph system.
 * Unlike the AEM-builtin paragraph systems this parsys does not support column controls or iparsys inheritance,
 * but is only a simple paragraph system which allows full control about the markup generated for the child resources
 * and the new area.
 */
@Model(adaptables = SlingHttpServletRequest.class,
    adapters = { Parsys.class, ContainerExporter.class, ComponentExporter.class },
    resourceType = Parsys.RESOURCE_TYPE)
@Exporter(name = ExporterConstants.SLING_MODEL_EXPORTER_NAME, extensions = ExporterConstants.SLING_MODEL_EXTENSION)
@ProviderType
public final class Parsys implements ContainerExporter {

  static final String RESOURCE_TYPE = "wcm-io/wcm/parsys/components/parsys";
  static final String RA_PARSYS_PARENT_RESOURCE = "parsysParentResource";
  static final String SECTION_DEFAULT_CLASS_NAME = "section";
  static final String NEWAREA_RESOURCE_PATH = "./*";
  static final String NEWAREA_STYLE = "clear:both";
  static final String NEWAREA_CSS_CLASS_NAME = "new";
  static final String NEWAREA_CHILD_NAME = "newpar";
  static final String FALLBACK_NEWAREA_RESOURCE_TYPE = "wcm-io/wcm/parsys/components/parsys/newpar";
  static final String DEFAULT_ELEMENT_NAME = "div";

  /**
   * Allows to override the resource which children are iterated to display the parsys.
   */
  @RequestAttribute(name = RA_PARSYS_PARENT_RESOURCE, injectionStrategy = InjectionStrategy.OPTIONAL)
  private Resource parsysParentResource;

  @SlingObject
  private SlingHttpServletRequest request;
  @SlingObject
  private Resource currentResource;
  @SlingObject
  private ResourceResolver resolver;
  @AemObject
  private WCMMode wcmMode;
  @AemObject
  private ComponentContext componentContext;
  @OSGiService
  private ModelFactory modelFactory;
  @OSGiService
  private ComponentPropertyResolverFactory componentPropertyResolverFactory;
  @OSGiService
  private SlingModelFilter slingModelFilter;

  private ComponentManager componentManager;

  private boolean generateDefaultCss;
  private String paragraphCss;
  private String newAreaCss;
  private String paragraphElementName;
  private boolean paragraphDecoration;
  private boolean paragraphValidate;
  private String wrapperElementName;
  private String wrapperCss;

  private List<Item> items;
  private Map<String, ? extends ComponentExporter> childModels;
  private String[] exportedItemsOrder;

  @PostConstruct
  private void activate() {
    // read customize properties from parsys component
    try (ComponentPropertyResolver componentPropertyResolver = componentPropertyResolverFactory.get(componentContext)
        .componentPropertiesResolution(ComponentPropertyResolution.RESOLVE_INHERIT)) {
      generateDefaultCss = componentPropertyResolver.get(PN_PARSYS_GENERATE_DEAFULT_CSS, true);
      paragraphCss = componentPropertyResolver.get(PN_PARSYS_PARAGRAPH_CSS, String.class);
      newAreaCss = componentPropertyResolver.get(PN_PARSYS_NEWAREA_CSS, String.class);
      paragraphElementName = componentPropertyResolver.get(PN_PARSYS_PARAGRAPH_ELEMENT, String.class);
      wrapperElementName = componentPropertyResolver.get(PN_PARSYS_WRAPPER_ELEMENT, String.class);
      wrapperCss = componentPropertyResolver.get(PN_PARSYS_WRAPPER_CSS, String.class);
      paragraphValidate = componentPropertyResolver.get(PN_PARSYS_PARAGRAPH_VALIDATE, false);

      // check decoration
      String[] paragraphNoDecorationWcmMode = componentPropertyResolver.get(PN_PARSYS_PARAGRAPH_NODECORATION_WCMMODE, String[].class);
      paragraphDecoration = getDecoration(paragraphNoDecorationWcmMode, wcmMode);
    }

    // prepare paragraph items
    items = new ArrayList<>();
    if (parsysParentResource == null) {
      parsysParentResource = currentResource;
    }
    for (Resource childResource : parsysParentResource.getChildren()) {
      Item item = createResourceItem(childResource);
      if (wcmMode != WCMMode.DISABLED || item.isValid()) {
        items.add(item);
      }
    }
    if (wcmMode != WCMMode.DISABLED) {
      items.add(createNewAreaItem());
    }
  }

  private static boolean getDecoration(String[] paragraphNoDecorationWcmMode, WCMMode wcmMode) {
    if (paragraphNoDecorationWcmMode != null && paragraphNoDecorationWcmMode.length > 0) {
      for (String wcmModeItem : paragraphNoDecorationWcmMode) {
        if (StringUtils.equalsIgnoreCase(wcmMode.name(), wcmModeItem)) {
          return false;
        }
      }
    }
    return true;
  }

  private Item createResourceItem(Resource resource) {
    CssBuilder css = new CssBuilder();
    if (generateDefaultCss) {
      css.add(SECTION_DEFAULT_CLASS_NAME);
    }
    css.add(paragraphCss);

    Map<String,String> htmlTagAttrs = getComponentHtmlTagAttributes(resource.getResourceType());

    // apply html tag attributes from component definition
    String itemElementName = paragraphElementName;
    if (StringUtils.isEmpty(itemElementName)) {
      itemElementName = StringUtils.defaultString(htmlTagAttrs.get(NameConstants.PN_TAG_NAME), DEFAULT_ELEMENT_NAME);
    }
    if (StringUtils.isEmpty(paragraphCss)) {
      css.add(htmlTagAttrs.get("class"));
    }

    // try to check valid state of paragraph item
    boolean valid = true;
    if (paragraphValidate) {
      Optional<Boolean> validStatus = isParagraphValid(resource);
      if (validStatus.isPresent()) {
        valid = validStatus.get();
      }
    }

    return new Item(resource.getPath())
        .elementName(itemElementName)
        .cssClassName(css.build())
        .decorate(paragraphDecoration)
        .valid(valid);
  }

  /**
   * Checks if the given paragraph is valid.
   * @param resource Resource
   * @return if the return value is empty there is no model associated with this resource, or
   *         it does not support validation via {@link ParsysItem} interface. Otherwise it contains the valid status.
   */
  @SuppressWarnings({ "null", "unused" })
  private Optional<@NotNull Boolean> isParagraphValid(Resource resource) {
    // try to get model adapting from request associated with the resource implementing ParsysItem
    ParsysItem parsysItem = modelFactory.getModelFromWrappedRequest(request, resource, ParsysItem.class);
    if (parsysItem != null) {
      return Optional.of(parsysItem.isValid());
    }
    else {
      try {
        // alternatively try to get model adapting from resource, and check if it implements the ParsysItem interface
        Object model = modelFactory.getModelFromResource(resource);
        if (model instanceof ParsysItem) {
          return Optional.of(((ParsysItem)model).isValid());
        }
      }
      catch (ModelClassException ex) {
        // ignore if no model was registered for this resource type
      }
    }
    return Optional.empty();
  }

  /**
   * Get HTML tag attributes from component.
   * @param resourceType Component path
   * @return Map (never null)
   */
  private Map<String, String> getComponentHtmlTagAttributes(String resourceType) {
    if (StringUtils.isNotEmpty(resourceType)) {
      Component component = componentManager().getComponent(resourceType);
      if (component != null && component.getHtmlTagAttributes() != null) {
        return component.getHtmlTagAttributes();
      }
    }
    return ImmutableMap.of();
  }

  private ComponentManager componentManager() {
    if (componentManager == null) {
      componentManager = AdaptTo.notNull(this.resolver, ComponentManager.class);
    }
    return componentManager;
  }

  private Item createNewAreaItem() {
    String style = null;
    CssBuilder css = new CssBuilder();
    css.add(NEWAREA_CSS_CLASS_NAME);
    if (generateDefaultCss) {
      style = NEWAREA_STYLE;
      css.add(SECTION_DEFAULT_CLASS_NAME);
    }
    css.add(newAreaCss);
    String newAreaElementName = StringUtils.defaultString(paragraphElementName, DEFAULT_ELEMENT_NAME);
    String newAreaResourceType = getNewAreaResourceType(componentContext.getComponent().getPath());
    return new Item(NEWAREA_RESOURCE_PATH)
        .newArea(true)
        .resourceType(newAreaResourceType)
        .elementName(newAreaElementName)
        .style(style)
        .cssClassName(css.build())
        .decorate(true);
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
  @JsonIgnore
  public List<Item> getItems() {
    return items;
  }

  /**
   * @return Element name for wrapper element
   */
  @JsonIgnore
  public String getWrapperElementName() {
    return StringUtils.defaultString(wrapperElementName, DEFAULT_ELEMENT_NAME);
  }

  /**
   * @return Wrapper element CSS
   */
  @JsonIgnore
  public String getWrapperCss() {
    return this.wrapperCss;
  }

  /**
   * @return True if the wrapper element should be rendered
   */
  @JsonIgnore
  public boolean isWrapperElement() {
    return StringUtils.isNotBlank(wrapperElementName);
  }

  @Override
  public @NotNull String getExportedType() {
    return currentResource.getResourceType();
  }

  @Override
  public @NotNull Map<String, ? extends ComponentExporter> getExportedItems() {
    if (childModels == null) {
      childModels = getChildModels(ComponentExporter.class);
    }
    return childModels;
  }

  @Override
  public String @NotNull [] getExportedItemsOrder() {
    if (exportedItemsOrder == null) {
      Map<String, ? extends ComponentExporter> models = getExportedItems();
      if (!models.isEmpty()) {
        exportedItemsOrder = models.keySet().toArray(ArrayUtils.EMPTY_STRING_ARRAY);
      }
      else {
        exportedItemsOrder = ArrayUtils.EMPTY_STRING_ARRAY;
      }
    }
    return Arrays.copyOf(exportedItemsOrder, exportedItemsOrder.length);
  }

  private <T> Map<String, T> getChildModels(@NotNull Class<T> modelClass) {
    Map<String, T> models = new LinkedHashMap<>();
    for (Resource child : slingModelFilter.filterChildResources(currentResource.getChildren())) {
      T model = modelFactory.getModelFromWrappedRequest(request, child, modelClass);
      if (model != null) {
        models.put(child.getName(), model);
      }
    }
    return models;
  }

  /**
   * Paragraph system item.
   */
  public static final class Item {

    private final String resourcePath;
    private String resourceType;
    private String elementName;
    private String style;
    private String cssClassName;
    private boolean decorate;
    private boolean newArea;
    private boolean valid;

    Item(String resourcePath) {
      this.resourcePath = resourcePath;
    }

    Item resourceType(String value) {
      this.resourceType = value;
      return this;
    }

    Item elementName(String value) {
      this.elementName = value;
      return this;
    }

    Item style(String value) {
      this.style = value;
      return this;
    }

    Item cssClassName(String value) {
      this.cssClassName = value;
      return this;
    }

    Item decorate(boolean value) {
      this.decorate = value;
      return this;
    }

    Item newArea(boolean value) {
      this.newArea = value;
      return this;
    }

    Item valid(boolean value) {
      this.valid = value;
      return this;
    }

    /**
     * @return Resource path
     */
    public String getResourcePath() {
      return resourcePath;
    }

    /**
     * @return Resource type
     */
    public String getResourceType() {
      return resourceType;
    }

    /**
     * @return Name for item element
     */
    public String getElementName() {
      return elementName;
    }

    /**
     * @return Style string
     */
    public String getStyle() {
      return style;
    }

    /**
     * @return CSS classes
     */
    public String getCssClassName() {
      return cssClassName;
    }

    /**
     * @return Render with decoration tag
     */
    public boolean isDecorate() {
      return this.decorate;
    }

    /**
     * @return true if this is the new area
     */
    public boolean isNewArea() {
      return newArea;
    }

    /**
     * @return true if content of this paragraph item is valid.
     *         If not it should be hidded when wcmmode=disabled.
     */
    public boolean isValid() {
      return this.valid;
    }

  }

}
