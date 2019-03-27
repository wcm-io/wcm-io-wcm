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
package io.wcm.wcm.commons.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Hashtable;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableSet;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("deprecation")
class RunModeTest {

  private static final Set<String> AUTHOR_RUNMODES = ImmutableSet.of(RunMode.AUTHOR);
  private static final Set<String> PUBLISH_RUNMODES = ImmutableSet.of(RunMode.PUBLISH, "anotherRunMode");

  @Mock
  private ComponentContext componentContext;
  @Mock
  private Logger logger;

  @BeforeEach
  void setUp() {
    when(logger.isDebugEnabled()).thenReturn(true);
    Hashtable<String, Object> props = new Hashtable<String, Object>();
    props.put("component.name", "myName");
    when(componentContext.getProperties()).thenReturn(props);
  }

  @Test
  void testIs() {
    Set<String> runModes = ImmutableSet.of("mode1", "mode2");
    assertTrue(RunMode.is(runModes, "mode1"));
    assertTrue(RunMode.is(runModes, "mode2"));
    assertFalse(RunMode.is(runModes, "mode3"));
  }

  @Test
  void testIsEmptySet() {
    Set<String> runModes = ImmutableSet.of();
    assertFalse(RunMode.is(runModes, "mode1"));
    assertFalse(RunMode.is(runModes, "mode2"));
    assertFalse(RunMode.is(runModes, "mode3"));
  }

  @Test
  void testIsNullSet() {
    Set<String> runModes = null;
    assertFalse(RunMode.is(runModes, "mode1"));
    assertFalse(RunMode.is(runModes, "mode2"));
    assertFalse(RunMode.is(runModes, "mode3"));
  }

  @Test
  void testIsInvalidParams() {
    Set<String> runModes = ImmutableSet.of("mode1", "mode2");
    assertFalse(RunMode.is(runModes));
    assertFalse(RunMode.is(runModes, (String[])null));
    assertFalse(RunMode.is(runModes, (String)null));
  }

  @Test
  void testIsAuthor() {
    Set<String> runModes = ImmutableSet.of("mode1", "author");
    assertTrue(RunMode.isAuthor(runModes));
  }

  @Test
  void testIsPublish() {
    Set<String> runModes = ImmutableSet.of("publish");
    assertTrue(RunMode.isPublish(runModes));
  }

  @Test
  void testDisableIfNotAuthor_Author() {
    boolean disabled = RunMode.disableIfNotAuthor(AUTHOR_RUNMODES, componentContext, logger);
    assertFalse(disabled);
    verify(componentContext, never()).disableComponent(anyString());
  }

  @Test
  void testDisableIfNotAuthor_Publish() {
    boolean disabled = RunMode.disableIfNotAuthor(PUBLISH_RUNMODES, componentContext, logger);
    assertTrue(disabled);
    verify(componentContext).disableComponent(anyString());
  }

  @Test
  void testDisableIfNotPublish_Author() {
    boolean disabled = RunMode.disableIfNotPublish(AUTHOR_RUNMODES, componentContext, logger);
    assertTrue(disabled);
    verify(componentContext).disableComponent(anyString());
  }

  @Test
  void testDisableIfNotPublish_Publish() {
    boolean disabled = RunMode.disableIfNotPublish(PUBLISH_RUNMODES, componentContext, logger);
    assertFalse(disabled);
    verify(componentContext, never()).disableComponent(anyString());
  }

}
