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
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.components.ComponentContext;

import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class ComponentPropertyResolverDirectTest extends AbstractComponentPropertyResolverTest {

  private ComponentPropertyResolver componentPropertyResolver;

  @AfterEach
  void tearDown() {
    if (componentPropertyResolver != null) {
      componentPropertyResolver.close();
    }
  }

  @Override
  @SuppressWarnings("deprecation")
  ComponentPropertyResolver getComponentPropertyResolver(@NotNull Page page) {
    componentPropertyResolver = new ComponentPropertyResolver(page);
    return componentPropertyResolver;
  }

  @Override
  @SuppressWarnings("deprecation")
  ComponentPropertyResolver getComponentPropertyResolver(@NotNull Resource resource) {
    componentPropertyResolver = new ComponentPropertyResolver(resource);
    return componentPropertyResolver;
  }

  @Override
  @SuppressWarnings("deprecation")
  ComponentPropertyResolver getComponentPropertyResolver(@NotNull Resource resource, boolean ensureResourceType) {
    componentPropertyResolver = new ComponentPropertyResolver(resource, ensureResourceType);
    return componentPropertyResolver;
  }

  @Override
  @SuppressWarnings("deprecation")
  ComponentPropertyResolver getComponentPropertyResolver(@NotNull ComponentContext wcmComponentContext) {
    componentPropertyResolver = new ComponentPropertyResolver(wcmComponentContext);
    return componentPropertyResolver;
  }

}
