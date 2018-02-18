/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2018 wcm.io
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

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;

import static org.junit.Assert.assertTrue;

import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.wcm.commons.testcontext.AppTemplate;

@RunWith(MockitoJUnitRunner.class)
public class TemplatePageFilterTest {

  @Mock
  private Page pageTemplate1;

  @Mock
  private Page pageTemplate2;

  @Mock
  private Page pageTemplate3;

  @Before
  public void setUp() {
    when(pageTemplate1.getProperties()).thenReturn(ImmutableValueMap.of(NameConstants.PN_TEMPLATE, AppTemplate.TEMPLATE_1.getTemplatePath()));
    when(pageTemplate2.getProperties()).thenReturn(ImmutableValueMap.of(NameConstants.PN_TEMPLATE, AppTemplate.TEMPLATE_2.getTemplatePath()));
    when(pageTemplate3.getProperties()).thenReturn(ImmutableValueMap.of(NameConstants.PN_TEMPLATE, AppTemplate.TEMPLATE_3.getTemplatePath()));
  }

  @Test
  public void testSingleMatchingTemplates() {
    TemplatePageFilter pageFilter = new TemplatePageFilter(true, true, AppTemplate.TEMPLATE_3);
    assertFalse(pageFilter.includes(pageTemplate1));
    assertFalse(pageFilter.includes(pageTemplate2));
    assertTrue(pageFilter.includes(pageTemplate3));
  }

  @Test
  public void testMultipleTemplates() {
    TemplatePageFilter pageFilter = new TemplatePageFilter(true, true, AppTemplate.TEMPLATE_1, AppTemplate.TEMPLATE_2);
    assertTrue(pageFilter.includes(pageTemplate1));
    assertTrue(pageFilter.includes(pageTemplate2));
    assertFalse(pageFilter.includes(pageTemplate3));
  }
}
