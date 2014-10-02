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

import static io.wcm.wcm.commons.caching.CacheHeader.HEADER_CACHE_CONTROL;
import static io.wcm.wcm.commons.caching.CacheHeader.HEADER_DISPATCHER;
import static io.wcm.wcm.commons.caching.CacheHeader.HEADER_EXPIRES;
import static io.wcm.wcm.commons.caching.CacheHeader.HEADER_IF_MODIFIED_SINCE;
import static io.wcm.wcm.commons.caching.CacheHeader.HEADER_LAST_MODIFIED;
import static io.wcm.wcm.commons.caching.CacheHeader.HEADER_PRAGMA;
import static io.wcm.wcm.commons.caching.CacheHeader.formatDate;
import static io.wcm.wcm.commons.caching.ModificationDateTest.SAMPLE_CALENDAR_1;
import static io.wcm.wcm.commons.caching.ModificationDateTest.SAMPLE_CALENDAR_2;
import static io.wcm.wcm.commons.caching.ModificationDateTest.applyLastModified;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.day.cq.wcm.api.WCMMode;

@RunWith(MockitoJUnitRunner.class)
public class CacheHeaderTest {

  @Mock
  private Resource resource;
  @Mock
  private SlingHttpServletRequest request;
  @Mock
  private SlingHttpServletResponse response;

  @Before
  public void setUp() {
    applyLastModified(resource, SAMPLE_CALENDAR_1);
  }

  @Test
  public void testIsNotModified_WithoutIfModifiedSinceHeader_Publish() throws Exception {
    when(request.getAttribute(WCMMode.REQUEST_ATTRIBUTE_NAME)).thenReturn(WCMMode.DISABLED);
    assertFalse(CacheHeader.isNotModified(resource, request, response));
    verify(response).setHeader(HEADER_LAST_MODIFIED, formatDate(SAMPLE_CALENDAR_1.getTime()));
    verifyNoMoreInteractions(response);
  }

  @Test
  public void testIsNotModified_WithoutIfModifiedSinceHeader_Author() throws Exception {
    when(request.getAttribute(WCMMode.REQUEST_ATTRIBUTE_NAME)).thenReturn(WCMMode.EDIT);
    assertFalse(CacheHeader.isNotModified(resource, request, response));
    verify(response).setHeader(HEADER_LAST_MODIFIED, formatDate(SAMPLE_CALENDAR_1.getTime()));
    verify(response).setHeader(HEADER_EXPIRES, "-1");
    verifyNoMoreInteractions(response);
  }

  @Test
  public void testIsNotModified_WithIfModifiedSinceHeader_Publish() throws Exception {
    when(request.getAttribute(WCMMode.REQUEST_ATTRIBUTE_NAME)).thenReturn(WCMMode.DISABLED);
    when(request.getHeader(HEADER_IF_MODIFIED_SINCE)).thenReturn(formatDate(SAMPLE_CALENDAR_2.getTime()));
    assertTrue(CacheHeader.isNotModified(resource, request, response));
    verify(response).setStatus(HttpServletResponse.SC_NOT_MODIFIED);
    verifyNoMoreInteractions(response);
  }

  @Test
  public void testIsNotModified_WithIfModifiedSinceHeader_Author() throws Exception {
    when(request.getAttribute(WCMMode.REQUEST_ATTRIBUTE_NAME)).thenReturn(WCMMode.EDIT);
    when(request.getHeader(HEADER_IF_MODIFIED_SINCE)).thenReturn(formatDate(SAMPLE_CALENDAR_2.getTime()));
    assertTrue(CacheHeader.isNotModified(resource, request, response));
    verify(response).setStatus(HttpServletResponse.SC_NOT_MODIFIED);
    verifyNoMoreInteractions(response);
  }

  @Test
  public void testSetNonCachingHeaders() throws Exception {
    CacheHeader.setNonCachingHeaders(response);
    verify(response).setHeader(HEADER_PRAGMA, "no-cache");
    verify(response).setHeader(HEADER_CACHE_CONTROL, "no-cache");
    verify(response).setHeader(HEADER_EXPIRES, "0");
    verify(response).setHeader(HEADER_DISPATCHER, "no-cache");
    verifyNoMoreInteractions(response);
  }

  @Test
  public void testSetExpires() {
    CacheHeader.setExpires(response, null);
    verify(response).setHeader(CacheHeader.HEADER_EXPIRES, "-1");

    Date date = new Date();
    CacheHeader.setExpires(response, date);
    verify(response).setHeader(CacheHeader.HEADER_EXPIRES, formatDate(date));
  }

  @Test
  public void testSetExpiresSeconds() {
    doAnswer(new ValidateDateHeaderAnswer(200 * DateUtils.MILLIS_PER_SECOND))
    .when(response).setHeader(eq(CacheHeader.HEADER_EXPIRES), anyString());
    CacheHeader.setExpiresSeconds(response, 200);
  }

  @Test
  public void testSetExpiresHours() {
    doAnswer(new ValidateDateHeaderAnswer(15 * DateUtils.MILLIS_PER_HOUR))
    .when(response).setHeader(eq(CacheHeader.HEADER_EXPIRES), anyString());
    CacheHeader.setExpiresHours(response, 15);
  }

  @Test
  public void testSetExpiresDays() {
    doAnswer(new ValidateDateHeaderAnswer(20 * DateUtils.MILLIS_PER_DAY))
    .when(response).setHeader(eq(CacheHeader.HEADER_EXPIRES), anyString());
    CacheHeader.setExpiresDays(response, 20);
  }


  /**
   * Parses a expires date header value and checks against a diff to the current time with a tolerance +/- 1h, 5secs.
   * (the 1h to avoid failing tests if the daylight saving time switch is during this period)
   */
  private final class ValidateDateHeaderAnswer implements Answer {

    private static final long TIMESPAN_DIFF_TOLERANCE_MILLISECONDS = DateUtils.MILLIS_PER_SECOND * 5
        + DateUtils.MILLIS_PER_HOUR; // add

    private final long mTimespanMillisecondsFrom;
    private final long mTimespanMillisecondsTo;
    private final long mNow;

    public ValidateDateHeaderAnswer(long pTimespanMilliseconds) {
      mTimespanMillisecondsFrom = pTimespanMilliseconds - TIMESPAN_DIFF_TOLERANCE_MILLISECONDS;
      mTimespanMillisecondsTo = pTimespanMilliseconds + TIMESPAN_DIFF_TOLERANCE_MILLISECONDS;
      mNow = Calendar.getInstance().getTimeInMillis();
    }

    @Override
    public Object answer(InvocationOnMock pInvocation) throws ParseException {
      String headerValue = pInvocation.getArguments()[1].toString();
      Date dateValue = CacheHeader.parseDate(headerValue);
      long diffMilliseconds = dateValue.getTime() - mNow;
      if (diffMilliseconds < mTimespanMillisecondsFrom || diffMilliseconds > mTimespanMillisecondsTo) {
        fail("time diff " + diffMilliseconds + "ms is not between "
            + mTimespanMillisecondsFrom + "ms and " + mTimespanMillisecondsTo + "ms, "
            + "header value is: " + headerValue + ", "
            + "date is: " + dateValue + ", "
            + "diff is: " + DurationFormatUtils.formatDurationWords(diffMilliseconds, true, true) + ", "
            + "now is: " + new Date(mNow));
      }
      return null;
    }

  }

}
