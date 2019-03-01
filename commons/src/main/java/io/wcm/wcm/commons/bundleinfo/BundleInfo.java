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

import java.util.Date;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;
import org.osgi.framework.Bundle;

/**
 * Provides meta-information about an bundle in the system.
 */
@ProviderType
public interface BundleInfo extends Comparable<BundleInfo> {

  /**
   * @return OSGI bundle
   */
  @NotNull
  Bundle getBundle();

  /**
   * @return Bundle symbolic name
   */
  @NotNull
  String getSymbolicName();

  /**
   * @return Bundle name (fallback to symbolic name if no name default)
   */
  @NotNull
  String getName();

  /**
   * @return Version string
   */
  @NotNull
  String getVersion();

  /**
   * @return Bundle state
   */
  @NotNull
  BundleState getState();

  /**
   * @return Last modified date
   */
  @Nullable
  Date getLastModified();

  /**
   * @return true if bundle is a fragment bundle
   */
  boolean isFragment();

}
