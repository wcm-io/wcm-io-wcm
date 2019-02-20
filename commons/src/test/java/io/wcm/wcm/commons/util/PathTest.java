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
package io.wcm.wcm.commons.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.tenant.Tenant;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.day.cq.wcm.api.Page;
import com.day.jcr.vault.util.Text;

import io.wcm.testing.mock.aem.junit.AemContext;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("null")
public class PathTest {

  @Rule
  public AemContext context = new AemContext();

  @Mock
  private Tenant tenant;

  private ResourceResolver resolver;

  @Before
  public void setUp() {
    resolver = context.resourceResolver();
    context.create().page("/content");
    context.create().page("/content/versionhistory");
    context.create().page("/content/versionhistory/user1");
    context.create().page("/content/versionhistory/tenant1/user1");
    context.create().page("/content/launches");
  }

  @Test
  public void testGetAbsoluteLevel() {
    assertEquals("/content", Text.getAbsoluteParent("/content/a/b/c", Path.getAbsoluteLevel("/content", resolver)));
    assertEquals("/content/a", Text.getAbsoluteParent("/content/a/b/c", Path.getAbsoluteLevel("/content/a", resolver)));
    assertEquals("/content/a/b", Text.getAbsoluteParent("/content/a/b/c", Path.getAbsoluteLevel("/content/a/b", resolver)));

    assertEquals(2, Path.getAbsoluteLevel("/content/a/b", resolver));
    assertEquals(1, Path.getAbsoluteLevel("/content/a", resolver));
    assertEquals(0, Path.getAbsoluteLevel("/content", resolver));
    assertEquals(-1, Path.getAbsoluteLevel("/", resolver));
    assertEquals(-1, Path.getAbsoluteLevel("", resolver));
    assertEquals(-1, Path.getAbsoluteLevel(null, resolver));
  }

  @Test
  public void testGetAbsoluteLevel_VersionHistory() {
    assertEquals(2, Path.getAbsoluteLevel("/content/versionhistory/user1/a/b", resolver));
    assertEquals(1, Path.getAbsoluteLevel("/content/versionhistory/user1/a", resolver));
    assertEquals(0, Path.getAbsoluteLevel("/content/versionhistory/user1", resolver));
  }

  @Test
  public void testGetAbsoluteLevel_VersionHistory_Tenant() {
    context.registerAdapter(ResourceResolver.class, Tenant.class, tenant);
    assertEquals(2, Path.getAbsoluteLevel("/content/versionhistory/tenant1/user1/a/b", resolver));
    assertEquals(1, Path.getAbsoluteLevel("/content/versionhistory/tenant1/user1/a", resolver));
    assertEquals(0, Path.getAbsoluteLevel("/content/versionhistory/tenant1/user1", resolver));
  }

  @Test
  public void testGetAbsoluteLevel_Launches() {
    assertEquals(2, Path.getAbsoluteLevel("/content/launches/2018/01/01/launch1/content/a/b", resolver));
    assertEquals(1, Path.getAbsoluteLevel("/content/launches/2018/01/01/launch1/content/a", resolver));
    assertEquals(0, Path.getAbsoluteLevel("/content/launches/2018/01/01/launch1/content", resolver));
    assertEquals(-1, Path.getAbsoluteLevel("/content/launches/2018/01/01/launch1", resolver));
  }

  @Test
  public void testGetAbsoluteParent() {
    assertEquals("/content", Path.getAbsoluteParent("/content/a/b/c", 0, resolver));
    assertEquals("/content/a", Path.getAbsoluteParent("/content/a/b/c", 1, resolver));
    assertEquals("/content/a/b", Path.getAbsoluteParent("/content/a/b/c", 2, resolver));
    assertEquals("/content/a/b/c", Path.getAbsoluteParent("/content/a/b/c", 3, resolver));
    assertEquals("", Path.getAbsoluteParent("/content/a/b/c", 4, resolver));
    assertEquals("", Path.getAbsoluteParent("/content/a/b/c", -1, resolver));
  }

  @Test
  public void testGetAbsoluteParent_VersionHistory() {
    assertEquals("/content/versionhistory/user1", Path.getAbsoluteParent("/content/versionhistory/user1/a/b/c", 0, resolver));
    assertEquals("/content/versionhistory/user1/a", Path.getAbsoluteParent("/content/versionhistory/user1/a/b/c", 1, resolver));
    assertEquals("/content/versionhistory/user1/a/b", Path.getAbsoluteParent("/content/versionhistory/user1/a/b/c", 2, resolver));
    assertEquals("/content/versionhistory/user1/a/b/c", Path.getAbsoluteParent("/content/versionhistory/user1/a/b/c", 3, resolver));
    assertEquals("", Path.getAbsoluteParent("/content/versionhistory/user1/a/b/c", 4, resolver));
    assertEquals("", Path.getAbsoluteParent("/content/versionhistory/user1/a/b/c", -1, resolver));
  }

  @Test
  public void testGetAbsoluteParent_VersionHistory_Tenant() {
    context.registerAdapter(ResourceResolver.class, Tenant.class, tenant);
    assertEquals("/content/versionhistory/tenant1/user1", Path.getAbsoluteParent("/content/versionhistory/tenant1/user1/a/b/c", 0, resolver));
    assertEquals("/content/versionhistory/tenant1/user1/a", Path.getAbsoluteParent("/content/versionhistory/tenant1/user1/a/b/c", 1, resolver));
    assertEquals("/content/versionhistory/tenant1/user1/a/b", Path.getAbsoluteParent("/content/versionhistory/tenant1/user1/a/b/c", 2, resolver));
    assertEquals("/content/versionhistory/tenant1/user1/a/b/c", Path.getAbsoluteParent("/content/versionhistory/tenant1/user1/a/b/c", 3, resolver));
    assertEquals("", Path.getAbsoluteParent("/content/versionhistory/tenant1/user1/a/b/c", 4, resolver));
    assertEquals("", Path.getAbsoluteParent("/content/versionhistory/tenant1/user1/a/b/c", -1, resolver));
  }

  @Test
  public void testGetAbsoluteParent_Launches() {
    assertEquals("/content/launches/2018/01/01/launch1/content", Path.getAbsoluteParent("/content/launches/2018/01/01/launch1/content/a/b/c", 0, resolver));
    assertEquals("/content/launches/2018/01/01/launch1/content/a", Path.getAbsoluteParent("/content/launches/2018/01/01/launch1/content/a/b/c", 1, resolver));
    assertEquals("/content/launches/2018/01/01/launch1/content/a/b", Path.getAbsoluteParent("/content/launches/2018/01/01/launch1/content/a/b/c", 2, resolver));
    assertEquals("/content/launches/2018/01/01/launch1/content/a/b/c",
        Path.getAbsoluteParent("/content/launches/2018/01/01/launch1/content/a/b/c", 3, resolver));
    assertEquals("", Path.getAbsoluteParent("/content/launches/2018/01/01/launch1/content/a/b/c", 4, resolver));
    assertEquals("", Path.getAbsoluteParent("/content/launches/2018/01/01/launch1/content/a/b/c", -1, resolver));
  }

  @Test
  public void testGetAbsoluteParent_Page() {
    context.create().page("/content/a");
    context.create().page("/content/a/b");
    Page pageC = context.create().page("/content/a/b/c");

    assertEquals("/content", Path.getAbsoluteParent(pageC, 0, resolver).getPath());
    assertEquals("/content/a", Path.getAbsoluteParent(pageC, 1, resolver).getPath());
    assertEquals("/content/a/b", Path.getAbsoluteParent(pageC, 2, resolver).getPath());
    assertEquals("/content/a/b/c", Path.getAbsoluteParent(pageC, 3, resolver).getPath());
    assertNull(Path.getAbsoluteParent(pageC, 4, resolver));
    assertNull(Path.getAbsoluteParent(pageC, -1, resolver));
  }

  @Test
  public void testGetAbsoluteParent_Page_VersionHistory() {
    context.create().page("/content/versionhistory/user1/a");
    context.create().page("/content/versionhistory/user1/a/b");
    Page pageC = context.create().page("/content/versionhistory/user1/a/b/c");

    assertEquals("/content/versionhistory/user1", Path.getAbsoluteParent(pageC, 0, resolver).getPath());
    assertEquals("/content/versionhistory/user1/a", Path.getAbsoluteParent(pageC, 1, resolver).getPath());
    assertEquals("/content/versionhistory/user1/a/b", Path.getAbsoluteParent(pageC, 2, resolver).getPath());
    assertEquals("/content/versionhistory/user1/a/b/c", Path.getAbsoluteParent(pageC, 3, resolver).getPath());
  }

  @Test
  public void testGetAbsoluteParent_Page_VersionHistory_Tenant() {
    context.registerAdapter(ResourceResolver.class, Tenant.class, tenant);
    context.create().page("/content/versionhistory/tenant1/user1/a");
    context.create().page("/content/versionhistory/tenant1/user1/a/b");
    Page pageC = context.create().page("/content/versionhistory/tenant1/user1/a/b/c");

    assertEquals("/content/versionhistory/tenant1/user1", Path.getAbsoluteParent(pageC, 0, resolver).getPath());
    assertEquals("/content/versionhistory/tenant1/user1/a", Path.getAbsoluteParent(pageC, 1, resolver).getPath());
    assertEquals("/content/versionhistory/tenant1/user1/a/b", Path.getAbsoluteParent(pageC, 2, resolver).getPath());
    assertEquals("/content/versionhistory/tenant1/user1/a/b/c", Path.getAbsoluteParent(pageC, 3, resolver).getPath());
  }

  @Test
  public void testGetAbsoluteParent_Page_Launches() {
    context.create().page("/content/launches/2018/01/01/launch1/content");
    context.create().page("/content/launches/2018/01/01/launch1/content/a");
    context.create().page("/content/launches/2018/01/01/launch1/content/a/b");
    Page pageC = context.create().page("/content/launches/2018/01/01/launch1/content/a/b/c");

    assertEquals("/content/launches/2018/01/01/launch1/content", Path.getAbsoluteParent(pageC, 0, resolver).getPath());
    assertEquals("/content/launches/2018/01/01/launch1/content/a", Path.getAbsoluteParent(pageC, 1, resolver).getPath());
    assertEquals("/content/launches/2018/01/01/launch1/content/a/b", Path.getAbsoluteParent(pageC, 2, resolver).getPath());
    assertEquals("/content/launches/2018/01/01/launch1/content/a/b/c", Path.getAbsoluteParent(pageC, 3, resolver).getPath());
  }

  @Test
  public void testGetOriginalPath() {
    assertEquals("/any/path", Path.getOriginalPath("/any/path", resolver));
    assertEquals("/content/a/b/c", Path.getOriginalPath("/content/a/b/c", resolver));

    assertNull(Path.getOriginalPath("", resolver));
    assertNull(Path.getOriginalPath(null, resolver));
  }

  @Test
  public void testGetOriginalPath_VersionHistory() {
    assertEquals("/content/a/b/c", Path.getOriginalPath("/content/versionhistory/user1/a/b/c", resolver));
  }

  @Test
  public void testGetOriginalPath_VersionHistory_Tenant() {
    context.registerAdapter(ResourceResolver.class, Tenant.class, tenant);
    assertEquals("/content/a/b/c", Path.getOriginalPath("/content/versionhistory/tenant1/user1/a/b/c", resolver));
  }

  @Test
  public void testGetOriginalPath_Launches() {
    assertEquals("/content/a/b/c", Path.getOriginalPath("/content/launches/2018/01/01/launch1/content/a/b/c", resolver));
  }

  @Test
  public void testIsExperienceFragmentPath() {
    assertTrue(Path.isExperienceFragmentPath("/content/experience-fragments/xf1"));
    assertTrue(Path.isExperienceFragmentPath("/content/experience-fragments/level1/level2/xf2"));

    assertFalse(Path.isExperienceFragmentPath("/content/experience-fragments"));
    assertFalse(Path.isExperienceFragmentPath("/content/other-path"));
  }

}
