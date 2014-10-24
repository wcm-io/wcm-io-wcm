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

import org.apache.sling.api.resource.Resource;

import com.day.cq.wcm.api.Page;

/**
 * Custom styles for {@link org.apache.commons.lang3.builder.ToStringBuilder} from Apache Commons.
 */
public final class ToStringStyle {

  /**
   * Same as {@link org.apache.commons.lang3.builder.ToStringStyle#SHORT_PREFIX_STYLE}, but
   * omits all entries that have null values. Additional it implements special toString() rules
   * for common Sling/AEM objects like {@link Resource} and {@link Page}.
   */
  public static final org.apache.commons.lang3.builder.ToStringStyle SHORT_PREFIX_OMIT_NULL_STYLE = new DefaultToStringStyle();

  private ToStringStyle() {
    // constants only
  }

  private static class DefaultToStringStyle extends org.apache.commons.lang3.builder.ToStringStyle {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    DefaultToStringStyle() {
      this.setUseShortClassName(true);
      this.setUseIdentityHashCode(false);
    }

    @Override
    public void append(StringBuffer buffer, String fieldName, Object value, Boolean fullDetail) {
      if (value != null) {
        super.append(buffer, fieldName, preprocessToString(value), fullDetail);
      }
    }

    @Override
    public void append(StringBuffer buffer, String fieldName, Object[] array, Boolean fullDetail) {
      if (array != null) {
        super.append(buffer, fieldName, preprocessToString(array), fullDetail);
      }
    }

    private Object preprocessToString(Object object) {
      if (object instanceof Object[]) {
        return preprocessToString((Object[])object);
      }
      if (object instanceof Resource) {
        return ((Resource)object).getPath();
      }
      if (object instanceof Page) {
        return ((Page)object).getPath();
      }
      return object;
    }

    private Object[] preprocessToString(Object[] array) {
      Object[] convertedArray = new Object[array.length];
      for (int i = 0; i < array.length; i++) {
        convertedArray[i] = preprocessToString(array[i]);
      }
      return convertedArray;
    }

    /**
     * Ensure <code>Singleton</ode> after serialization.
     * @return the singleton
     */
    private Object readResolve() {
      return ToStringStyle.SHORT_PREFIX_OMIT_NULL_STYLE;
    }

  }

}
