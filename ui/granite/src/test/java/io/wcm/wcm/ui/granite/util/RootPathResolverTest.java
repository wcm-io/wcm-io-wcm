/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2019 wcm.io
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
package io.wcm.wcm.ui.granite.util;

import static io.wcm.wcm.ui.granite.util.RootPathResolver.DEFAULT_FALLBACK_ROOT_PATH;
import static io.wcm.wcm.ui.granite.util.RootPathResolver.PN_APPEND_PATH;
import static io.wcm.wcm.ui.granite.util.RootPathResolver.PN_FALLBACK_PATH;
import static io.wcm.wcm.ui.granite.util.RootPathResolver.PN_ROOT_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.PageContext;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.adobe.granite.ui.components.ComponentHelper;
import com.adobe.granite.ui.components.ExpressionResolver;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.ui.granite.testcontext.MockExpressionResolver;
import io.wcm.wcm.ui.granite.testcontext.MockPageContext;

@ExtendWith(AemContextExtension.class)
class RootPathResolverTest {

  private final AemContext context = new AemContext();

  @BeforeEach
  void setUp() {
    context.registerService(ExpressionResolver.class, new MockExpressionResolver());
  }

  @Test
  void testDefaultFallbackPath() {
    RootPathResolver underTest = prepareComponent();
    assertRootPath(underTest, DEFAULT_FALLBACK_ROOT_PATH);
  }

  @Test
  void testGivenFallbackPath() {
    context.create().resource("/content");
    RootPathResolver underTest = prepareComponent();
    underTest.setFallbackRootPath("/content");
    assertRootPath(underTest, "/content");
  }

  @Test
  void testGivenFallbackPath_FallbackToExisting() {
    RootPathResolver underTest = prepareComponent();
    underTest.setFallbackRootPath("/content/abc");
    assertRootPath(underTest, DEFAULT_FALLBACK_ROOT_PATH);
  }

  @Test
  void testConfiguredFallbackPath() {
    context.create().resource("/content");
    RootPathResolver underTest = prepareComponent(
        PN_FALLBACK_PATH, "/content");
    assertRootPath(underTest, "/content");
  }

  @Test
  void testConfiguredFallbackPath_FallbackToExisting() {
    RootPathResolver underTest = prepareComponent(
        PN_FALLBACK_PATH, "/content/abc");
    assertRootPath(underTest, DEFAULT_FALLBACK_ROOT_PATH);
  }

  @Test
  void testConfiguredRootPath() {
    context.create().resource("/content/abc/def");

    RootPathResolver underTest = prepareComponent(
        PN_ROOT_PATH, "/content/abc/def");
    assertRootPath(underTest, "/content/abc/def");
  }

  @Test
  void testConfiguredRootPath_FallbackToExisting() {
    context.create().resource("/content");

    RootPathResolver underTest = prepareComponent(
        PN_ROOT_PATH, "/content/abc/def");
    assertRootPath(underTest, "/content");
  }

  @Test
  void testConfiguredRootPath_AppendPath() {
    context.create().resource("/content/abc/def");

    RootPathResolver underTest = prepareComponent(
        PN_ROOT_PATH, "/content",
        PN_APPEND_PATH, "/abc/def");
    assertRootPath(underTest, "/content/abc/def");
  }

  @Test
  void testConfiguredRootPath_AppendPath_FallbackToExisting() {
    context.create().resource("/content/abc");

    RootPathResolver underTest = prepareComponent(
        PN_ROOT_PATH, "/content",
        PN_APPEND_PATH, "/abc/def");
    assertRootPath(underTest, "/content/abc");
  }

  @Test
  void testDetectedRootPath() {
    context.create().resource("/content/abc/def");

    RootPathResolver underTest = prepareComponent();
    underTest.setRootPathDetector((cmp, request) -> "/content/abc/def");
    assertRootPath(underTest, "/content/abc/def");
  }

  @Test
  void testDetectedRootPath_AppendPath_FallbackToExisting() {
    context.create().resource("/content/abc");

    RootPathResolver underTest = prepareComponent(
        PN_APPEND_PATH, "abc/def");
    underTest.setRootPathDetector((cmp, request) -> "/content");
    assertRootPath(underTest, "/content/abc");
  }

  private RootPathResolver prepareComponent(Object... cmpProps) {
    Resource cmpResource = context.create().resource("/apps/app1/components/comp1", cmpProps);
    context.currentResource(cmpResource);

    PageContext pageContext = new MockPageContext(context);
    ComponentHelper cmp = new ComponentHelper(pageContext);

    return new RootPathResolver(cmp, context.request());
  }

  private void assertRootPath(RootPathResolver underTest, String expectedPath) {
    assertEquals(expectedPath, underTest.get());
    Map<String, Object> expectedProps = new HashMap<>();
    expectedProps.put(PN_ROOT_PATH, expectedPath);
    expectedProps.put(PN_APPEND_PATH, "");
    expectedProps.put(PN_FALLBACK_PATH, "");
    assertEquals(expectedProps, underTest.getOverrideProperties());
  }

}
