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
/**
 * Form checkbox for boolean data.
 * <p>Enhancements over AEM version:</p>
 * <ul>
 *   <li>Fix problem with standalone ExtJS checkbox storing always a correct value</li>
 *   <li>Store with correct Boolean datatype via explicit @TypeHint</li>
 *   <li>Always store value to repository (true or false), even if checkbox is not checked</li>
 *   <li>Fix problem with default value in AEM</li>
 * </ul>
 */
io.wcm.wcm.ui.form.Checkbox = CQ.Ext.extend(CQ.form.Selection, {

  /**
   * Creates a new component.
   * @param {Object} config configuration
   */
  constructor : function(config) {
    config = config || {};

    // set value to defaultValue to fix problem in AEM with applying default values
    if (config.value===undefined && config.defaultValue!==undefined) {
      config.value = config.defaultValue;
    }

    var defaults = {
      "type": "checkbox",
      "inputValue": "true"
    };
    CQ.Util.applyDefaults(config, defaults);
    io.wcm.wcm.ui.form.Checkbox.superclass.constructor.call(this, config);
  },

  // private
  onRender : function(ct, position) {
    io.wcm.wcm.ui.form.Checkbox.superclass.onRender.call(this, ct, position);

    // add additional hidden form field for JCR attribute type information
    if (!this.typeHintElement) {
      this.typeHintElement = new CQ.Ext.form.Hidden({
        name: this.name + "@TypeHint",
        value: "Boolean",
        renderTo: ct
      });
      // always store value to repository (true or false), even if checkbox is not checked
      new CQ.Ext.form.Hidden({
        name: this.name + "@DefaultValue",
        value: "false",
        renderTo: ct
      });
      new CQ.Ext.form.Hidden({
        name: this.name + "@UseDefaultWhenMissing",
        value: "true",
        renderTo: ct
      });
    }
  }

});

CQ.Ext.reg("io.wcm.wcm.ui.checkbox", io.wcm.wcm.ui.form.Checkbox);
