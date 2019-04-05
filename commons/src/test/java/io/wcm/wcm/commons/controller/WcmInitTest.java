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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.Writer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import com.day.cq.wcm.api.AuthoringUIMode;
import com.day.cq.wcm.api.components.Component;
import com.day.cq.wcm.api.components.ComponentContext;
import com.day.cq.wcm.api.components.EditContext;
import com.day.cq.wcm.undo.UndoConfigService;

@ExtendWith(MockitoExtension.class)
class WcmInitTest {

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

  @BeforeEach
  void setUp() throws Exception {
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
  void testIsTouchAuthoring() throws Exception {
    WcmInit underTest = new WcmInit(AuthoringUIMode.TOUCH, componentContext, undoConfigService);
    assertTrue(underTest.isTouchUI());
    assertFalse(underTest.isClassicUI());

    underTest = new WcmInit(AuthoringUIMode.CLASSIC, componentContext, undoConfigService);
    assertFalse(underTest.isTouchUI());
    assertTrue(underTest.isClassicUI());
  }

  @Test
  void testGetPagePropertiesDialogPath() throws Exception {
    WcmInit underTest = new WcmInit(AuthoringUIMode.TOUCH, componentContext, undoConfigService);
    assertEquals(DIALOG_PATH, underTest.getPagePropertiesDialogPath());

    when(componentContext.getEditContext()).thenReturn(null);
    underTest = new WcmInit(AuthoringUIMode.TOUCH, componentContext, undoConfigService);
    assertNull(underTest.getPagePropertiesDialogPath());
  }

  @Test
  void testGetUndoConfig() throws Exception {
    WcmInit underTest = new WcmInit(AuthoringUIMode.TOUCH, componentContext, undoConfigService);
    assertEquals(UNDO_CONFIG, underTest.getUndoConfig());
  }


}
