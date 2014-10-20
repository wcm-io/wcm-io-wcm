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
package io.wcm.wcm.ui.extjs.provider.impl.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import io.wcm.sling.commons.resource.ImmutableValueMap;

import java.util.Iterator;
import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageFilter;
import com.google.common.collect.ImmutableList;

@RunWith(MockitoJUnitRunner.class)
public class PageIteratorTest {

  @Mock
  private Resource resource1;
  @Mock
  private Resource resource2;
  @Mock
  private Resource resource3;
  @Mock
  private Page page1;
  @Mock
  private Page page2;
  @Mock
  private Page page3;
  @Mock
  private PageFilter pageFilter;

  private Iterator<Resource> resources;

  @Before
  public void setUp() {
    when(resource1.getPath()).thenReturn("/path1");
    when(resource2.getPath()).thenReturn("/path2");
    when(resource3.getPath()).thenReturn("/path3");
    when(resource1.getValueMap()).thenReturn(ValueMap.EMPTY);
    when(resource2.getValueMap()).thenReturn(ValueMap.EMPTY);
    when(resource3.getValueMap()).thenReturn(ValueMap.EMPTY);
    when(resource1.adaptTo(Page.class)).thenReturn(page1);
    when(resource2.adaptTo(Page.class)).thenReturn(page2);
    when(resource3.adaptTo(Page.class)).thenReturn(page3);
    when(page1.getPath()).thenReturn("/path1");
    when(page2.getPath()).thenReturn("/path2");
    when(page3.getPath()).thenReturn("/path3");
    resources = ImmutableList.of(resource1, resource2, resource3).iterator();
  }

  @Test
  public void testResources() {
    List<Page> result = ImmutableList.copyOf(new PageIterator(resources, null));
    assertEquals(ImmutableList.of(page1, page2, page3), result);
  }

  @Test
  public void testWithPageFilter() {
    when(pageFilter.includes(page1)).thenReturn(true);

    List<Page> result = ImmutableList.copyOf(new PageIterator(resources, pageFilter));
    assertEquals(ImmutableList.of(page1), result);
  }

  @Test
  public void testWithSlingFolder() {
    when(resource1.adaptTo(Page.class)).thenReturn(null);
    when(resource2.adaptTo(Page.class)).thenReturn(null);
    when(resource3.adaptTo(Page.class)).thenReturn(null);

    when(resource2.getValueMap()).thenReturn(ImmutableValueMap.of(JcrConstants.JCR_PRIMARYTYPE, "sling:Folder"));
    when(resource3.getValueMap()).thenReturn(ImmutableValueMap.of(JcrConstants.JCR_PRIMARYTYPE, "sling:OrderedFolder"));

    List<Page> result = ImmutableList.copyOf(new PageIterator(resources, null));
    assertEquals(2, result.size());
    assertEquals("/path2", result.get(0).getPath());
    assertEquals("/path3", result.get(1).getPath());
  }

}
