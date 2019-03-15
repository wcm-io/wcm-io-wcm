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

import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import io.wcm.wcm.commons.bundleinfo.BundleInfo;
import io.wcm.wcm.commons.bundleinfo.BundleInfoService;

/**
 * Implementation of {@link BundleInfoService}.
 */
@Component(service = BundleInfoService.class)
public class BundleInfoServiceImpl implements BundleInfoService {

  private BundleContext bundleContext;

  @Activate
  void activate(BundleContext bc) {
    this.bundleContext = bc;
  }

  @Override
  @SuppressWarnings("null")
  public @NotNull Collection<BundleInfo> getBundles() {
    return Arrays.stream(bundleContext.getBundles())
        .map(bundle -> (BundleInfo)new BundleInfoImpl(bundle))
        .collect(Collectors.toCollection(() -> new TreeSet<BundleInfo>()));
  }

}
