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
package io.wcm.wcm.commons.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.day.cq.wcm.api.WCMMode;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.commons.testcontext.AppAemContext;

@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
class DenyWcmDisabledTest {

  private final AemContext context = AppAemContext.newAemContext();

  @Test
  void testWcmModeDisabled() {
    WCMMode.DISABLED.toRequest(context.request());
    context.request().adaptTo(DenyWcmDisabled.class);
    assertEquals(HttpServletResponse.SC_FORBIDDEN, context.response().getStatus());
  }

  @Test
  void testWcmModeEdit() {
    WCMMode.EDIT.toRequest(context.request());
    context.request().adaptTo(DenyWcmDisabled.class);
    assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());
  }

  @Test
  void testWcmModeDisabledNotFound() {
    WCMMode.DISABLED.toRequest(context.request());
    context.request().setAttribute("errorCode", HttpServletResponse.SC_NOT_FOUND);
    context.request().adaptTo(DenyWcmDisabled.class);
    assertEquals(HttpServletResponse.SC_NOT_FOUND, context.response().getStatus());
  }

}
