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
 * Form field for date value.
 * <p>Enhancements over AEM version:</p>
 * <ul>
 *   <li>Ensure time is always set to 00:00:00</li>
 * </ul>
 */
io.wcm.wcm.ui.form.DateField = CQ.Ext.extend(io.wcm.wcm.ui.form.DateTimeField, {

  /**
   * Creates a new component.
   * @param config configuration
   */
  constructor : function(config) {
    config = config || {};

    // set value to defaultValue to fix problem in AEM with applying default values
    if (config.value===undefined && config.defaultValue!==undefined) {
      config.value = config.defaultValue;
    }

    var defaults = {
      "hideTime": true
    };
    CQ.Util.applyDefaults(config, defaults);

    io.wcm.wcm.ui.form.DateField.superclass.constructor.call(this, config);
  },

  /**
   * @private Updates the date part
   */
  updateDate : function() {
    io.wcm.wcm.ui.form.DateField.superclass.updateDate.call(this);

    // always set time to 00:00:00
    if (this.dateValue &&
        (typeof this.dateValue.setHours === "function") &&
        (typeof this.dateValue.setMinutes === "function") &&
        (typeof this.dateValue.setSeconds === "function")) {
      this.dateValue.setHours(0);
      this.dateValue.setMinutes(0);
      this.dateValue.setSeconds(0);
    }
  }

});

CQ.Ext.reg("io.wcm.wcm.ui.datefield", io.wcm.wcm.ui.form.DateField);
