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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.common.collect.ImmutableSet;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.parsys.componentinfo.ParsysConfig;
import io.wcm.wcm.parsys.textcontext.AppAemContext;

@ExtendWith(AemContextExtension.class)
@SuppressWarnings("null")
class OsgiParsysConfigProviderTest {

  private static final String COMPONENT_PATH = "/component/path";
  private static final Set<String> ALLOWED_PARENTS = ImmutableSet.of("/parent1", "/parent2");
  private static final Set<String> ALLOWED_CHILDREN = ImmutableSet.of("/child1", "/child2");
  private static final Set<String> DENIED_CHILDREN = ImmutableSet.of("/child3");

  private final AemContext context = AppAemContext.newAemContext();

  @Test
  void testWithProperties_Path() {
    ParsysConfig underTest = context.registerInjectActivateService(new OsgiParsysConfigProvider(),
        "pageComponentPath", COMPONENT_PATH,
        "path", "localpath",
        "parentAncestorLevel", 2,
        "allowedParents", ALLOWED_PARENTS.toArray(new String[ALLOWED_PARENTS.size()]),
        "allowedChildren", ALLOWED_CHILDREN.toArray(new String[ALLOWED_CHILDREN.size()]),
        "deniedChildren", DENIED_CHILDREN.toArray(new String[DENIED_CHILDREN.size()]));

    assertEquals(COMPONENT_PATH, underTest.getPageComponentPath());
    assertEquals("^" + Pattern.quote("jcr:content/localpath") + "$", underTest.getPathPattern().toString());
    assertEquals(2, underTest.getParentAncestorLevel());
    assertEquals(ALLOWED_PARENTS, underTest.getAllowedParents());
    assertEquals(ALLOWED_CHILDREN, underTest.getAllowedChildren());
    assertEquals(DENIED_CHILDREN, underTest.getDeniedChildren());
    assertEquals(true, underTest.isInherit());
  }

  @Test
  void testWithProperties_PathPattern() {
    ParsysConfig underTest = context.registerInjectActivateService(new OsgiParsysConfigProvider(),
        "pageComponentPath", COMPONENT_PATH,
        "pathPattern", ".*any.*",
        "parentAncestorLevel", 1,
        "allowedParents", ALLOWED_PARENTS.toArray(new String[ALLOWED_PARENTS.size()]),
        "allowedChildren", ALLOWED_CHILDREN.toArray(new String[ALLOWED_CHILDREN.size()]),
        "inherit", false);

    assertEquals(COMPONENT_PATH, underTest.getPageComponentPath());
    assertEquals(".*any.*", underTest.getPathPattern().toString());
    assertEquals(1, underTest.getParentAncestorLevel());
    assertEquals(ALLOWED_PARENTS, underTest.getAllowedParents());
    assertEquals(ALLOWED_CHILDREN, underTest.getAllowedChildren());
    assertEquals(false, underTest.isInherit());
  }

  @Test
  void testWithoutProperties() {
    ParsysConfig underTest = context.registerInjectActivateService(new OsgiParsysConfigProvider());

    assertNull(underTest.getPageComponentPath());
    assertNull(underTest.getPathPattern());
    assertEquals(OsgiParsysConfigProvider.DEFAULT_PARENT_ANCESTOR_LEVEL, underTest.getParentAncestorLevel());
    assertTrue(underTest.getAllowedParents().isEmpty());
    assertTrue(underTest.getAllowedChildren().isEmpty());
    assertTrue(underTest.getDeniedChildren().isEmpty());
    assertEquals(true, underTest.isInherit());
  }

}
