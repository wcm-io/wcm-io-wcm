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
package io.wcm.wcm.ui.granite.pathfield.impl.predicate;

import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.collections.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;

import com.google.common.collect.ImmutableSet;

/**
 * Hide certain "AEM-internal" content paths when listing resource children.
 */
public class HideInternalContentPathsPredicate implements Predicate {

  /**
   * List of paths that are hidden by default.
   * Please note: it's allowed to see them if they are selected as root path, but not visible as children.
   */
  private static final Set<String> HIDE_CONTENT_PATHS = ImmutableSet.<String>builder()
      .add("/content/catalogs")
      .add("/content/cq:tags")
      .add("/content/campaigns")
      .add("/content/communities")
      .add("/content/community")
      .add("/content/community-components")
      .add("/content/community-templates")
      .add("/content/dam")
      .add("/content/dam/catalogs")
      .add("/content/dam/collections")
      .add("/content/dam/formsanddocuments")
      .add("/content/dam/formsanddocuments-themes")
      .add("/content/dam/templates")
      .add("/content/entities")
      .add("/content/experience-fragments")
      .add("/content/forms")
      .add("/content/launches")
      .add("/content/mac")
      .add("/content/mobileapps")
      .add("/content/publications")
      .add("/content/usergenerated")
      .add("/content/phonegap")
      .add("/content/projects")
      .add("/content/screens")
      .add("/content/sites")
      .add("/content/versionhistory")
      .build();

  private static final Pattern FIRST_LEVEL_PATH = Pattern.compile("^/[^/]+$");
  private static final String CONTENT_ROOT_PATH = "/content";

  @Override
  public boolean evaluate(Object object) {
    // if resource is a resource on the first level (below root node), allow only /content
    String path = ((Resource)object).getPath();
    if (FIRST_LEVEL_PATH.matcher(path).matches()) {
      return StringUtils.equals(path, CONTENT_ROOT_PATH);
    }
    // allow all other paths except those from the list above
    return !HIDE_CONTENT_PATHS.contains(path);
  }

}
