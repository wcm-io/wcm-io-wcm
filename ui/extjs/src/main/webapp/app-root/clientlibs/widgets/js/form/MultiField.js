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
 * The MultiField is an editable list of form fields for editing multi-value properties.
 * <p>Enhancements over CQ5 version:</p>
 * <ul>
 *   <li>Allow drag and drop into subfields</li>
 * </ul>
 */
io.wcm.wcm.ui.form.MultiField = CQ.Ext.extend(CQ.form.MultiField, {

  getDropTargets : function() {
    var subDropTargets = [];
    try {
      // Build a list of drop targets from all sub fields
      this.items.each(function(item/* , index, length */) {
        if (item instanceof CQ.form.MultiField.Item) {
          subDropTargets = subDropTargets.concat(item.field.getDropTargets());
        }
      }, this);
    }
    catch (e) {
    }
    return subDropTargets;
  }

});

CQ.Ext.reg("io.wcm.wcm.ui.multifield", io.wcm.wcm.ui.form.MultiField);
