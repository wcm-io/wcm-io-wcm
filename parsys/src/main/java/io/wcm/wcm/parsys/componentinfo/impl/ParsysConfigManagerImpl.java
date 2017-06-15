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
package io.wcm.wcm.parsys.componentinfo.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.commons.osgi.Order;
import org.apache.sling.commons.osgi.RankedServices;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import io.wcm.sling.commons.resource.ResourceType;
import io.wcm.wcm.parsys.componentinfo.ParsysConfig;
import io.wcm.wcm.parsys.componentinfo.ParsysConfigManager;

/**
 * Collects paragraph system configurations from repository and OSGi configuration.
 * Apply super resource type based inheritance to both configuration types.
 */
@Component(service = ParsysConfigManager.class, immediate = true, reference = {
    @Reference(service = ParsysConfig.class, name = "parsysConfig",
        bind = "bindParsysConfig", unbind = "unbindParsysConfig",
        cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
})
public final class ParsysConfigManagerImpl implements ParsysConfigManager {

  private final RankedServices<ParsysConfig> osgiParsysConfigs = new RankedServices<>(Order.ASCENDING);

  @Override
  public Iterable<ParsysConfig> getParsysConfigs(String pageComponentPath, ResourceResolver resolver) {
    Resource pageComponentResource = resolver.getResource(pageComponentPath);
    if (pageComponentResource != null) {
      return ImmutableList.copyOf(getParsysConfigsWithInheritance(pageComponentResource, resolver));
    }
    else {
      return ImmutableList.<ParsysConfig>of();
    }
  }

  @Override
  public Iterable<ParsysConfig> getParsysConfigs(final String pageComponentPath, final String relativePath,
      final ResourceResolver resolver) {
    Iterable<ParsysConfig> configs = getParsysConfigs(pageComponentPath, resolver);
    return Iterables.filter(configs, new Predicate<ParsysConfig>() {
      @Override
      public boolean apply(ParsysConfig parsysConfig) {
        // sanity check
        if (parsysConfig == null || parsysConfig.getPathPattern() == null) {
          return false;
        }
        return parsysConfig.getPathPattern().matcher(relativePath).matches();
      }
    });
  }

  private Collection<ParsysConfig> getParsysConfigs(Resource pageComponentResource) {
    List<ParsysConfig> configs = new ArrayList<>();

    // get first jcr parsys configurations for this page component
    ResourceParsysConfigProvider resourceParsysConfigProvider = new ResourceParsysConfigProvider(pageComponentResource);
    configs.addAll(resourceParsysConfigProvider.getPathDefs());

    // add osgi parsys configurations
    for (ParsysConfig osgiParsysConfig : osgiParsysConfigs) {
      if (ResourceType.equals(pageComponentResource.getPath(), osgiParsysConfig.getPageComponentPath(), pageComponentResource.getResourceResolver())) {
        configs.add(osgiParsysConfig);
      }
    }

    return configs;
  }

  private Collection<ParsysConfig> getParsysConfigsWithInheritance(Resource pageComponentResource, ResourceResolver resolver) {
    List<ParsysConfig> configs = new ArrayList<>();

    // get path definitions from this page component
    configs.addAll(getParsysConfigs(pageComponentResource));

    // add path definitions from for super page components
    String resourceSuperType = pageComponentResource.getResourceSuperType();
    if (StringUtils.isNotEmpty(resourceSuperType)) {
      Resource superResource = resolver.getResource(resourceSuperType);
      if (superResource != null) {
        configs.addAll(getParsysConfigsWithInheritance(superResource, resolver));
      }
    }

    return configs;
  }

  void bindParsysConfig(ParsysConfig service, Map<String, Object> props) {
    osgiParsysConfigs.bind(service, props);
  }

  void unbindParsysConfig(ParsysConfig service, Map<String, Object> props) {
    osgiParsysConfigs.unbind(service, props);
  }

}
