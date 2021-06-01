/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2020 wcm.io
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
package io.wcm.wcm.commons.component.impl;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.components.ComponentContext;

import io.wcm.wcm.commons.component.ComponentPropertyResolver;
import io.wcm.wcm.commons.component.ComponentPropertyResolverFactory;

/**
 * Implementation of {@link ComponentPropertyResolverFactory}.
 */
@Component(service = ComponentPropertyResolverFactory.class, immediate = true)
public class ComponentPropertyResolverFactoryImpl implements ComponentPropertyResolverFactory {

  @Reference
  private ResourceResolverFactory resourceResolverFactory;

  @Override
  public @NotNull ComponentPropertyResolver get(@NotNull Page page) {
    return new ComponentPropertyResolver(page, resourceResolverFactory);
  }

  @Override
  public @NotNull ComponentPropertyResolver get(@NotNull Resource resource) {
    return new ComponentPropertyResolver(resource, resourceResolverFactory);
  }

  @Override
  public @NotNull ComponentPropertyResolver get(@NotNull Resource resource, boolean ensureResourceType) {
    return new ComponentPropertyResolver(resource, ensureResourceType, resourceResolverFactory);
  }

  @Override
  public @NotNull ComponentPropertyResolver get(@NotNull ComponentContext wcmComponentContext) {
    return new ComponentPropertyResolver(wcmComponentContext, resourceResolverFactory);
  }

}
