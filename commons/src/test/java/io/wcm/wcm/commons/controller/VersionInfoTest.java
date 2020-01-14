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
package io.wcm.wcm.commons.controller;

import static io.wcm.wcm.commons.controller.VersionInfo.PN_FILTER_REGEX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.common.collect.ImmutableList;

import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.commons.bundleinfo.BundleInfo;
import io.wcm.wcm.commons.bundleinfo.BundleInfoService;
import io.wcm.wcm.commons.component.impl.ComponentPropertyResolverFactoryImpl;
import io.wcm.wcm.commons.testcontext.AppAemContext;

@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class VersionInfoTest {

  private final AemContext context = AppAemContext.newAemContext();

  @Mock
  private BundleInfoService bundleInfoService;

  @BeforeEach
  void setUp() {
    context.registerService(BundleInfoService.class, bundleInfoService);
    context.registerInjectActivateService(new ComponentPropertyResolverFactoryImpl());

    List<BundleInfo> bundles = ImmutableList.of(
        bundle("aaa.bundle1"),
        bundle("aaa.bundle2"),
        bundle("bbb.bundle3"));
    when(bundleInfoService.getBundles()).thenReturn(bundles);
  }

  @Test
  void testUnfiltered() {
    context.currentPage(context.create().page("/content/page1"));
    assertBundles("aaa.bundle1", "aaa.bundle2", "bbb.bundle3");
  }

  @Test
  void testFiltered_PageProperties() {
    context.currentPage(context.create().page("/content/page1", null, ImmutableValueMap.of(
        PN_FILTER_REGEX, "^aaa\\..*$")));
    assertBundles("aaa.bundle1", "aaa.bundle2");
  }

  @Test
  void testFiltered_PageProperties_Array() {
    context.currentPage(context.create().page("/content/page1", null, ImmutableValueMap.of(
        PN_FILTER_REGEX, new String[] { "^.*\\.bundle2$", "^.*\\.bundle3$" })));
    assertBundles("aaa.bundle2", "bbb.bundle3");
  }

  @Test
  void testFiltered_PageComponent() {
    context.create().resource("/apps/app1/components/comp1",
        PN_FILTER_REGEX, "^aaa\\..*$");
    context.currentPage(context.create().page("/content/page1", null, ImmutableValueMap.of(
        "sling:resourceType", "/apps/app1/components/comp1")));
    assertBundles("aaa.bundle1", "aaa.bundle2");
  }

  @Test
  void testFiltered_PageComponent_Array() {
    context.create().resource("/apps/app1/components/comp1",
        PN_FILTER_REGEX, new String[] { "^.*\\.bundle2$", "^.*\\.bundle3$" });
    context.currentPage(context.create().page("/content/page1", null, ImmutableValueMap.of(
        "sling:resourceType", "/apps/app1/components/comp1")));
    assertBundles("aaa.bundle2", "bbb.bundle3");
  }

  private BundleInfo bundle(String symbolicName) {
    BundleInfo bundle = mock(BundleInfo.class);
    when(bundle.getSymbolicName()).thenReturn(symbolicName);
    return bundle;
  }

  private void assertBundles(String... expectedBundles) {
    List<String> expected = ImmutableList.copyOf(expectedBundles);

    VersionInfo underTest = AdaptTo.notNull(context.request(), VersionInfo.class);
    List<String> actual = underTest.getBundles().stream()
        .map(BundleInfo::getSymbolicName)
        .collect(Collectors.toList());

    assertEquals(expected, actual);
  }

}
