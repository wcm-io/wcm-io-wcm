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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.google.common.collect.ImmutableMap;

@RunWith(MockitoJUnitRunner.class)
public class ModificationDateTest {

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

  @Test
  public void testGetPage() {
    assertNull(ModificationDate.get((Page)null));

    assertNull(ModificationDate.get(page1));

    applyPageLastModified(page1, SAMPLE_CALENDAR_1);
    assertEquals(SAMPLE_CALENDAR_1.getTime(), ModificationDate.get(page1));
  }

  @Test
  public void testGetResource() {
    assertNull(ModificationDate.get((Resource)null));

    assertNull(ModificationDate.get(resource1));

    applyPageLastModified(resource1, SAMPLE_CALENDAR_1);
    assertEquals(SAMPLE_CALENDAR_1.getTime(), ModificationDate.get(resource1));

    applyPageLastModified(resource1, SAMPLE_CALENDAR_1);
    applyLastModified(resource1, SAMPLE_CALENDAR_2);
    assertEquals(SAMPLE_CALENDAR_2.getTime(), ModificationDate.get(resource1));
  }

  @Test
  public void testMostRecentResourceArray() {
    applyLastModified(resource1, SAMPLE_CALENDAR_1);
    applyLastModified(resource2, SAMPLE_CALENDAR_2);
    assertEquals(SAMPLE_CALENDAR_2.getTime(), ModificationDate.mostRecent(resource1, resource2));
  }

  @Test
  public void testMostRecentPageArray() {
    applyPageLastModified(page1, SAMPLE_CALENDAR_1);
    applyPageLastModified(page2, SAMPLE_CALENDAR_2);
    assertEquals(SAMPLE_CALENDAR_2.getTime(), ModificationDate.mostRecent(page1, page2));
  }

  @Test
  public void testMostRecentModificationDateProviderArray() throws Exception {
    applyPageLastModified(page1, SAMPLE_CALENDAR_1);
    applyPageLastModified(resource2, SAMPLE_CALENDAR_2);
    assertEquals(SAMPLE_CALENDAR_2.getTime(), ModificationDate.mostRecent(
        new PageModificationDateProvider(page1),
        new ResourceModificationDateProvider(resource2)
        ));
  }

  @Test
  public void testMostRecentDateArray() {
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
    when(resource.getValueMap()).thenReturn(new ValueMapDecorator(ImmutableMap.<String, Object>builder()
        .put(NameConstants.PN_PAGE_LAST_MOD, value.getTime()).build()));
  }

  static void applyPageLastModified(Page page, Calendar value) {
    when(page.getLastModified()).thenReturn(value);
  }

}
