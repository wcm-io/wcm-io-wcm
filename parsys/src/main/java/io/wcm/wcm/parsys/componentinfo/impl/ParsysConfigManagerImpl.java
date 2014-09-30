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

import io.wcm.sling.commons.osgi.RankedServices;
import io.wcm.wcm.parsys.componentinfo.ParsysConfig;
import io.wcm.wcm.parsys.componentinfo.ParsysConfigManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

/**
 * ParSys configuration manager
 * TODO: add unit tests
 */
@Component(immediate = true, metatype = false)
@Service(ParsysConfigManager.class)
public final class ParsysConfigManagerImpl implements ParsysConfigManager {

  @Reference(name = "parsysConfig", referenceInterface = ParsysConfig.class,
      cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, policy = ReferencePolicy.DYNAMIC)
  private final RankedServices<ParsysConfig> osgiParsysConfigs = new RankedServices<>();

  @Override
  public List<ParsysConfig> getParSysConfigs(String pageComponentPath, ResourceResolver resolver) {
    List<ParsysConfig> configs = new ArrayList<>();

    // get first jcr parsys configurations for this page component
    Resource pageComponentResource = resolver.getResource(pageComponentPath);
    if (pageComponentResource != null) {
      ResourceParsysConfigProvider resourceParsysConfigProvider = new ResourceParsysConfigProvider(pageComponentResource);
      configs.addAll(resourceParsysConfigProvider.getAllPathDefs());
    }

    // add osgi parsys configurations
    for (ParsysConfig osgiParsysConfig : osgiParsysConfigs) {
      if (StringUtils.equals(pageComponentPath, osgiParsysConfig.getPageComponentPath())) {
        configs.add(osgiParsysConfig);
      }
    }

    return configs;
  }

  @Override
  public List<ParsysConfig> getParSysConfigs(String pageComponentPath, String relativeResourcePath, ResourceResolver resolver) {
    List<ParsysConfig> configs = new ArrayList<>();

    for (ParsysConfig parSysConfig : getParSysConfigs(pageComponentPath, resolver)) {
      if (matches(parSysConfig, relativeResourcePath)) {
        configs.add(parSysConfig);
      }
    }

    return configs;
  }

  private boolean matches(ParsysConfig parSysConfig, String resourcePath) {
    // sanity check
    if (parSysConfig == null || parSysConfig.getPattern() == null || StringUtils.isEmpty(resourcePath)) {
      return false;
    }
    return parSysConfig.getPattern().matcher(resourcePath).matches();
  }

  void bindParsysConfig(ParsysConfig service, Map<String, Object> props) {
    osgiParsysConfigs.bind(service, props);
  }

  void unbindParsysConfig(ParsysConfig service, Map<String, Object> props) {
    osgiParsysConfigs.unbind(service, props);
  }

}
