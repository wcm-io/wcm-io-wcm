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

import java.io.IOException;
import java.io.StringWriter;

import javax.inject.Inject;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.osgi.annotation.versioning.ProviderType;

import com.day.cq.wcm.api.AuthoringUIMode;
import com.day.cq.wcm.api.components.ComponentContext;
import com.day.cq.wcm.undo.UndoConfigService;

import io.wcm.sling.models.annotations.AemObject;

/**
 * Controller for wcmInit component.
 */
@Model(adaptables = SlingHttpServletRequest.class)
@ProviderType
public final class WcmInit {

  private final boolean touchUI;
  private final boolean classicUI;
  private final String pagePropertiesDialogPath;
  private final String undoConfig;

  /**
   * Constructor
   */
  @Inject
  public WcmInit(
      @AemObject AuthoringUIMode authoringUIMode,
      @AemObject ComponentContext componentContext,
      @OSGiService UndoConfigService undoConfigService) throws IOException {

    // detect touch authoring mode
    this.touchUI = (authoringUIMode == AuthoringUIMode.TOUCH);
    this.classicUI = (authoringUIMode == AuthoringUIMode.CLASSIC);

    // detect page properties dialog path
    if (componentContext.getEditContext() != null) {
      this.pagePropertiesDialogPath = componentContext.getEditContext().getComponent().getDialogPath();
    }
    else {
      this.pagePropertiesDialogPath = null;
    }

    // get undo config
    StringWriter writer = new StringWriter();
    undoConfigService.writeClientConfig(writer);
    undoConfig = writer.toString();
  }

  /**
   * @return true if Touch UI authoring mode is active
   */
  public boolean isTouchUI() {
    return this.touchUI;
  }

  /**
   * @return true if Classic UI authoring mode is active
   */
  public boolean isClassicUI() {
    return this.classicUI;
  }

  /**
   * @return Page properties dialog path
   */
  public String getPagePropertiesDialogPath() {
    return this.pagePropertiesDialogPath;
  }

  /**
   * @return Undo configuration JSON string
   */
  public String getUndoConfig() {
    return this.undoConfig;
  }

}
