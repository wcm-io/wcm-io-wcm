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
package io.wcm.wcm.commons.filter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.osgi.annotation.versioning.ProviderType;

import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageFilter;

import io.wcm.wcm.commons.util.TemplatePathInfo;

/**
 * Extension of {@link PageFilter} allowing usage of {@link TemplatePathInfo}.
 */
@ProviderType
public final class TemplatePageFilter extends PageFilter {

  private final Set<String> allowedTemplatePaths;

  /**
   * @param templates The templates to be included
   */
  public TemplatePageFilter(@NotNull TemplatePathInfo @NotNull... templates) {
    this(false, false, templates);
  }

  /**
   * @param includeInvalid if <code>true</code> invalid pages are included.
   * @param includeHidden if <code>true</code> hidden pages are included.
   * @param templates The templates to be included
   */
  @SuppressWarnings("null")
  public TemplatePageFilter(boolean includeInvalid, boolean includeHidden, @NotNull TemplatePathInfo @NotNull... templates) {
    super(includeInvalid, includeHidden);
    allowedTemplatePaths = Arrays.stream(templates)
        .map(TemplatePathInfo::getTemplatePath)
        .collect(Collectors.toSet());
  }

  @Override
  public boolean includes(Page page) {
    return super.includes(page)
        && allowedTemplatePaths.contains(page.getProperties().get(NameConstants.PN_TEMPLATE, String.class));
  }

}
