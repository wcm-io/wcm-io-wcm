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
package io.wcm.wcm.parsys.componentinfo.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.wcm.parsys.componentinfo.ParsysConfig;

import java.util.Set;
import java.util.regex.Pattern;

import org.junit.Rule;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

public class OsgiParsysConfigProviderTest {

  private static final String COMPONENT_PATH = "/component/path";
  private static final Set<String> ALLOWED_PARENTS = ImmutableSet.of("/parent1", "/parent2");
  private static final Set<String> ALLOWED_CHILDREN = ImmutableSet.of("/child1", "/child2");
  private static final Set<String> DENIED_CHILDREN = ImmutableSet.of("/child3");

  @Rule
  public AemContext context = new AemContext();

  @Test
  public void testWithProperties_Path() {
    ParsysConfig underTest = context.registerInjectActivateService(new OsgiParsysConfigProvider(),
        ImmutableValueMap.builder()
        .put(OsgiParsysConfigProvider.PROPERTY_PAGE_COMPONENT_PATH, COMPONENT_PATH)
        .put(OsgiParsysConfigProvider.PROPERTY_PATH, "localpath")
        .put(OsgiParsysConfigProvider.PROPERTY_PARENT_ANCESTOR_LEVEL, 2)
        .put(OsgiParsysConfigProvider.PROPERTY_ALLOWED_PARENTS, ALLOWED_PARENTS.toArray(new String[ALLOWED_PARENTS.size()]))
        .put(OsgiParsysConfigProvider.PROPERTY_ALLOWED_CHILDREN, ALLOWED_CHILDREN.toArray(new String[ALLOWED_CHILDREN.size()]))
        .put(OsgiParsysConfigProvider.PROPERTY_DENIED_CHILDREN, DENIED_CHILDREN.toArray(new String[DENIED_CHILDREN.size()]))
        .build());

    assertEquals(COMPONENT_PATH, underTest.getPageComponentPath());
    assertEquals("^" + Pattern.quote("jcr:content/localpath") + "$", underTest.getPathPattern().toString());
    assertEquals(2, underTest.getParentAncestorLevel());
    assertEquals(ALLOWED_PARENTS, underTest.getAllowedParents());
    assertEquals(ALLOWED_CHILDREN, underTest.getAllowedChildren());
    assertEquals(DENIED_CHILDREN, underTest.getDeniedChildren());
  }

  @Test
  public void testWithProperties_PathPattern() {
    ParsysConfig underTest = context.registerInjectActivateService(new OsgiParsysConfigProvider(),
        ImmutableValueMap.builder()
        .put(OsgiParsysConfigProvider.PROPERTY_PAGE_COMPONENT_PATH, COMPONENT_PATH)
        .put(OsgiParsysConfigProvider.PROPERTY_PATH_PATTERN, ".*any.*")
        .put(OsgiParsysConfigProvider.PROPERTY_PARENT_ANCESTOR_LEVEL, 1)
        .put(OsgiParsysConfigProvider.PROPERTY_ALLOWED_PARENTS, ALLOWED_PARENTS.toArray(new String[ALLOWED_PARENTS.size()]))
        .put(OsgiParsysConfigProvider.PROPERTY_ALLOWED_CHILDREN, ALLOWED_CHILDREN.toArray(new String[ALLOWED_CHILDREN.size()]))
        .build());

    assertEquals(COMPONENT_PATH, underTest.getPageComponentPath());
    assertEquals(".*any.*", underTest.getPathPattern().toString());
    assertEquals(1, underTest.getParentAncestorLevel());
    assertEquals(ALLOWED_PARENTS, underTest.getAllowedParents());
    assertEquals(ALLOWED_CHILDREN, underTest.getAllowedChildren());
  }

  @Test
  public void testWithoutProperties() {
    ParsysConfig underTest = context.registerInjectActivateService(new OsgiParsysConfigProvider());

    assertNull(underTest.getPageComponentPath());
    assertNull(underTest.getPathPattern());
    assertEquals(OsgiParsysConfigProvider.DEFAULT_PARENT_ANCESTOR_LEVEL, underTest.getParentAncestorLevel());
    assertTrue(underTest.getAllowedParents().isEmpty());
    assertTrue(underTest.getAllowedChildren().isEmpty());
    assertTrue(underTest.getDeniedChildren().isEmpty());
  }

}
