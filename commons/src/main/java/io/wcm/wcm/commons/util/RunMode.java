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

import java.util.Set;

/**
 * Sling run mode utility methods
 */
public final class RunMode {

  /**
   * Runmode for author instance
   */
  public static final String AUTHOR = "author";

  /**
   * Runmode for publish instance
   */
  public static final String PUBLISH = "publish";

  private RunMode() {
    // static methods only
  }

  /**
   * Checks if given run mode is active.
   * @param runModes Run modes for current instance
   * @param expectedRunModes Run mode(s) to check for
   * @return true if any of the given run modes is active
   */
  public static boolean is(Set<String> runModes, String... expectedRunModes) {
    if (runModes != null && expectedRunModes != null) {
      for (String expectedRunMode : expectedRunModes) {
        if (runModes.contains(expectedRunMode)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Checks if context is running on author instance.
   * @param runModes Run modes
   * @return true if "author" run mode is active
   */
  public static boolean isAuthor(Set<String> runModes) {
    return RunMode.is(runModes, AUTHOR);
  }

  /**
   * Checks if context is running on publish instance.
   * @param runModes Run modes
   * @return true if "publish" run mode is active
   */
  public static boolean isPublish(Set<String> runModes) {
    return RunMode.is(runModes, PUBLISH);
  }

}
