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
package io.wcm.wcm.commons.instancetype;

import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Allows to detect if the current AEM instance is an author or publish instance.
 * <p>
 * This service does not rely in <code>SlingSettingServices</code> which is deprecated and subject to removal in latest
 * AEM versions.
 * Instead, it is based on a OSGi configuration which should be configured properly for author and publish instances.
 * If not configured, it "guesses" the instance type from other OSGi configs and writes a warning in the logs.
 * </p>
 */
@ProviderType
public interface InstanceTypeService {

  /**
   * Returns true if code is running on AEM author instance.
   * @return true if AEM author instance.
   */
  boolean isAuthor();

  /**
   * Returns true if code is running on AEM publish instance.
   * @return true if AEM publish instance.
   */
  boolean isPublish();

  /**
   * Returns a set with a single "author" or "publish" run mode string.
   * This method is provided for for compatibility with code relying on sets of run modes formerly provided by
   * <code>SlingSettingsService</code>.
   * @return Set with a single element: Either "author" or "publish".
   */
  @NotNull
  Set<String> getRunModes();

}
