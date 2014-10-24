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

import static org.junit.Assert.assertEquals;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.junit.Test;

public class ToStringStyleTest {

  @Test
  public void testAllSet() {
    ToStringTester tester = new ToStringTester("abc", new String[] {
        "abc", "def"
    }, new StringBuilder("def"));

    assertEquals("ToStringStyleTest.ToStringTester[string=abc,stringArray={abc,def},object=def]", tester.toString());
  }

  @Test
  public void testAllNull() {
    ToStringTester tester = new ToStringTester(null, null, null);

    assertEquals("ToStringStyleTest.ToStringTester[]", tester.toString());
  }

  @Test
  public void testSomeNull() {
    ToStringTester tester = new ToStringTester("abc", null, null);

    assertEquals("ToStringStyleTest.ToStringTester[string=abc]", tester.toString());
  }


  @SuppressWarnings("unused")
  private static class ToStringTester {

    private final String string;
    private final String[] stringArray;
    private final Object object;

    public ToStringTester(String string, String[] stringArray, Object object) {
      this.string = string;
      this.stringArray = stringArray;
      this.object = object;
    }

    @Override
    public String toString() {
      return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_OMIT_NULL_STYLE);
    }

  }

}
