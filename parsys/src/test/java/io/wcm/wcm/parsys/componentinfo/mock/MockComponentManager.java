/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
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
package io.wcm.wcm.parsys.componentinfo.mock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Model;

import com.day.cq.wcm.api.components.Component;
import com.day.cq.wcm.api.components.ComponentManager;
import com.google.common.collect.ImmutableList;

/**
 * Mocked WCM API component manager returning two mocked components.
 */
@Model(adaptables = ResourceResolver.class, adapters = ComponentManager.class)
public class MockComponentManager implements ComponentManager {

  public static final String COMPONENT_PATH_1 = "sample/components/comp1";
  public static final String COMPONENT_PATH_2 = "sample/components/comp2";

  @Override
  public Component getComponentOfResource(Resource resource) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Component getComponent(String path) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Collection<Component> getComponents() {
    return ImmutableList.of(
        mockComponent(COMPONENT_PATH_1),
        mockComponent(COMPONENT_PATH_2)
        );
  }

  private Component mockComponent(String path) {
    Component component = mock(Component.class);
    when(component.getPath()).thenReturn(path);
    when(component.getResourceType()).thenReturn(path);
    when(component.isEditable()).thenReturn(true);
    when(component.getProperties()).thenReturn(ValueMap.EMPTY);
    return component;
  }

}
