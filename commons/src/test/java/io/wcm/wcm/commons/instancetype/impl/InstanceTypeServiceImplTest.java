/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2021 wcm.io
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
package io.wcm.wcm.commons.instancetype.impl;

import static io.wcm.wcm.commons.instancetype.impl.InstanceTypeServiceImpl.WCM_MODE_PROPERTY;
import static io.wcm.wcm.commons.instancetype.impl.InstanceTypeServiceImpl.WCM_REQUEST_FILTER_PID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import com.day.cq.wcm.api.WCMMode;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.commons.instancetype.InstanceTypeService;
import io.wcm.wcm.commons.util.RunMode;

@ExtendWith(AemContextExtension.class)
class InstanceTypeServiceImplTest {

  final AemContext context = new AemContext();

  @Test
  void testWithoutAnyConfig() {
    InstanceTypeService underTest = context.registerInjectActivateService(InstanceTypeServiceImpl.class);
    assertPublish(underTest);
  }

  @Test
  void testWithGuessingFromWcmFilterConfig_Author() throws IOException {
    // prepare a WCM request filter config as it is expected to be found on author
    createWcmRequestFilerConfig(WCMMode.EDIT);

    InstanceTypeService underTest = context.registerInjectActivateService(InstanceTypeServiceImpl.class);
    assertAuthor(underTest);
  }

  @Test
  void testWithGuessingFromWcmFilterConfig_Publish() throws IOException {
    // prepare a WCM request filter config as it is expected to be found on publish
    createWcmRequestFilerConfig(WCMMode.DISABLED);

    InstanceTypeService underTest = context.registerInjectActivateService(InstanceTypeServiceImpl.class);
    assertPublish(underTest);
  }

  @Test
  void testWithExplicitConfig_Author() {
    InstanceTypeService underTest = context.registerInjectActivateService(InstanceTypeServiceImpl.class,
        "instance.type", RunMode.AUTHOR);
    assertAuthor(underTest);
  }

  @Test
  void testWithExplicitConfig_Publish() {
    InstanceTypeService underTest = context.registerInjectActivateService(InstanceTypeServiceImpl.class,
        "instance.type", RunMode.PUBLISH);
    assertPublish(underTest);
  }

  private void assertAuthor(InstanceTypeService underTest) {
    assertTrue(underTest.isAuthor());
    assertFalse(underTest.isPublish());
    assertEquals(Collections.singleton(RunMode.AUTHOR), underTest.getRunModes());
  }

  private void assertPublish(InstanceTypeService underTest) {
    assertFalse(underTest.isAuthor());
    assertTrue(underTest.isPublish());
    assertEquals(Collections.singleton(RunMode.PUBLISH), underTest.getRunModes());
  }

  @SuppressWarnings("null")
  private void createWcmRequestFilerConfig(WCMMode wcmMode) throws IOException {
    ConfigurationAdmin configAdmin = context.getService(ConfigurationAdmin.class);
    Configuration config = configAdmin.getConfiguration(WCM_REQUEST_FILTER_PID);
    Dictionary<String, Object> props = new Hashtable<>();
    props.put(WCM_MODE_PROPERTY, wcmMode.name());
    config.update(props);
  }

}
