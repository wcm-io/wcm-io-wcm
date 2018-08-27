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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.WCMException;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("null")
public class SlingFolderVirtualPageTest {

  @Mock
  private Resource resource;
  @Mock
  private ResourceResolver resourceResolver;
  @Mock
  private Resource contentResource;

  private SlingFolderVirtualPage underTest;

  @Before
  public void setUp() {
    when(resource.getResourceResolver()).thenReturn(resourceResolver);
    underTest = new SlingFolderVirtualPage(resource);
  }

  @Test
  public void testAdaptTo() {
    assertSame(resource, underTest.adaptTo(Resource.class));
    assertNull(underTest.adaptTo(Page.class));
  }

  @Test
  public void testNullProperties() {
    assertNull(underTest.getDescription());
    assertFalse(underTest.canUnlock());
    assertNull(underTest.getContentResource());
    assertNull(underTest.getContentResource(null));
    assertNull(underTest.getLanguage(false));
    assertNull(underTest.getLastModified());
    assertNull(underTest.getLastModifiedBy());
    assertNull(underTest.getLockOwner());
    assertNull(underTest.getNavigationTitle());
    assertNull(underTest.getOffTime());
    assertNull(underTest.getOnTime());
    assertNull(underTest.getPageTitle());
    assertTrue(underTest.getProperties().isEmpty());
    assertNull(underTest.getProperties(null));
    assertEquals(0, underTest.getTags().length);
    assertNull(underTest.getTemplate());
    assertNull(underTest.getTitle());
    assertNull(underTest.getVanityUrl());
    assertFalse(underTest.hasChild(null));
    assertFalse(underTest.hasContent());
    assertFalse(underTest.isHideInNav());
    assertFalse(underTest.isLocked());
    assertFalse(underTest.isValid());
    assertEquals(0L, underTest.timeUntilValid());
  }

  @Test
  public void testGetAbsoluteParent() {
    when(resource.getPath()).thenReturn("/path1/path2/path3");
    Resource parentResource = mock(Resource.class);
    when(resourceResolver.getResource("/path1/path2")).thenReturn(parentResource);
    Page page = mock(Page.class);
    when(parentResource.adaptTo(Page.class)).thenReturn(page);
    assertSame(page, underTest.getAbsoluteParent(2));
  }

  @Test
  public void testDepth() {
    when(resource.getPath()).thenReturn("/path1/path2/path3");
    assertEquals(3, underTest.getDepth());
  }

  @Test
  public void testGetName() {
    when(resource.getName()).thenReturn("name1");
    assertEquals("name1", underTest.getName());
  }

  @Test
  public void testGetPageManager() {
    PageManager pageManager = mock(PageManager.class);
    when(resourceResolver.adaptTo(PageManager.class)).thenReturn(pageManager);
    assertSame(pageManager, underTest.getPageManager());
  }

  @Test
  public void testGetParent() {
    Resource parentResource = mock(Resource.class);
    Page parentPage = mock(Page.class);
    when(parentResource.adaptTo(Page.class)).thenReturn(parentPage);
    when(resource.getParent()).thenReturn(parentResource);
    assertSame(parentPage, underTest.getParent());
  }

  @Test
  public void testGetParentLevel() {
    when(resource.getPath()).thenReturn("/path1/path2/path3");
    Resource parentResource = mock(Resource.class);
    Page parentPage = mock(Page.class);
    when(parentResource.adaptTo(Page.class)).thenReturn(parentPage);
    when(resourceResolver.getResource("/path1")).thenReturn(parentResource);
    assertSame(parentPage, underTest.getParent(2));
  }

  @Test
  public void testGetPath() {
    when(resource.getPath()).thenReturn("/path1");
    assertEquals("/path1", underTest.getPath());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testLock() throws WCMException {
    underTest.lock();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testUnlock() throws WCMException {
    underTest.unlock();
  }

}
