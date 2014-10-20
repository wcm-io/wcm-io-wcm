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
 * Form field for date/time value.
 * <p>Enhancements over AEM version:</p>
 * <ul>
 *   <li>Set default date format</li>
 *   <li>Fix problems with setting date field to empty value</li>
 * </ul>
 */
io.wcm.wcm.ui.form.DateTimeField = CQ.Ext.extend(CQ.form.DateTime, {

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
      "dateFormat": "m/d/Y"
    };
    CQ.Util.applyDefaults(config, defaults);

    io.wcm.wcm.ui.form.DateTimeField.superclass.constructor.call(this, config);
  },

  /**
   * @private Updates the date part
   */
  updateDate : function() {

    // set to null if date value was erased
    var d = this.df.getValue();
    if (d===null || d==="") {
      this.dateValue = null;
      return;
    }

    io.wcm.wcm.ui.form.DateTimeField.superclass.updateDate.call(this);
  },

  /**
   * private
   * Checks if the object is a date by not using instanceof
   */
  isDate: function(pObj) {
    // extra null check
    if (pObj===null) {
      return false;
    }
    return io.wcm.wcm.ui.form.DateTimeField.superclass.isDate.call(this, pObj);
  }


});

CQ.Ext.reg("io.wcm.wcm.ui.datetimefield", io.wcm.wcm.ui.form.DateTimeField);
