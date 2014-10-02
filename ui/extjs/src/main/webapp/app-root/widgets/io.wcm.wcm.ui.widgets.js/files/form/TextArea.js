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
 * Form field for multiple line String data.
 * <p>Enhancements over CQ5 version:</p>
 * <ul>
 *   <li>Store with correct String datatype via explicit @TypeHint</li>
 *   <li>Support "rows" attribute defining number of rows to display</li>
 * </ul>
 */
io.wcm.wcm.ui.form.TextArea = CQ.Ext.extend(CQ.Ext.form.TextArea, {

  /**
   * Define height of text area via rows parameter (each row is 15px in height).
   * Parameter is ignored if explicit "style" attribute is provided.
   */
  rows : null,

  /**
   * Creates a new component.
   * @param config configuration
   */
  constructor : function(config) {
    config = config || {};
    var defaults = {
    };
    CQ.Util.applyDefaults(config, defaults);

    this.rows = config.rows;

    // calculate height from rows and put into style attribute
    if (this.rows && (typeof(this.style)==="undefined" || this.style===null)) {
      this.style = "height:" + (this.rows * 15) + "px";
    }

    io.wcm.wcm.ui.form.TextArea.superclass.constructor.call(this, config);
  },

  // private
  onRender : function(ct, position) {
    io.wcm.wcm.ui.form.TextArea.superclass.onRender.call(this, ct, position);

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

CQ.Ext.reg("io.wcm.wcm.ui.textarea", io.wcm.wcm.ui.form.TextArea);
