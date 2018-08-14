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
package io.wcm.wcm.commons.util;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.osgi.annotation.versioning.ProviderType;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;

/**
 * Sling run mode utility methods.
 */
@ProviderType
public final class RunMode {

  /**
   * Runmode for author instance
   */
  public static final @NotNull String AUTHOR = "author";

  /**
   * Runmode for publish instance
   */
  public static final @NotNull String PUBLISH = "publish";

  private RunMode() {
    // static methods only
  }

  /**
   * Checks if given run mode is active.
   * @param runModes Run modes for current instance
   * @param expectedRunModes Run mode(s) to check for
   * @return true if any of the given run modes is active
   * @deprecated Instead of directly using the run modes, it is better to make the component in question require a
   *             configuration (see OSGI Declarative Services Spec: configuration policy). In this case, a component
   *             gets only active if a configuration is available. Such a configuration can be put into the repository
   *             for the specific run mode.
   */
  @Deprecated
  public static boolean is(Set<String> runModes, String... expectedRunModes) {
    if (runModes != null && expectedRunModes != null) {
      for (String expectedRunMode : expectedRunModes) {
        if (runModes.contains(expectedRunMode)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Checks if context is running on author instance.
   * @param runModes Run modes
   * @return true if "author" run mode is active
   * @deprecated Instead of directly using the run modes, it is better to make the component in question require a
   *             configuration (see OSGI Declarative Services Spec: configuration policy). In this case, a component
   *             gets only active if a configuration is available. Such a configuration can be put into the repository
   *             for the specific run mode.
   */
  @Deprecated
  public static boolean isAuthor(Set<String> runModes) {
    return RunMode.is(runModes, AUTHOR);
  }

  /**
   * Checks if context is running on publish instance.
   * @param runModes Run modes
   * @return true if "publish" run mode is active
   * @deprecated Instead of directly using the run modes, it is better to make the component in question require a
   *             configuration (see OSGI Declarative Services Spec: configuration policy). In this case, a component
   *             gets only active if a configuration is available. Such a configuration can be put into the repository
   *             for the specific run mode.
   */
  @Deprecated
  public static boolean isPublish(Set<String> runModes) {
    return RunMode.is(runModes, PUBLISH);
  }

  /**
   * Use this to disable a component if none of its run modes are active. Component activation status is logged
   * with DEBUG level.
   * This method is a replacement for the
   * <code>com.day.cq.commons.RunModeUtil#disableIfNoRunModeActive(RunMode, String[], ComponentContext, Logger)</code>
   * method which is deprecated.
   * @param runModes Run modes
   * @param allowedRunModes Allowed run modes
   * @param componentContext OSGI component context
   * @param log Logger
   * @return true if component was disabled
   * @deprecated Instead of directly using the run modes, it is better to make the component in question require a
   *             configuration (see OSGI Declarative Services Spec: configuration policy). In this case, a component
   *             gets only active if a configuration is available. Such a configuration can be put into the repository
   *             for the specific run mode.
   */
  @Deprecated
  public static boolean disableIfNoRunModeActive(Set<String> runModes, String[] allowedRunModes,
      ComponentContext componentContext, Logger log) {

    final String name = (String)componentContext.getProperties().get(ComponentConstants.COMPONENT_NAME);
    boolean result = false;

    boolean isActive = false;
    for (String runMode : allowedRunModes) {
      if (runModes.contains(runMode)) {
        isActive = true;
        break;
      }
    }
    if (!isActive) {
      if (log.isDebugEnabled()) {
        log.debug("Component '" + name + "' "
            + "disabled as none of its run modes (" + StringUtils.join(allowedRunModes, ",") + ") "
            + "are currently active (" + StringUtils.join(runModes, ",") + ")."
            );
      }
      componentContext.disableComponent(name);
      result = true;
    }
    else if (log.isDebugEnabled()) {
      log.debug("Component '" + name + "' "
          + "enabled as at least one of its run modes (" + StringUtils.join(allowedRunModes, ",") + ") "
          + "are currently active (" + StringUtils.join(runModes, ",") + ")."
          );
    }

    return result;
  }

  /**
   * Use this to disable a component if the runmode "author" is not active. Component activation status is logged
   * with DEBUG level.
   * This method is a replacement for the
   * <code>com.day.cq.commons.RunModeUtil#disableIfNoRunModeActive(RunMode, String[], ComponentContext, Logger)</code>
   * method which is deprecated.
   * @param runModes Run modes
   * @param componentContext OSGI component context
   * @param log Logger
   * @return true if component was disabled
   * @deprecated Instead of directly using the run modes, it is better to make the component in question require a
   *             configuration (see OSGI Declarative Services Spec: configuration policy). In this case, a component
   *             gets only active if a configuration is available. Such a configuration can be put into the repository
   *             for the specific run mode.
   */
  @Deprecated
  public static boolean disableIfNotAuthor(Set<String> runModes, ComponentContext componentContext, Logger log) {
    return disableIfNoRunModeActive(runModes, new String[] {
        AUTHOR
    }, componentContext, log);
  }

  /**
   * Use this to disable a component if the runmode "publish" is not active. Component activation status is logged
   * with DEBUG level.
   * This method is a replacement for the
   * <code>com.day.cq.commons.RunModeUtil#disableIfNoRunModeActive(RunMode, String[], ComponentContext, Logger)</code>
   * method which is deprecated.
   * @param runModes Run modes
   * @param componentContext OSGI component context
   * @param log Logger
   * @return true if component was disabled
   * @deprecated Instead of directly using the run modes, it is better to make the component in question require a
   *             configuration (see OSGI Declarative Services Spec: configuration policy). In this case, a component
   *             gets only active if a configuration is available. Such a configuration can be put into the repository
   *             for the specific run mode.
   */
  @Deprecated
  public static boolean disableIfNotPublish(Set<String> runModes, ComponentContext componentContext, Logger log) {
    return disableIfNoRunModeActive(runModes, new String[] {
        PUBLISH
    }, componentContext, log);
  }

}
