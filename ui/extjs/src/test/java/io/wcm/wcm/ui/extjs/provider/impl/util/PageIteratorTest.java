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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Iterator;
import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageFilter;
import com.google.common.collect.ImmutableList;

import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
class PageIteratorTest {

  private final AemContext context = new AemContext();

  private Resource resource1;
  private Resource resource2;
  private Resource resource3;
  private Page page1;
  private Page page2;
  private Page page3;
  @Mock
  private PageFilter pageFilter;

  private Iterator<Resource> resources;

  @BeforeEach
  void setUp() {
    page1 = context.create().page("/path1");
    page2 = context.create().page("/path2");
    page3 = context.create().page("/path3");

    resource1 = context.resourceResolver().getResource("/path1");
    resource2 = context.resourceResolver().getResource("/path2");
    resource3 = context.resourceResolver().getResource("/path3");

    resources = ImmutableList.of(resource1, resource2, resource3).iterator();
  }

  @Test
  void testResources() {
    List<Page> result = ImmutableList.copyOf(new PageIterator(resources, null));
    assertEquals(ImmutableList.of(page1, page2, page3), result);
  }

  @Test
  void testWithPageFilter() {
    when(pageFilter.includes(page1)).thenReturn(true);

    List<Page> result = ImmutableList.copyOf(new PageIterator(resources, pageFilter));
    assertEquals(ImmutableList.of(page1), result);
  }

  @Test
  void testWithSlingFolder() {
    resource1 = context.create().resource("/another/path1");
    resource2 = context.create().resource("/another/path2",
        ImmutableValueMap.of(JcrConstants.JCR_PRIMARYTYPE, "sling:Folder"));
    resource3 = context.create().resource("/another/path3",
        ImmutableValueMap.of(JcrConstants.JCR_PRIMARYTYPE, "sling:OrderedFolder"));
    resources = ImmutableList.of(resource1, resource2, resource3).iterator();

    List<Page> result = ImmutableList.copyOf(new PageIterator(resources, null));
    assertEquals(2, result.size());
    assertEquals("/another/path2", result.get(0).getPath());
    assertEquals("/another/path3", result.get(1).getPath());
  }

}
