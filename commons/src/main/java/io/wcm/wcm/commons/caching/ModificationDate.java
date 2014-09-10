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

import java.util.Calendar;
import java.util.Date;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ValueMap;

import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;

/**
 * Contains static utility methods to find the last modification date of one or multiple resources
 */
public final class ModificationDate {

  private ModificationDate() {
    // utility methods only
  }

  /**
   * Get modification date of page
   * @param page CQ page
   * @return Modification date or null if none set
   */
  public static Date get(Page page) {
    if (page == null) {
      return null;
    }
    Calendar lastModified = page.getLastModified();
    if (lastModified != null) {
      return lastModified.getTime();
    }
    else {
      return null;
    }
  }

  /**
   * Looks for either jcr:lastModified or cq:lastModified property in the given resource, which can be either the jcr:content-ode of a cq-page, or a rendition
   * node
   * @param resource a resource with a cq:lastModified property *and/or* a file/jcr:content subnode with a jcr:lastModified property
   * @return the date or null if last modified property could not be found
   */
  public static Date get(Resource resource) {
    if (resource == null) {
      return null;
    }
    ValueMap resourceProperties = resource.getValueMap();

    // get the cq:lastModified property from the resource (used in jcr:content nodes of cq pages)
    Date cqModified = resourceProperties != null ? resourceProperties.get(NameConstants.PN_PAGE_LAST_MOD, Date.class) : null;

    // try to find a jcr:lastModified property that is used for binary uploads or cq paragraphs
    Date resourceModified = getResourceMetadataModificationTime(resource);

    // get the most recent date of both
    return mostRecent(cqModified, resourceModified);
  }

  private static Date getResourceMetadataModificationTime(Resource resource) {
    ResourceMetadata metadata = resource.getResourceMetadata();
    if (metadata != null) {
      long modificationTime = metadata.getModificationTime();
      if (modificationTime > 0) {
        return new Date(modificationTime);
      }
    }
    return null;
  }

  /**
   * Finds the most recent modification date of all specified resources
   * @param resources multiple resources (typically jcr:content nodes of cq-pages or rendition resources)
   * @return the most recent date (or null if none of the resources has a modification date)
   */
  public static Date mostRecent(Resource... resources) {
    Date[] dates = new Date[resources.length];
    for (int i = 0; i < resources.length; i++) {
      dates[i] = get(resources[i]);
    }
    return mostRecent(dates);
  }

  /**
   * Finds the most recent modification date of all specified pages
   * @param pages multiple cq pages
   * @return the most recent date (or null if none of the pages has a modification date)
   */
  public static Date mostRecent(Page... pages) {
    Date[] dates = new Date[pages.length];
    for (int i = 0; i < pages.length; i++) {
      dates[i] = pages[i].getLastModified().getTime();
    }
    return mostRecent(dates);
  }

  /**
   * Finds the most recent modification date from a {@link ModificationDateProvider} and multiple additional resources
   * @param dateProviders Multiple modification date providers
   * @return the most recent modification date (or null if none of the objects has a modification date)
   */
  public static Date mostRecent(ModificationDateProvider... dateProviders) {
    Date[] dates = new Date[dateProviders.length];
    for (int i = 0; i < dateProviders.length; i++) {
      dates[i] = dateProviders[i].getModificationDate();
    }
    return mostRecent(dates);
  }

  /**
   * Returns the most recent of the specified dates
   * @param dates
   * @return the most recent (or null if all dates were null)
   */
  public static Date mostRecent(Date... dates) {
    Date mostRecentDate = null;
    for (Date date : dates) {
      if (date != null && (mostRecentDate == null || date.after(mostRecentDate))) {
        mostRecentDate = date;
      }
    }
    return mostRecentDate;
  }

}
