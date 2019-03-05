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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.osgi.annotation.versioning.ProviderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.components.Component;
import com.day.cq.wcm.api.components.ComponentManager;

import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.sling.models.annotations.AemObject;
import io.wcm.wcm.commons.bundleinfo.BundleInfo;
import io.wcm.wcm.commons.bundleinfo.BundleInfoService;

/**
 * Provides access to a list of OSGi bundles present in the system.
 * The list can be filtered by a regex on the symbolic name, either defined as
 * property on the page component or in the page properties.
 */
@Model(adaptables = SlingHttpServletRequest.class)
@ProviderType
public final class VersionInfo {

  /**
   * Property name to define the regex for the symbolic name.
   */
  public static final String PN_FILTER_REGEX = "wcmio:versionInfoBundleSymbolicNameRegex";

  @OSGiService
  private BundleInfoService bundleInfoService;
  @AemObject
  private Page currentPage;
  @SlingObject
  private ResourceResolver resourceResolver;

  private Collection<BundleInfo> bundles;

  private static final Logger log = LoggerFactory.getLogger(VersionInfo.class);

  @PostConstruct
  private void activate() {
    List<Pattern> filterPatterns = toPattern(getFilterRegex());
    this.bundles = getFilteredBundles(filterPatterns);
  }

  public Collection<BundleInfo> getBundles() {
    return this.bundles;
  }

  private Collection<BundleInfo> getFilteredBundles(List<Pattern> filterPatterns) {
    Collection<BundleInfo> allBundles = bundleInfoService.getBundles();
    return allBundles.stream()
        .filter(bundle -> matchesFilterPatterns(bundle, filterPatterns))
        .collect(Collectors.toList());
  }

  private boolean matchesFilterPatterns(BundleInfo bundle, List<Pattern> filterPatterns) {
    if (filterPatterns.isEmpty()) {
      return true;
    }
    return filterPatterns.stream()
        .filter(pattern -> pattern.matcher(bundle.getSymbolicName()).matches())
        .findAny().isPresent();
  }

  private Stream<String> getFilterRegex() {
    // try to read from page properties
    String[] regex = currentPage.getProperties().get(PN_FILTER_REGEX, String[].class);
    if (regex == null) {
      // alternatively read from page component property
      @SuppressWarnings("null")
      ComponentManager componentManager = AdaptTo.notNull(resourceResolver, ComponentManager.class);
      Component pageComponent = componentManager.getComponentOfResource(currentPage.getContentResource());
      if (pageComponent != null) {
        regex = pageComponent.getProperties().get(PN_FILTER_REGEX, String[].class);
      }
    }
    if (regex != null) {
      return Arrays.stream(regex);
    }
    else {
      return Stream.empty();
    }
  }

  private List<Pattern> toPattern(Stream<String> regex) {
    return regex
        .map(regExString -> {
          try {
            return Pattern.compile(regExString);
          }
          catch (PatternSyntaxException ex) {
            log.warn("Invalid pattern for version info filtering: " + regex + " in " + currentPage.getPath(), ex);
            return null;
          }
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

}
