/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2015 wcm.io
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
package io.wcm.wcm.parsys.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CssBuilderTest {

  private CssBuilder underTest;

  @BeforeEach
  void setUp() {
    underTest = new CssBuilder();
  }

  @Test
  void testNoItems() {
    assertNull(underTest.build());
  }

  @Test
  void testAppendNull() {
    underTest.add(null);
    assertNull(underTest.build());
  }

  @Test
  void testAppendBlank() {
    underTest.add(" ");
    assertNull(underTest.build());
  }

  @Test
  void testAppendOne() {
    underTest.add("one");
    assertEquals("one", underTest.build());
  }

  @Test
  void testAppendTwo() {
    underTest.add("two one");
    underTest.add(null);
    assertEquals("one two", underTest.build());
  }

}
