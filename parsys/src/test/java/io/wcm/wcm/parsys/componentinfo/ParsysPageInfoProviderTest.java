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
package io.wcm.wcm.parsys.componentinfo;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import io.wcm.testing.mock.aem.junit.AemContext;

import java.util.Collection;
import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.models.annotations.Model;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.day.cq.wcm.api.components.Component;
import com.day.cq.wcm.api.components.ComponentManager;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;

@RunWith(MockitoJUnitRunner.class)
public class ParsysPageInfoProviderTest {

  private static final String COMPONENT_PATH_1 = "/apps/sample/components/comp1";
  private static final String COMPONENT_PATH_2 = "/apps/sample/components/comp2";

  @Rule
  public AemContext context = new AemContext();

  @Mock
  private AllowedComponentsProvider allowedComponentsProvider;

  private ParsysPageInfoProvider underTest;

  @Before
  public void setUp() {
    context.currentPage(context.create().page("/conent/sample/page1", "/apps/sample/templates/template1",
        ImmutableMap.<String, Object>builder()
        .put(ResourceResolver.PROPERTY_RESOURCE_TYPE, "/apps/sample/components/page1")
        .build()));

    when(allowedComponentsProvider.getAllowedComponentsForTemplate(anyString(), any(ResourceResolver.class)))
    .thenReturn(ImmutableSortedSet.of(COMPONENT_PATH_1, COMPONENT_PATH_2));

    context.registerService(AllowedComponentsProvider.class, allowedComponentsProvider);

    context.addModelsForPackage("io.wcm.wcm.parsys.componentinfo");

    underTest = new ParsysPageInfoProvider();
  }

  @Test
  public void testUpdatePageInfo() throws JSONException {
    JSONObject result = new JSONObject();
    underTest.updatePageInfo(context.request(), result, context.currentResource());

    JSONObject components = result.getJSONObject("components");
    List<String> keys = ImmutableList.copyOf(components.keys());
    assertEquals(2, keys.size());
    assertEquals(COMPONENT_PATH_1, keys.get(0));
    assertEquals(COMPONENT_PATH_2, keys.get(1));
  }


  /**
   * Mocked WCM API component manager returning two mocked components.
   */
  @Model(adaptables = ResourceResolver.class, adapters = ComponentManager.class)
  public static class MockComponentManager implements ComponentManager {

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

}
