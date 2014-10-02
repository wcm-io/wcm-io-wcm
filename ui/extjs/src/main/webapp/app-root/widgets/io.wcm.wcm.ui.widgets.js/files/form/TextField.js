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
 * Form field for String data.
 * <p>Enhancements over CQ5 version:</p>
 * <ul>
 *   <li>Store with correct String datatype via explicit @TypeHint</li>
 *   <li>Set maxlength attribute for text field to dis-allow typing more text than allowed</li>
 *   <li>Fix problem with default value in CQ54 </li>
 * </ul>
 */
io.wcm.wcm.ui.form.TextField = CQ.Ext.extend(CQ.Ext.form.TextField, {

  /**
   * Creates a new component.
   * @param config configuration
   */
  constructor : function(config) {
    config = config || {};

    // set value to defaultValue to fix problem in CQ54 with applying default values
    if (config.value===undefined && config.defaultValue!==undefined) {
      config.value = config.defaultValue;
    }

    io.wcm.wcm.ui.form.TextField.superclass.constructor.call(this, config);
  },

  // private
  onRender : function(ct, position) {
    io.wcm.wcm.ui.form.TextField.superclass.onRender.call(this, ct, position);

    // If maxLength property is specified, set maxlength attribute for input element as well
    if (this.maxLength && this.maxLength<Number.MAX_VALUE) {
      this.el.dom.setAttribute("maxlength", this.maxLength);
    }

    // add additional hidden form field for JCR attribute type information
    if (!this.typeHintElement) {
      this.typeHintElement = new CQ.Ext.form.Hidden({
        name: this.name + "@TypeHint",
        value: "String",
        renderTo: ct
      });
    }
  }

});

CQ.Ext.reg("io.wcm.wcm.ui.textfield", io.wcm.wcm.ui.form.TextField);
