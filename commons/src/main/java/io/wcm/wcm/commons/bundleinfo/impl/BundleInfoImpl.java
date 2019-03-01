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
package io.wcm.wcm.commons.bundleinfo.impl;

import java.util.Date;
import java.util.Dictionary;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

import io.wcm.wcm.commons.bundleinfo.BundleInfo;
import io.wcm.wcm.commons.bundleinfo.BundleState;

/**
 * Provides meta-information about a installed bundle.
 */
class BundleInfoImpl implements BundleInfo {

  private final String symbolicName;
  private final Bundle bundle;
  private final Dictionary headers;
  private final BundleState state;

  BundleInfoImpl(Bundle bundle) {
    this.symbolicName = bundle.getSymbolicName();
    this.bundle = bundle;
    this.headers = bundle.getHeaders();
    if (isFragment()) {
      this.state = BundleState.FRAGMENT;
    }
    else {
      this.state = BundleState.valueOf(bundle.getState());
    }
  }

  @Override
  public @NotNull Bundle getBundle() {
    return bundle;
  }

  @Override
  public @NotNull String getSymbolicName() {
    return symbolicName;
  }

  @Override
  public @NotNull String getName() {
    return StringUtils.defaultString((String)headers.get(Constants.BUNDLE_NAME), getSymbolicName());
  }

  @Override
  public @NotNull String getVersion() {
    return StringUtils.defaultString((String)headers.get(Constants.BUNDLE_VERSION));
  }

  @Override
  public @NotNull BundleState getState() {
    return state;
  }

  @Override
  public @Nullable Date getLastModified() {
    if (bundle.getLastModified() == 0) {
      return null;
    }
    return new Date(bundle.getLastModified());
  }

  @Override
  public boolean isFragment() {
    String fragmentHost = (String)headers.get(Constants.FRAGMENT_HOST);
    return StringUtils.isNotBlank(fragmentHost);
  }

  @Override
  public String toString() {
    return symbolicName;
  }

  @Override
  public int hashCode() {
    return symbolicName.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof BundleInfoImpl)) {
      return false;
    }
    BundleInfoImpl other = (BundleInfoImpl)obj;
    return symbolicName.equals(other.symbolicName);
  }

  @Override
  public int compareTo(BundleInfo obj) {
    return getSymbolicName().compareTo(obj.getSymbolicName());
  }

}
