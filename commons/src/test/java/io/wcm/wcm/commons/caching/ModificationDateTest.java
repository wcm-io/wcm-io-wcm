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
package io.wcm.wcm.commons.caching;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ValueMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;

import io.wcm.sling.commons.resource.ImmutableValueMap;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ModificationDateTest {

  static final Calendar SAMPLE_CALENDAR_1 = DateUtils.toCalendar(new Date(1000000L));
  static final Calendar SAMPLE_CALENDAR_2 = DateUtils.toCalendar(new Date(1200000L));

  @Mock
  private Page page1;
  @Mock
  private Page page2;
  @Mock
  private Resource resource1;
  @Mock
  private Resource resource2;

  @BeforeEach
  void setUp() {
    when(resource1.getValueMap()).thenReturn(ValueMap.EMPTY);
    when(resource2.getValueMap()).thenReturn(ValueMap.EMPTY);
  }

  @Test
  void testGetPage() {
    assertNull(ModificationDate.get((Page)null));

    assertNull(ModificationDate.get(page1));

    applyPageLastModified(page1, SAMPLE_CALENDAR_1);
    assertEquals(SAMPLE_CALENDAR_1.getTime(), ModificationDate.get(page1));
  }

  @Test
  void testGetResource() {
    assertNull(ModificationDate.get((Resource)null));

    assertNull(ModificationDate.get(resource1));

    applyPageLastModified(resource1, SAMPLE_CALENDAR_1);
    assertEquals(SAMPLE_CALENDAR_1.getTime(), ModificationDate.get(resource1));

    applyPageLastModified(resource1, SAMPLE_CALENDAR_1);
    applyLastModified(resource1, SAMPLE_CALENDAR_2);
    assertEquals(SAMPLE_CALENDAR_2.getTime(), ModificationDate.get(resource1));
  }

  @Test
  void testMostRecentResourceArray() {
    applyLastModified(resource1, SAMPLE_CALENDAR_1);
    applyLastModified(resource2, SAMPLE_CALENDAR_2);
    assertEquals(SAMPLE_CALENDAR_2.getTime(), ModificationDate.mostRecent(resource1, resource2));
  }

  @Test
  void testMostRecentPageArray() {
    applyPageLastModified(page1, SAMPLE_CALENDAR_1);
    applyPageLastModified(page2, SAMPLE_CALENDAR_2);
    assertEquals(SAMPLE_CALENDAR_2.getTime(), ModificationDate.mostRecent(page1, page2));
  }

  @Test
  void testMostRecentPageArray_WithNullDate() {
    applyPageLastModified(page1, SAMPLE_CALENDAR_1);
    assertEquals(SAMPLE_CALENDAR_1.getTime(), ModificationDate.mostRecent(page1, page2));
  }

  @Test
  void testMostRecentModificationDateProviderArray() throws Exception {
    applyPageLastModified(page1, SAMPLE_CALENDAR_1);
    applyPageLastModified(resource2, SAMPLE_CALENDAR_2);
    assertEquals(SAMPLE_CALENDAR_2.getTime(), ModificationDate.mostRecent(
        new PageModificationDateProvider(page1),
        new ResourceModificationDateProvider(resource2)
        ));
  }

  @Test
  void testMostRecentDateArray() {
    assertEquals(SAMPLE_CALENDAR_2.getTime(),
        ModificationDate.mostRecent(SAMPLE_CALENDAR_1.getTime(), SAMPLE_CALENDAR_2.getTime()));
    assertEquals(SAMPLE_CALENDAR_2.getTime(),
        ModificationDate.mostRecent(SAMPLE_CALENDAR_2.getTime(), SAMPLE_CALENDAR_1.getTime()));
  }

  static void applyLastModified(Resource resource, Calendar value) {
    ResourceMetadata metadata = new ResourceMetadata();
    metadata.setModificationTime(value.getTimeInMillis());
    when(resource.getResourceMetadata()).thenReturn(metadata);
  }

  static void applyPageLastModified(Resource resource, Calendar value) {
    when(resource.getValueMap()).thenReturn(
        ImmutableValueMap.of(NameConstants.PN_PAGE_LAST_MOD, value.getTime()));
  }

  static void applyPageLastModified(Page page, Calendar value) {
    when(page.getLastModified()).thenReturn(value);
  }

}
