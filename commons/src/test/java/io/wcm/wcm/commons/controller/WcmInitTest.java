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
package io.wcm.wcm.commons.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.Writer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.day.cq.wcm.api.AuthoringUIMode;
import com.day.cq.wcm.api.components.Component;
import com.day.cq.wcm.api.components.ComponentContext;
import com.day.cq.wcm.api.components.EditContext;
import com.day.cq.wcm.undo.UndoConfigService;

@RunWith(MockitoJUnitRunner.class)
public class WcmInitTest {

  private static final String DIALOG_PATH = "/dialog/path";
  private static final String UNDO_CONFIG = "{config:'xyz'}";

  @Mock
  private ComponentContext componentContext;
  @Mock
  private EditContext editContext;
  @Mock
  private Component component;
  @Mock
  private UndoConfigService undoConfigService;

  @Before
  public void setUp() throws Exception {
    when(componentContext.getEditContext()).thenReturn(editContext);
    when(editContext.getComponent()).thenReturn(component);
    when(component.getDialogPath()).thenReturn(DIALOG_PATH);
    doAnswer(new Answer<Object>() {
      @Override
      public Object answer(InvocationOnMock invocation) throws IOException {
        Writer writer = (Writer)invocation.getArguments()[0];
        writer.write(UNDO_CONFIG);
        return null;
      }
    }).when(undoConfigService).writeClientConfig(any(Writer.class));
  }

  @Test
  public void testIsTouchAuthoring() throws Exception {
    WcmInit underTest = new WcmInit(AuthoringUIMode.TOUCH, componentContext, undoConfigService);
    assertTrue(underTest.isTouchAuthoring());

    underTest = new WcmInit(AuthoringUIMode.CLASSIC, componentContext, undoConfigService);
    assertFalse(underTest.isTouchAuthoring());
  }

  @Test
  public void testGetPagePropertiesDialogPath() throws Exception {
    WcmInit underTest = new WcmInit(AuthoringUIMode.TOUCH, componentContext, undoConfigService);
    assertEquals(DIALOG_PATH, underTest.getPagePropertiesDialogPath());

    when(componentContext.getEditContext()).thenReturn(null);
    underTest = new WcmInit(AuthoringUIMode.TOUCH, componentContext, undoConfigService);
    assertNull(DIALOG_PATH, underTest.getPagePropertiesDialogPath());
  }

  @Test
  public void testGetUndoConfig() throws Exception {
    WcmInit underTest = new WcmInit(AuthoringUIMode.TOUCH, componentContext, undoConfigService);
    assertEquals(UNDO_CONFIG, underTest.getUndoConfig());
  }


}
