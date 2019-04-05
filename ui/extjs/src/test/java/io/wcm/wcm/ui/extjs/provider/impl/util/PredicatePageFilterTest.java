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
package io.wcm.wcm.ui.extjs.provider.impl.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.apache.commons.collections.Predicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.day.cq.wcm.api.Page;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PredicatePageFilterTest {

  @Mock
  private Predicate predicate;
  @Mock
  private Page page;

  private PredicatePageFilter underTest;

  @BeforeEach
  void setUp() throws Exception {
    underTest = new PredicatePageFilter(predicate);
    when(page.isValid()).thenReturn(true);
    when(page.isHideInNav()).thenReturn(false);
  }

  @Test
  void testIncludesAccept() {
    when(predicate.evaluate(page)).thenReturn(true);
    assertTrue(underTest.includes(page));
  }

  @Test
  void testIncludesDeny() {
    when(predicate.evaluate(page)).thenReturn(false);
    assertFalse(underTest.includes(page));
  }

  @Test
  void testIncludesAccept_InvalidPage() {
    when(predicate.evaluate(page)).thenReturn(true);
    when(page.isValid()).thenReturn(false);
    assertFalse(underTest.includes(page));
  }

}
