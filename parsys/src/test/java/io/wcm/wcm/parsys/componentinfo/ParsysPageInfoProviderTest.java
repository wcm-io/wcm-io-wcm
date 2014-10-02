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
import static org.mockito.Mockito.when;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.wcm.parsys.componentinfo.mock.MockComponentManager;

import java.util.List;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;

@RunWith(MockitoJUnitRunner.class)
public class ParsysPageInfoProviderTest {

  @Rule
  public AemContext context = new AemContext();

  @Mock
  private AllowedComponentsProvider allowedComponentsProvider;

  private ParsysPageInfoProvider underTest;

  @Before
  public void setUp() {
    context.currentPage(context.create().page("/conent/sample/page1", "/apps/sample/templates/template1",
        ImmutableValueMap.of(ResourceResolver.PROPERTY_RESOURCE_TYPE, "/apps/sample/components/page1")));

    when(allowedComponentsProvider.getAllowedComponentsForTemplate(anyString(), any(ResourceResolver.class)))
    .thenReturn(ImmutableSortedSet.of(MockComponentManager.COMPONENT_PATH_1, MockComponentManager.COMPONENT_PATH_2));

    context.registerService(AllowedComponentsProvider.class, allowedComponentsProvider);

    context.addModelsForPackage("io.wcm.wcm.parsys.componentinfo.mock");

    underTest = new ParsysPageInfoProvider();
  }

  @Test
  public void testUpdatePageInfo() throws JSONException {
    JSONObject result = new JSONObject();
    underTest.updatePageInfo(context.request(), result, context.currentResource());

    JSONObject components = result.getJSONObject("components");
    List<String> keys = ImmutableList.copyOf(components.keys());
    assertEquals(2, keys.size());
    assertEquals(MockComponentManager.COMPONENT_PATH_1, keys.get(0));
    assertEquals(MockComponentManager.COMPONENT_PATH_2, keys.get(1));
  }

}
