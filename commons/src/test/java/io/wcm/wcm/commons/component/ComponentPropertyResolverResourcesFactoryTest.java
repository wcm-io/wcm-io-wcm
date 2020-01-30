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
package io.wcm.wcm.commons.component;

import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.components.ComponentContext;

import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.commons.component.impl.ComponentPropertyResolverFactoryImpl;

@ExtendWith(AemContextExtension.class)
class ComponentPropertyResolverResourcesFactoryTest extends AbstractComponentPropertyResolverResourcesTest {

  private ComponentPropertyResolverFactory componentPropertyResolverFactory;
  private ComponentPropertyResolver componentPropertyResolver;

  @BeforeEach
  void setUp() {
    componentPropertyResolverFactory = context.registerInjectActivateService(new ComponentPropertyResolverFactoryImpl());
  }

  @AfterEach
  void tearDown() {
    if (componentPropertyResolver != null) {
      componentPropertyResolver.close();
    }
  }

  @Override
  ComponentPropertyResolver getComponentPropertyResolver(@NotNull Page page) {
    componentPropertyResolver = componentPropertyResolverFactory.get(page);
    return componentPropertyResolver;
  }

  @Override
  ComponentPropertyResolver getComponentPropertyResolver(@NotNull Resource resource) {
    componentPropertyResolver = componentPropertyResolverFactory.get(resource);
    return componentPropertyResolver;
  }

  @Override
  ComponentPropertyResolver getComponentPropertyResolver(@NotNull Resource resource, boolean ensureResourceType) {
    componentPropertyResolver = componentPropertyResolverFactory.get(resource, ensureResourceType);
    return componentPropertyResolver;
  }

  @Override
  ComponentPropertyResolver getComponentPropertyResolver(@NotNull ComponentContext wcmComponentContext) {
    componentPropertyResolver = componentPropertyResolverFactory.get(wcmComponentContext);
    return componentPropertyResolver;
  }

}
