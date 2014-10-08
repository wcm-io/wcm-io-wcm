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

import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletRequest;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * Several helper methods for getting parameters from the servlet request.
 * This class automatically converts string parameters from ISO-8859-1 to UTF-8, because UTF-8 form data
 * is expected by default. This is only done if no request parameter "_charset_" with an explicit encoding is set.
 * If it is set, the {@link org.apache.sling.api.SlingHttpServletRequest} automatically converts the parameter data (DVDI-369).
 */
public final class ParamUtil {

  /**
   * The name of the form encoding parameter.
   * If such a form parameter is set in a request the {@link org.apache.sling.api.SlingHttpServletRequest} automatically
   * transcodes all parameters to this encoding.
   */
  public static final String PARAMETER_FORMENCODING = "_charset_";

  private ParamUtil() {
    // Utility class - no instancing allowed
  }

  /**
   * Returns a request parameter.<br>
   * Contrary to ServletRequest.getParameter this method returns a zero-length String and
   * not null, if the parameter does not exist.<br>
   * In addition the method fixes problems with incorrect UTF-8 characters returned by the servlet engine.
   * All character data is converted from ISO-8859-1 to UTF-8.
   * @param request Request.
   * @param param Parameter name.
   * @return Parameter value or null if it is not set.
   */
  public static String get(ServletRequest request, String param) {
    return get(request, param, null);
  }

  /**
   * Returns a request parameter.<br>
   * In addition the method fixes problems with incorrect UTF-8 characters returned by the servlet engine.
   * All character data is converted from ISO-8859-1 to UTF-8.
   * @param request Request.
   * @param param Parameter name.
   * @param defaultValue Default value.
   * @return Parameter value or the default value if it is not set.
   */
  public static String get(ServletRequest request, String param, String defaultValue) {
    String value = request.getParameter(param);
    if (value != null) {
      // convert encoding to UTF-8 if not form encoding parameter is set
      if (!hasFormEncodingParam(request)) {
        try {
          value = new String(value.getBytes(CharEncoding.ISO_8859_1), CharEncoding.UTF_8);
        }
        catch (UnsupportedEncodingException ex) {
          // ignore
        }
      }
      return value;
    }
    else {
      return defaultValue;
    }
  }

  /**
   * Returns a request parameter array.<br>
   * The method fixes problems with incorrect UTF-8 characters returned by the servlet engine.
   * All character data is converted from ISO-8859-1 to UTF-8.
   * @param request Request.
   * @param param Parameter name.
   * @return Parameter value array value or null if it is not set.
   */
  public static String[] getMultiple(ServletRequest request, String param) {
    String[] values = request.getParameterValues(param);
    if (values == null) {
      return null;
    }
    // convert encoding to UTF-8 if not form encoding parameter is set
    if (!hasFormEncodingParam(request)) {
      String[] convertedValues = new String[values.length];
      for (int i = 0; i < values.length; i++) {
        if (values[i] != null) {
          try {
            convertedValues[i] = new String(values[i].getBytes(CharEncoding.ISO_8859_1), CharEncoding.UTF_8);
          }
          catch (UnsupportedEncodingException ex) {
            // ignore and fallback to original encoding
            convertedValues[i] = values[i];
          }
        }
      }
      return convertedValues;
    }
    else {
      return values;
    }
  }

  /**
   * Returns a request parameter.<br>
   * Contrary to ServletRequest.getParameter this method returns a zero-length String and
   * not null, if the parameter does not exist.<br>
   * In addition the method fixes problems with incorrect UTF-8 characters returned by the servlet engine.
   * All character data is converted from ISO-8859-1 to UTF-8.
   * @param requestMap Request Parameter map.
   * @param param Parameter name.
   * @return Parameter value or "" if it is not set.
   */
  public static String get(Map requestMap, String param) {
    Object obj = requestMap.get(param);
    if (obj != null) {
      String value = "";
      if (obj instanceof String[]) {
        String[] astr = (String[])obj;
        if (astr.length != 0) {
          value = astr[0];
        }
      }
      else if (obj instanceof String) {
        value = (String)obj;
      }
      // convert encoding to UTF-8 if not form encoding parameter is set
      if (!hasFormEncodingParam(requestMap)) {
        try {
          value = new String(value.getBytes(CharEncoding.ISO_8859_1), CharEncoding.UTF_8);
        }
        catch (UnsupportedEncodingException ex) {
          // ignore
        }
      }
      return value;
    }
    return "";
  }

  /**
   * Returns a request parameter as integer.
   * @param request Request.
   * @param param Parameter name.
   * @return Parameter value or 0 if it does not exist or is not a number.
   */
  public static int getInt(ServletRequest request, String param) {
    return getInt(request, param, 0);
  }

  /**
   * Returns a request parameter as integer.
   * @param request Request.
   * @param param Parameter name.
   * @param defaultValue Default value.
   * @return Parameter value or default value if it does not exist or is not a number.
   */
  public static int getInt(ServletRequest request, String param, int defaultValue) {
    String value = request.getParameter(param);
    return NumberUtils.toInt(value, defaultValue);
  }

  /**
   * Returns a request parameter as long.
   * @param request Request.
   * @param param Parameter name.
   * @return Parameter value or 0 if it does not exist or is not a number.
   */
  public static long getLong(ServletRequest request, String param) {
    return getLong(request, param, 0L);
  }

  /**
   * Returns a request parameter as long.
   * @param request Request.
   * @param param Parameter name.
   * @param defaultValue Default value.
   * @return Parameter value or default value if it does not exist or is not a number.
   */
  public static long getLong(ServletRequest request, String param, long defaultValue) {
    String value = request.getParameter(param);
    return NumberUtils.toLong(value, defaultValue);
  }

  /**
   * Returns a request parameter as float.
   * @param request Request.
   * @param param Parameter name.
   * @param locale Locale to be used for parsing the float value.
   * @return Parameter value or 0 if it does not exist or is not a number.
   */
  public static float getFloat(ServletRequest request, String param, Locale locale) {
    return getFloat(request, param, 0f, locale);
  }

  /**
   * Returns a request parameter as float.
   * @param request Request.
   * @param param Parameter name.
   * @param defaultValue Default value.
   * @param locale Locale to be used for parsing the float value.
   * @return Parameter value or default value if it does not exist or is not a number.
   */
  public static float getFloat(ServletRequest request, String param, float defaultValue, Locale locale) {
    NumberFormat nf = NumberFormat.getInstance(locale);
    String value = request.getParameter(param);
    if (value != null) {
      try {
        return nf.parse(value).floatValue();
      }
      catch (ParseException ex) {
        return defaultValue;
      }
    }
    else {
      return defaultValue;
    }
  }

  /**
   * Returns a request parameter as double.
   * @param request Request.
   * @param param Parameter name.
   * @param locale Locale to be used for parsing the double value.
   * @return Parameter value or 0 if it does not exist or is not a number.
   */
  public static double getDouble(ServletRequest request, String param, Locale locale) {
    return getDouble(request, param, 0d, locale);
  }

  /**
   * Returns a request parameter as double.
   * @param request Request.
   * @param param Parameter name.
   * @param defaultValue Default value.
   * @param locale Locale to be used for parsing the double value.
   * @return Parameter value or default value if it does not exist or is not a number.
   */
  public static double getDouble(ServletRequest request, String param, double defaultValue, Locale locale) {
    NumberFormat nf = NumberFormat.getInstance(locale);
    String value = request.getParameter(param);
    if (value != null) {
      try {
        return nf.parse(value).doubleValue();
      }
      catch (ParseException ex) {
        return defaultValue;
      }
    }
    else {
      return defaultValue;
    }
  }

  /**
   * Returns a request parameter as boolean.
   * @param request Request.
   * @param param Parameter name.
   * @return Parameter value or <code>false</code> if it does not exist or cannot be interpreted as boolean.
   */
  public static boolean getBoolean(ServletRequest request, String param) {
    return getBoolean(request, param, false);
  }

  /**
   * Returns a request parameter as boolean.
   * @param request Request.
   * @param param Parameter name.
   * @param defaultValue Default value.
   * @return Parameter value or default value if it does not exist or <code>false</code> if it cannot be interpreted as boolean.
   */
  public static boolean getBoolean(ServletRequest request, String param, boolean defaultValue) {
    String value = request.getParameter(param);
    if (value != null) {
      return BooleanUtils.toBoolean(value);
    }
    else {
      return defaultValue;
    }
  }

  /**
   * Returns a request parameter as enum value.
   * @param <T> Enum type
   * @param request Request.
   * @param param Parameter name.
   * @param enumClass Enum class
   * @return Parameter value or null if it is not set or an invalid enum value.
   */
  public static <T extends Enum> T getEnum(ServletRequest request, String param, Class<T> enumClass) {
    return getEnum(request, param, enumClass, null);
  }

  /**
   * Returns a request parameter as enum value.
   * @param <T> Enum type
   * @param request Request.
   * @param param Parameter name.
   * @param enumClass Enum class
   * @param defaultValue Default value.
   * @return Parameter value or the default value if it is not set or an invalid enum value.
   */
  @SuppressWarnings("unchecked")
  public static <T extends Enum> T getEnum(ServletRequest request, String param, Class<T> enumClass, T defaultValue) {
    String value = ParamUtil.get(request, param);
    if (StringUtils.isNotEmpty(value)) {
      try {
        return (T)Enum.valueOf(enumClass, value);
      }
      catch (IllegalArgumentException ex) {
        // ignore, return default
      }
    }
    return defaultValue;
  }

  /**
   * @param request Servlet request
   * @return Checks if form encoding parameter is set
   */
  private static boolean hasFormEncodingParam(ServletRequest request) {
    return StringUtils.isNotEmpty(request.getParameter(PARAMETER_FORMENCODING));
  }

  /**
   * @param requestMap Request map
   * @return Checks if form encoding parameter is set
   */
  private static boolean hasFormEncodingParam(Map requestMap) {
    Object object = requestMap.get(PARAMETER_FORMENCODING);
    if (object != null) {
      return StringUtils.isNotEmpty(object.toString());
    }
    else {
      return false;
    }
  }

}
