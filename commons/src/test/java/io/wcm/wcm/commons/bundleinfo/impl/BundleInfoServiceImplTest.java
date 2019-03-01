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
package io.wcm.wcm.commons.bundleinfo.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.osgi.framework.Constants.BUNDLE_NAME;
import static org.osgi.framework.Constants.BUNDLE_VERSION;
import static org.osgi.framework.Constants.FRAGMENT_HOST;

import java.util.Dictionary;
import java.util.List;

import org.apache.sling.testing.mock.osgi.MapUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.google.common.collect.ImmutableList;

import io.wcm.wcm.commons.bundleinfo.BundleInfo;
import io.wcm.wcm.commons.bundleinfo.BundleInfoService;
import io.wcm.wcm.commons.bundleinfo.BundleState;

@RunWith(MockitoJUnitRunner.class)
public class BundleInfoServiceImplTest {

  @Mock
  private BundleContext bundleContext;

  private BundleInfoService underTest;

  @Before
  public void setUp() {
    // instantiate OSGi service directly to be able to inject a mock bundle context
    BundleInfoServiceImpl instance = new BundleInfoServiceImpl();
    instance.activate(bundleContext);
    underTest = instance;
  }

  @Test
  public void testEmpty() {
    when(bundleContext.getBundles()).thenReturn(new Bundle[0]);
    assertTrue(underTest.getBundles().isEmpty());
  }

  @Test
  public void testBundles() {
    bundles(
        bundle("bundle1", BundleState.ACTIVE, BUNDLE_NAME, "Bundle 1", BUNDLE_VERSION, "1.2.3"),
        bundle("minimalBundle2", BundleState.RESOLVED),
        bundle("fragmentBundle3", BundleState.INSTALLED, BUNDLE_NAME, "Fragment 1", BUNDLE_VERSION, "1.0", FRAGMENT_HOST, "fragment"));

    List<BundleInfo> result = ImmutableList.copyOf(underTest.getBundles());
    assertEquals(3, result.size());

    BundleInfo bundle1 = result.get(0);
    assertEquals("bundle1", bundle1.getSymbolicName());
    assertEquals("Bundle 1", bundle1.getName());
    assertEquals(BundleState.ACTIVE, bundle1.getState());
    assertEquals("1.2.3", bundle1.getVersion());
    assertNotNull(bundle1.getLastModified());
    assertFalse(bundle1.isFragment());

    BundleInfo fragmentBundle3 = result.get(1);
    assertEquals("fragmentBundle3", fragmentBundle3.getSymbolicName());
    assertEquals("Fragment 1", fragmentBundle3.getName());
    assertEquals(BundleState.FRAGMENT, fragmentBundle3.getState());
    assertEquals("1.0", fragmentBundle3.getVersion());
    assertNotNull(bundle1.getLastModified());
    assertTrue(fragmentBundle3.isFragment());

    BundleInfo minimalBundle2 = result.get(2);
    assertEquals("minimalBundle2", minimalBundle2.getSymbolicName());
    assertEquals("minimalBundle2", minimalBundle2.getName());
    assertEquals(BundleState.RESOLVED, minimalBundle2.getState());
    assertEquals("", minimalBundle2.getVersion());
    assertNotNull(bundle1.getLastModified());
    assertFalse(minimalBundle2.isFragment());
  }

  private void bundles(Bundle... bundles) {
    when(bundleContext.getBundles()).thenReturn(bundles);
  }

  @SuppressWarnings({ "unchecked", "null" })
  private Bundle bundle(String symbolicName, BundleState state, Object... headers) {
    Dictionary headerDictionary = MapUtil.toDictionary(headers);
    Bundle bundle = mock(Bundle.class);
    when(bundle.getSymbolicName()).thenReturn(symbolicName);
    when(bundle.getState()).thenReturn(state.getStateValue());
    when(bundle.getHeaders()).thenReturn(headerDictionary);
    when(bundle.getLastModified()).thenReturn(System.currentTimeMillis());
    return bundle;
  }

}
