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

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.ProviderType;

import com.day.cq.wcm.api.WCMMode;

/**
 * Contains common functionality to control client-side caching.
 */
@ProviderType
public final class CacheHeader {

  private CacheHeader() {
    // utility methods only
  }

  private static final Logger log = LoggerFactory.getLogger(CacheHeader.class);

  private static final String RFC_1123_DATE_PATTERN = "EEE, dd MMM yyyy HH:mm:ss z";

  static final String HEADER_LAST_MODIFIED = "Last-Modified";
  static final String HEADER_IF_MODIFIED_SINCE = "If-Modified-Since";
  static final String HEADER_PRAGMA = "Pragma";
  static final String HEADER_CACHE_CONTROL = "Cache-Control";
  static final String HEADER_EXPIRES = "Expires";
  static final String HEADER_DISPATCHER = "Dispatcher";

  /**
   * shared instance of the RFC1123 date format, must not be used directly but only using the synchronized {@link #formatDate(Date)} and
   * {@link #parseDate(String)} methods
   */
  private static final DateFormat RFC1123_DATE_FORMAT = new SimpleDateFormat(RFC_1123_DATE_PATTERN, Locale.US);
  static {
    // all times are written and parsed in GMT
    RFC1123_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
  }

  static synchronized String formatDate(Date pDate) {
    return RFC1123_DATE_FORMAT.format(pDate);
  }

  static synchronized Date parseDate(String pDateString) throws ParseException {
    return RFC1123_DATE_FORMAT.parse(pDateString);
  }

  /**
   * Compares the "If-Modified-Since header" of the incoming request with the last modification date of a resource. If
   * the resource was not modified since the client retrieved the resource, a 304-redirect is send to the response (and
   * the method returns true). If the resource has changed (or the client didn't) supply the "If-Modified-Since" header
   * a "Last-Modified" header is set so future requests can be cached.
   * <p>
   * Expires header is automatically set on author instance, and not set on publish instance.
   * </p>
   * @param resource the JCR resource the last modification date is taken from
   * @param request Request
   * @param response Response
   * @return true if the method send a 304 redirect, so that the caller shouldn't write any output to the response
   *         stream
   * @throws IOException
   */
  public static boolean isNotModified(Resource resource,
      SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
    ResourceModificationDateProvider dateProvider = new ResourceModificationDateProvider(resource);
    return isNotModified(dateProvider, request, response);
  }

  /**
   * Compares the "If-Modified-Since header" of the incoming request with the last modification date of a resource. If
   * the resource was not modified since the client retrieved the resource, a 304-redirect is send to the response (and
   * the method returns true). If the resource has changed (or the client didn't) supply the "If-Modified-Since" header
   * a "Last-Modified" header is set so future requests can be cached.
   * @param resource the JCR resource the last modification date is taken from
   * @param request Request
   * @param response Response
   * @param setExpiresHeader Set expires header to -1 to ensure the browser checks for a new version on every request.
   * @return true if the method send a 304 redirect, so that the caller shouldn't write any output to the response
   *         stream
   * @throws IOException
   */
  public static boolean isNotModified(Resource resource,
      SlingHttpServletRequest request, SlingHttpServletResponse response, boolean setExpiresHeader) throws IOException {
    ResourceModificationDateProvider dateProvider = new ResourceModificationDateProvider(resource);
    return isNotModified(dateProvider, request, response, setExpiresHeader);
  }

  /**
   * Compares the "If-Modified-Since header" of the incoming request with the last modification date of an aggregated
   * resource. If the resource was not modified since the client retrieved the resource, a 304-redirect is send to the
   * response (and the method returns true). If the resource has changed (or the client didn't) supply the
   * "If-Modified-Since" header a "Last-Modified" header is set so future requests can be cached.
   * <p>
   * Expires header is automatically set on author instance, and not set on publish instance.
   * </p>
   * @param dateProvider abstraction layer that calculates the last-modification time of an aggregated resource
   * @param request Request
   * @param response Response
   * @return true if the method send a 304 redirect, so that the caller shouldn't write any output to the response
   *         stream
   * @throws IOException
   */
  public static boolean isNotModified(ModificationDateProvider dateProvider,
      SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
    boolean isAuthor = WCMMode.fromRequest(request) != WCMMode.DISABLED;
    return isNotModified(dateProvider, request, response, isAuthor);
  }

  /**
   * Compares the "If-Modified-Since header" of the incoming request with the last modification date of an aggregated
   * resource. If the resource was not modified since the client retrieved the resource, a 304-redirect is send to the
   * response (and the method returns true). If the resource has changed (or the client didn't) supply the
   * "If-Modified-Since" header a "Last-Modified" header is set so future requests can be cached.
   * @param dateProvider abstraction layer that calculates the last-modification time of an aggregated resource
   * @param request Request
   * @param response Response
   * @param setExpiresHeader Set expires header to -1 to ensure the browser checks for a new version on every request.
   * @return true if the method send a 304 redirect, so that the caller shouldn't write any output to the response
   *         stream
   * @throws IOException
   */
  public static boolean isNotModified(ModificationDateProvider dateProvider,
      SlingHttpServletRequest request, SlingHttpServletResponse response, boolean setExpiresHeader) throws IOException {

    // assume the resource *was* modified until we know better
    boolean isModified = true;

    // get the modification date of the resource(s) in question
    Date lastModificationDate = dateProvider.getModificationDate();

    // get the date of the version from the client's cache
    String ifModifiedSince = request.getHeader(HEADER_IF_MODIFIED_SINCE);

    // only compare if both resource modification date and If-Modified-Since header is available
    if (lastModificationDate != null && StringUtils.isNotBlank(ifModifiedSince)) {
      try {
        java.util.Date clientModificationDate = parseDate(ifModifiedSince);

        // resource is considered modified if it's modification date is *after* the client's modification date
        isModified = lastModificationDate.getTime() - DateUtils.MILLIS_PER_SECOND > clientModificationDate.getTime();
      }
      catch (ParseException ex) {
        log.warn("Failed to parse value '" + ifModifiedSince + "' of If-Modified-Since header.", ex);
      }
    }

    // if resource wasn't modified: send a 304 and return true so the caller knows it shouldn't go on writing the response
    if (!isModified) {
      response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
      return true;
    }

    // set last modified header so future requests can be cached
    if (lastModificationDate != null) {
      response.setHeader(HEADER_LAST_MODIFIED, formatDate(lastModificationDate));
      if (setExpiresHeader) {
        // by setting an expires header we force the browser to always check for updated versions (only on author)
        response.setHeader(HEADER_EXPIRES, "-1");
      }
    }

    // tell the caller it should go on writing the response as no 304-header was send
    return false;
  }

  /**
   * Set headers to disallow caching in browser, proxy servers and dispatcher for the current response.
   * @param response Current response
   */
  public static void setNonCachingHeaders(HttpServletResponse response) {
    response.setHeader(HEADER_PRAGMA, "no-cache");
    response.setHeader(HEADER_CACHE_CONTROL, "no-cache");
    response.setHeader(HEADER_EXPIRES, "0");
    response.setHeader(HEADER_DISPATCHER, "no-cache");
  }

  /**
   * Set expires header to given date.
   * @param response Response
   * @param date Expires date
   */
  public static void setExpires(HttpServletResponse response, Date date) {
    if (date == null) {
      response.setHeader(HEADER_EXPIRES, "-1");
    }
    else {
      response.setHeader(HEADER_EXPIRES, formatDate(date));
    }
  }

  /**
   * Set expires header to given amount of seconds in the future.
   * @param response Response
   * @param seconds Seconds to expire
   */
  public static void setExpiresSeconds(HttpServletResponse response, int seconds) {
    Date expiresDate = DateUtils.addSeconds(new Date(), seconds);
    setExpires(response, expiresDate);
  }

  /**
   * Set expires header to given amount of hours in the future.
   * @param response Response
   * @param hours Hours to expire
   */
  public static void setExpiresHours(HttpServletResponse response, int hours) {
    Date expiresDate = DateUtils.addHours(new Date(), hours);
    setExpires(response, expiresDate);
  }

  /**
   * Set expires header to given amount of days in the future.
   * @param response Response
   * @param days Days to expire
   */
  public static void setExpiresDays(HttpServletResponse response, int days) {
    Date expiresDate = DateUtils.addDays(new Date(), days);
    setExpires(response, expiresDate);
  }

}
