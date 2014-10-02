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
package io.wcm.wcm.parsys.componentinfo;

import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.wcm.parsys.componentinfo.impl.ResourceTypeUtil;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.commons.json.io.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageInfoProvider;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.components.Component;
import com.day.cq.wcm.api.components.ComponentEditConfig;
import com.day.cq.wcm.api.components.ComponentManager;
import com.day.cq.wcm.api.components.VirtualComponent;
import com.day.text.Text;

/**
 * Component list page info provider for wcm.io paragraph system.
 * This is only required for the Classic UI.
 * <p>
 * Example for page info provider and paragraph system configuration in page component definition:
 * </p>
 *
 * <pre>
 * "cq:infoProviders": {
 *   "complist": {
 *     "className": "io.wcm.wcm.parsys.componentinfo.ParsysPageInfoProvider"
 *   }
 * }
 * </pre>
 */
public final class ParsysPageInfoProvider implements PageInfoProvider {

  private static final Logger log = LoggerFactory.getLogger(ParsysPageInfoProvider.class);

  static final String ICON = "icon";
  static final String THUMBNAIL = "thumbnail";
  static final String DESCRIPTION = "description";
  static final String RESOURCE_TYPE = "resourceType";
  static final String DIV_ATTRIBUTES = "divAttributes";
  static final String TITLE = "title";
  static final String GROUP = "group";
  static final String PATH = "path";
  static final String VIRTUAL = "virtual";
  static final String DIALOG = "dialog";
  static final String CONFIG = "config";
  static final String STYLE_COMPONENTS = "components";
  static final String GROUP_PREFIX = "group:";

  @Override
  public void updatePageInfo(SlingHttpServletRequest request, JSONObject info, Resource resource) throws JSONException {

    long startTime = 0;
    if (log.isDebugEnabled()) {
      startTime = System.currentTimeMillis();
    }

    JSONObject components = new JSONObject();

    // check if resource is a CQ page
    try {
      ResourceResolver resolver = request.getResourceResolver();
      PageManager pageManager = AdaptTo.notNull(request.getResourceResolver(), PageManager.class);
      Page page = pageManager.getContainingPage(resource);
      if (page != null) {

        // get context objects
        ComponentManager componentManager = resolver.adaptTo(ComponentManager.class);
        String contextPrefix = StringUtils.defaultString(request.getContextPath());

        // get allowed component definition from template/page component
        String pageComponentPath = page.getContentResource().getResourceType();
        SlingBindings bindings = (SlingBindings)request.getAttribute(SlingBindings.class.getName());
        SlingScriptHelper slingScriptHelper = bindings.getSling();
        AllowedComponentsProvider provider = slingScriptHelper.getService(AllowedComponentsProvider.class);
        Set<String> allowedComponents = provider.getAllowedComponentsForTemplate(pageComponentPath, resolver);

        // write components and allowed components
        writeComponents(components, componentManager, allowedComponents, contextPrefix);

      }
    }
    catch (Throwable ex) {
      log.error("Error getting parsys component info for " + resource.getPath(), ex);
    }

    info.put("components", components);

    // output profiling info in DEBUG mode
    if (log.isDebugEnabled()) {
      long endTime = System.currentTimeMillis();
      long duration = endTime - startTime;
      log.debug("updatePageInfo for " + resource.getPath() + " took " + duration + "ms");
    }
  }

  /**
   * Generates a JSON export of component descriptions suitable for the new par and component lists.
   * @param components components list JSON object
   * @param componentManager component manager
   * @param allowedComponents set of allowed components
   * @param contextPrefix webapp content path
   * @return mapping from allowed strings to component keys
   * @throws JSONException if an JSON error occurs.
   */
  private Map<String, Set<String>> writeComponents(JSONObject components, ComponentManager componentManager,
      Set<String> allowedComponents, String contextPrefix) throws JSONException {

    // use a tree map for the component list and order them by path.
    // this ensures that for 2 components with the same resource type,
    // the /apps one comes first and the /libs one is not transferred.
    SortedMap<String, Component> allComponents = new TreeMap<>();
    for (Component component : componentManager.getComponents()) {
      if (component != null && component.isEditable()
          && (allowedComponents.contains(ResourceTypeUtil.makeAbsolute(component.getPath()))
              || allowedComponents.contains(GROUP_PREFIX + component.getComponentGroup()))) {
        boolean displayVirtualsOnly = component.getProperties().get("displayVirtualsOnly", false);
        if (!displayVirtualsOnly) {
          allComponents.put(component.getPath(), component);
        }
        // add virtual ones
        for (VirtualComponent v : component.getVirtualComponents()) {
          allComponents.put(v.getPath(), v);
        }
      }
    }

    // mapping from allowed strings to set of json keys.
    Map<String, Set<String>> allowMap = new HashMap<>();

    // set to filter out overlaid components
    Set<String> resourceTypes = new HashSet<>();
    for (Component component : allComponents.values()) {
      JSONObject componentDef = new JSONObject();
      String key;
      if (component instanceof VirtualComponent) {
        key = component.getPath();
        // update allowed set
        String componentPath = ((VirtualComponent)component).getComponent().getPath();
        if (allowedComponents.contains(componentPath)) {
          Set<String> keys = allowMap.get(componentPath);
          if (keys == null) {
            keys = new HashSet<String>();
            allowMap.put(component.getPath(), keys);
          }
          keys.add(key);
        }
      }
      else {
        if (resourceTypes.contains(component.getResourceType())) {
          continue;
        }
        key = component.getResourceType();
        resourceTypes.add(key);
        // update allowed set
        if (allowedComponents.contains(component.getPath())) {
          Set<String> keys = allowMap.get(component.getPath());
          if (keys == null) {
            keys = new HashSet<>();
            allowMap.put(component.getPath(), keys);
          }
          keys.add(key);
        }
      }
      if (allowedComponents.contains(GROUP_PREFIX + component.getComponentGroup())) {
        Set<String> keys = allowMap.get(GROUP_PREFIX + component.getComponentGroup());
        if (keys == null) {
          keys = new HashSet<String>();
          allowMap.put(GROUP_PREFIX + component.getComponentGroup(), keys);
        }
        keys.add(key);
      }

      componentDef.put(PATH, component.getPath());
      String title = component.getTitle();
      if (title == null) {
        title = Text.getName(component.getPath());
      }
      if (component.getComponentGroup() != null) {
        componentDef.put(GROUP, component.getComponentGroup());
      }
      componentDef.put(VIRTUAL, component instanceof VirtualComponent);
      componentDef.put(TITLE, title);
      componentDef.put(RESOURCE_TYPE, component.getResourceType());
      if (component.getDescription() != null) {
        componentDef.put(DESCRIPTION, component.getDescription());
      }
      if (component.getThumbnailPath() != null) {
        componentDef.put(THUMBNAIL, contextPrefix + component.getThumbnailPath());
      }
      if (component.getIconPath() != null) {
        componentDef.put(ICON, contextPrefix + component.getIconPath());
      }
      // include editing
      ComponentEditConfig ed = component.getEditConfig();
      if (ed != null && !ed.isDefault()) {
        StringWriter sw = new StringWriter();
        JSONWriter writer = new JSONWriter(sw);
        ed.write(writer);
        JSONObject editConfig = new JSONObject(sw.toString());
        componentDef.put(CONFIG, editConfig);
      }
      String dialogPath = component.getDialogPath();
      if (dialogPath != null) {
        if (!dialogPath.endsWith(".json")) {
          dialogPath += ".infinity.json";
        }
        componentDef.put(DIALOG, dialogPath);
      }

      components.put(key, componentDef);
    }

    return allowMap;
  }

}
