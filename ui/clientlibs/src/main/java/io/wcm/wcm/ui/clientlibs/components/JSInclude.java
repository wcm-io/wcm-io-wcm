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
package io.wcm.wcm.ui.clientlibs.components;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.RequestAttribute;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.xss.XSSAPI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;

import com.adobe.granite.ui.clientlibs.HtmlLibraryManager;
import com.adobe.granite.ui.clientlibs.LibraryType;

/**
 * Include JavaScript client libraries with optional attributes for script tag.
 */
@Model(adaptables = SlingHttpServletRequest.class)
@ProviderType
public class JSInclude {

  private static final Set CROSSORIGIN_ALLOWED_VALUES = new HashSet<>(Arrays.asList(
      "anonymous", "use-credentials"));
  private static final Set REFERRERPOLICY_ALLOWED_VALUES = new HashSet<>(Arrays.asList(
      "no-referrer", "no-referrer-when-downgrade", "origin", "origin-when-cross-origin",
      "same-origin", "strict-origin", "strict-origin-when-cross-origin", "unsafe-url"));
  private static final Set TYPE_ALLOWED_VALUES = new HashSet<>(Arrays.asList(
      "text/javascript", "module"));

  @SlingObject
  private SlingHttpServletRequest request;
  @OSGiService
  private HtmlLibraryManager htmlLibraryManager;
  @OSGiService
  private XSSAPI xssApi;

  @RequestAttribute(injectionStrategy = InjectionStrategy.OPTIONAL)
  private Object categories;
  @RequestAttribute(injectionStrategy = InjectionStrategy.OPTIONAL)
  private boolean async;
  @RequestAttribute(injectionStrategy = InjectionStrategy.OPTIONAL)
  private String crossorigin;
  @RequestAttribute(injectionStrategy = InjectionStrategy.OPTIONAL)
  private boolean defer;
  @RequestAttribute(injectionStrategy = InjectionStrategy.OPTIONAL)
  private String integrity;
  @RequestAttribute(injectionStrategy = InjectionStrategy.OPTIONAL)
  private boolean nomodule;
  @RequestAttribute(injectionStrategy = InjectionStrategy.OPTIONAL)
  private String nonce;
  @RequestAttribute(injectionStrategy = InjectionStrategy.OPTIONAL)
  private String referrerpolicy;
  @RequestAttribute(injectionStrategy = InjectionStrategy.OPTIONAL)
  private String type;

  private String include;

  @PostConstruct
  private void activate() {
    // build include string
    String[] categoryArray = IncludeUtil.toCategoryArray(categories);
    if (categoryArray != null) {
      List<String> libraryPaths = IncludeUtil.getLibraryUrls(htmlLibraryManager, request.getResourceResolver(),
          categoryArray, LibraryType.JS);
      if (!libraryPaths.isEmpty()) {
        Map<String, String> attrs = validateAndBuildAttributes();
        this.include = buildIncludeString(libraryPaths, attrs);
      }
    }
  }

  /**
   * Validate attribute values from HTL script, escape them properly and build a map with all attributes
   * for the resulting script tag(s).
   * @return Map with attribute for script tag
   */
  private @NotNull Map<String, String> validateAndBuildAttributes() {
    Map<String, String> attrs = new TreeMap<>();
    if (async) {
      attrs.put("async", "true");
    }
    if (CROSSORIGIN_ALLOWED_VALUES.contains(crossorigin)) {
      attrs.put("crossorigin", crossorigin);
    }
    if (defer) {
      attrs.put("defer", "true");
    }
    if (StringUtils.isNotEmpty(integrity)) {
      attrs.put("integrity", xssApi.encodeForHTMLAttr(integrity));
    }
    if (nomodule) {
      attrs.put("nomodule", null);
    }
    if (StringUtils.isNotEmpty(nonce)) {
      attrs.put("nonce", xssApi.encodeForHTMLAttr(nonce));
    }
    if (REFERRERPOLICY_ALLOWED_VALUES.contains(referrerpolicy)) {
      attrs.put("referrerpolicy", referrerpolicy);
    }
    if (TYPE_ALLOWED_VALUES.contains(type)) {
      attrs.put("type", type);
    }
    return attrs;
  }

  /**
   * Build script tags for all client libraries with the defined custom script tag attributes set.
   * @param libraryPaths Library paths
   * @param attrs HTML attributes for script tag
   * @return HTML markup with script tags
   */
  private @NotNull String buildIncludeString(@NotNull List<String> libraryPaths, @NotNull Map<String, String> attrs) {
    StringBuilder markup = new StringBuilder();
    for (String libraryPath : libraryPaths) {
      markup.append("<script src=\"" + xssApi.encodeForHTMLAttr(libraryPath) + "\"");
      for (Map.Entry<String, String> attr : attrs.entrySet()) {
        markup.append(" ");
        markup.append(attr.getKey());
        if (attr.getValue() != null) {
          markup.append("=\"");
          markup.append(attr.getValue());
          markup.append("\"");
        }
      }
      markup.append("></script>\n");
    }
    return markup.toString();
  }

  /**
   * @return HTML markup with script tags
   */
  public @Nullable String getInclude() {
    return include;
  }

}
