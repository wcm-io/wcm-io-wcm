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
 * Form field for long number data.
 * <p>Enhancements over CQ5 version:</p>
 * <ul>
 *   <li>Store with correct Long datatype via explicit @TypeHint</li>
 *   <li>Do not allow decimals</li>
 *   <li>Fix problem with default value in CQ54 </li>
 * </ul>
 */
io.wcm.wcm.ui.form.LongField = CQ.Ext.extend(CQ.Ext.form.NumberField, {

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

    var defaults = {
      "allowDecimals": false
    };
    CQ.Util.applyDefaults(config, defaults);

    io.wcm.wcm.ui.form.LongField.superclass.constructor.call(this, config);
  },

  // private
  onRender : function(ct, position) {
    io.wcm.wcm.ui.form.LongField.superclass.onRender.call(this, ct, position);

    // add additional hidden form field for JCR attribute type information
    if (!this.typeHintElement) {
      this.typeHintElement = new CQ.Ext.form.Hidden({
        name: this.name + "@TypeHint",
        value: "Long",
        renderTo: ct
      });
    }
  }

});

CQ.Ext.reg("io.wcm.wcm.ui.longfield", io.wcm.wcm.ui.form.LongField);
