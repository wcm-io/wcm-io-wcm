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
import io.wcm.testing.mock.aem.junit.AemContext;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.sling.api.resource.Resource;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.wcm.api.Page;

public class ToStringStyleAemObjectsTest {

  @Rule
  public AemContext context = new AemContext();

  @Test
  public void testResource() {
    Resource resource = context.create().resource("/my/resource");
    ToStringTester tester = new ToStringTester(resource, null, null);
    assertEquals("ToStringStyleAemObjectsTest.ToStringTester[resource=/my/resource]", tester.toString());
  }

  @Test
  public void testResources() {
    Resource resource1 = context.create().resource("/my/resource1");
    Resource resource2 = context.create().resource("/my/resource2");
    ToStringTester tester = new ToStringTester(null, new Resource[] {
        resource1, resource2
    }, null);
    assertEquals("ToStringStyleAemObjectsTest.ToStringTester[resources={/my/resource1,/my/resource2}]", tester.toString());
  }

  @Test
  public void testPage() {
    Page page = context.create().page("/my/page");
    ToStringTester tester = new ToStringTester(null, null, page);
    assertEquals("ToStringStyleAemObjectsTest.ToStringTester[page=/my/page]", tester.toString());
  }


  @SuppressWarnings("unused")
  private static class ToStringTester {

    private final Resource resource;
    private final Resource[] resources;
    private final Page page;

    public ToStringTester(Resource resource, Resource[] resources, Page page) {
      this.resource = resource;
      this.resources = resources;
      this.page = page;
    }

    @Override
    public String toString() {
      return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_OMIT_NULL_STYLE);
    }

  }

}
