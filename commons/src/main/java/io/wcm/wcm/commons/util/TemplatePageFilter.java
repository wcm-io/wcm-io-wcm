/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2018 wcm.io
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

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageFilter;

/**
 * Extension of {@link PageFilter} allowing usage of {@link TemplatePathInfo}
 *
 */
public class TemplatePageFilter extends PageFilter {

  private final Set<String> allowedTemplates;

  /**
   * @param templatePath
   */
  public TemplatePageFilter(TemplatePathInfo... templatePath) {
    this(false, false, templatePath);
  }

  /**
   * @param includeInvalid if <code>true</code> invalid pages are included.
   * @param includeHidden if <code>true</code> hidden pages are included.
   * @param templatePaths The resource types to be included
   */
  public TemplatePageFilter(boolean includeInvalid, boolean includeHidden, TemplatePathInfo... templatePaths) {
    super(includeInvalid, includeHidden);
    allowedTemplates = Arrays.stream(templatePaths).map(TemplatePathInfo::getTemplatePath).collect(Collectors.toSet());
  }

  @Override
  public boolean includes(Page page) {
    return super.includes(page)
        && allowedTemplates.contains(page.getProperties().get(NameConstants.PN_TEMPLATE, String.class));
  }

}
