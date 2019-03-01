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
package io.wcm.wcm.commons.bundleinfo;

import org.jetbrains.annotations.NotNull;
import org.osgi.annotation.versioning.ProviderType;
import org.osgi.framework.Bundle;

/**
 * Bundle state
 */
@ProviderType
public enum BundleState {

  /**
   * This bundle is uninstalled and may not be used.
   */
  UNINSTALLED(Bundle.UNINSTALLED),

  /**
   * This bundle is installed but not yet resolved.
   */
  INSTALLED(Bundle.INSTALLED),

  /**
   * This bundle is resolved and is able to be started.
   */
  RESOLVED(Bundle.RESOLVED),

  /**
   * This bundle is in the process of starting.
   */
  STARTING(Bundle.STARTING),

  /**
   * This bundle is in the process of stopping.
   */
  STOPPING(Bundle.STOPPING),

  /**
   * This bundle is now running.
   */
  ACTIVE(Bundle.ACTIVE),

  /**
   * Bundle is a fragment bundle
   */
  FRAGMENT(-1),

  /**
   * State is unknown
   */
  UNKNOWN(-2);

  private final int stateValue;

  BundleState(int stateValue) {
    this.stateValue = stateValue;
  }

  /**
   * @return OSGi framework state value
   */
  public int getStateValue() {
    return stateValue;
  }

  /**
   * @param stateValue OSGi framework state value
   * @return Bundle state matching state value - or UNKNOWN if no match found
   */
  public static @NotNull BundleState valueOf(int stateValue) {
    for (BundleState state : BundleState.values()) {
      if (state.stateValue == stateValue) {
        return state;
      }
    }
    return UNKNOWN;
  }

}
