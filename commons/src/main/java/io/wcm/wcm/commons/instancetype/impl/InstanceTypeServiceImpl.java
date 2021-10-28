/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2021 wcm.io
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
package io.wcm.wcm.commons.instancetype.impl;

import static org.osgi.framework.Constants.SERVICE_PID;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.WCMMode;

import io.wcm.wcm.commons.instancetype.InstanceTypeService;
import io.wcm.wcm.commons.util.RunMode;

/**
 * Implements {@link InstanceTypeService}.
 */
@Component(service = InstanceTypeService.class)
@Designate(ocd = InstanceTypeServiceImpl.Config.class)
public class InstanceTypeServiceImpl implements InstanceTypeService {

  @ObjectClassDefinition(name = "wcm.io Commons AEM Instance Type",
      description = "Configures if the current instance is an author or publish instance, and makes this information accessible for other services.")
  @interface Config {

    @AttributeDefinition(name = "Instance Type",
        description = "Should be explicitely configured to 'author' or 'publish'. If not set, instance type will be guessed by heuristics from other OSGi configurations.",
        options = {
            @Option(value = RunMode.AUTHOR, label = "Author"),
            @Option(value = RunMode.PUBLISH, label = "Publish"),
            @Option(value = InstanceTypeServiceImpl.TYPE_AUTO, label = "Detect automatically (not recommended)")
        })
    String instance_type() default InstanceTypeServiceImpl.TYPE_AUTO;

  }

  static final String TYPE_AUTO = "auto";

  static final String WCM_REQUEST_FILTER_PID = "com.day.cq.wcm.core.WCMRequestFilter";
  static final String WCM_MODE_PROPERTY = "wcmfilter.mode";

  private boolean isAuthor;
  private Set<String> runModes;

  @Reference
  private ConfigurationAdmin configAdmin;

  private final Logger log = LoggerFactory.getLogger(InstanceTypeServiceImpl.class);

  @Activate
  private void activate(Config config) {
    // detect instance type
    String instanceType = config.instance_type();
    if (StringUtils.equals(instanceType, RunMode.AUTHOR)) {
      isAuthor = true;
    }
    else if (StringUtils.equals(instanceType, RunMode.PUBLISH)) {
      isAuthor = false;
    }
    else {
      // not configured or set to "auto" - rely on guessing author mode
      isAuthor = detectAutorMode();

      log.warn("Please provide a 'wcm.io Commons AEM Instance Type' configuration "
          + "- falling back to guessing instance type from other configuration => {}.",
          isAuthor ? RunMode.AUTHOR : RunMode.PUBLISH);
    }

    // set matching run mode set
    if (isAuthor) {
      runModes = Collections.singleton(RunMode.AUTHOR);
    }
    else {
      runModes = Collections.singleton(RunMode.PUBLISH);
    }
  }

  private boolean detectAutorMode() {
    try {
      Configuration[] configs = configAdmin.listConfigurations("(" + SERVICE_PID + "=" + WCM_REQUEST_FILTER_PID + ")");
      if (configs != null && configs.length > 0) {
        Object defaultWcmMode = configs[0].getProperties().get(WCM_MODE_PROPERTY);
        if (defaultWcmMode instanceof String) {
          return !StringUtils.equalsIgnoreCase(WCMMode.DISABLED.name(), (String)defaultWcmMode);
        }
      }
    }
    catch (IOException | InvalidSyntaxException ex) {
      log.warn("Unable to read OSGi configuration: {}", WCM_REQUEST_FILTER_PID, ex);
    }
    return false;
  }

  @Override
  public boolean isAuthor() {
    return isAuthor;
  }

  @Override
  public boolean isPublish() {
    return !isAuthor;
  }

  @Override
  public @NotNull Set<String> getRunModes() {
    return Collections.unmodifiableSet(runModes);
  }

}
