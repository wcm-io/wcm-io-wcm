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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.ImmutableSet;

public class RunModeTest {

  @Test
  public void testIs() {
    Set<String> runModes = ImmutableSet.<String>builder().add("mode1", "mode2").build();
    assertTrue(RunMode.is(runModes, "mode1"));
    assertTrue(RunMode.is(runModes, "mode2"));
    assertFalse(RunMode.is(runModes, "mode3"));
  }

  @Test
  public void testIsEmptySet() {
    Set<String> runModes = Collections.emptySet();
    assertFalse(RunMode.is(runModes, "mode1"));
    assertFalse(RunMode.is(runModes, "mode2"));
    assertFalse(RunMode.is(runModes, "mode3"));
  }

  @Test
  public void testIsNullSet() {
    Set<String> runModes = null;
    assertFalse(RunMode.is(runModes, "mode1"));
    assertFalse(RunMode.is(runModes, "mode2"));
    assertFalse(RunMode.is(runModes, "mode3"));
  }

  @Test
  public void testIsInvalidParams() {
    Set<String> runModes = ImmutableSet.<String>builder().add("mode1", "mode2").build();
    assertFalse(RunMode.is(runModes));
    assertFalse(RunMode.is(runModes, (String[])null));
    assertFalse(RunMode.is(runModes, (String)null));
  }

}
