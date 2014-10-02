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
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.wcm.parsys.componentinfo.ParsysConfig;
import io.wcm.wcm.parsys.componentinfo.ParsysConfigManager;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.sling.api.SlingConstants;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class ParsysConfigManagerImplTest {

  private static final String RESOURCE_PATH_1 = "/apps/sample/components/component1";
  private static final String RESOURCE_PATH_2 = "/apps/sample/components/component2";

  private static final Pattern PATH_PATTERN_1 = Pattern.compile("^" + Pattern.quote("jcr:content/path1") + "$");
  private static final Pattern PATH_PATTERN_2 = Pattern.compile("^.*/path2(/.*)?$");

  @Rule
  public AemContext context = new AemContext();

  @Mock
  private ParsysConfig parsysConfig1;
  @Mock
  private ParsysConfig parsysConfig2;

  private ParsysConfigManager underTest;

  @Before
  public void setUp() {
    context.create().resource(RESOURCE_PATH_1);
    context.create().resource(RESOURCE_PATH_2,
        ImmutableValueMap.of(SlingConstants.NAMESPACE_PREFIX + ":" + SlingConstants.PROPERTY_RESOURCE_SUPER_TYPE, RESOURCE_PATH_1));

    when(parsysConfig1.getPageComponentPath()).thenReturn(RESOURCE_PATH_1);
    when(parsysConfig1.getPathPattern()).thenReturn(PATH_PATTERN_1);
    when(parsysConfig2.getPageComponentPath()).thenReturn(RESOURCE_PATH_2);
    when(parsysConfig2.getPathPattern()).thenReturn(PATH_PATTERN_2);

    context.registerService(ParsysConfig.class, parsysConfig1);
    context.registerService(ParsysConfig.class, parsysConfig2);

    underTest = context.registerInjectActivateService(new ParsysConfigManagerImpl());
  }

  @Test
  public void testGetPageComponentFromOsgi() {
    List<ParsysConfig> configs;

    configs = Lists.newArrayList(underTest.getParsysConfigs(RESOURCE_PATH_1, context.resourceResolver()));
    assertEquals(1, configs.size());
    assertSame(parsysConfig1, configs.get(0));

    configs = Lists.newArrayList(underTest.getParsysConfigs(RESOURCE_PATH_2, context.resourceResolver()));
    assertEquals(2, configs.size());
    assertSame(parsysConfig2, configs.get(0));
    assertSame(parsysConfig1, configs.get(1));
  }

  @Test
  public void testGetPageComponentRelativePathFromOsgi() {
    List<ParsysConfig> configs;

    configs = Lists.newArrayList(underTest.getParsysConfigs(RESOURCE_PATH_1, "jcr:content/path0", context.resourceResolver()));
    assertEquals(0, configs.size());

    configs = Lists.newArrayList(underTest.getParsysConfigs(RESOURCE_PATH_2, "jcr:content/path1", context.resourceResolver()));
    assertEquals(1, configs.size());
    assertSame(parsysConfig1, configs.get(0));

    configs = Lists.newArrayList(underTest.getParsysConfigs(RESOURCE_PATH_2, "jcr:content/path2", context.resourceResolver()));
    assertEquals(1, configs.size());
    assertSame(parsysConfig2, configs.get(0));

    configs = Lists.newArrayList(underTest.getParsysConfigs(RESOURCE_PATH_2, "jcr:content/xxx/path1", context.resourceResolver()));
    assertEquals(0, configs.size());
    configs = Lists.newArrayList(underTest.getParsysConfigs(RESOURCE_PATH_2, "jcr:content/xxx/path2/yyy", context.resourceResolver()));
    assertEquals(1, configs.size());
    assertSame(parsysConfig2, configs.get(0));
  }

}
