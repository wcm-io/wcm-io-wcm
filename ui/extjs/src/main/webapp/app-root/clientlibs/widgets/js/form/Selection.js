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
 * Enhanced version of selection.
 * <p>Enhancements over CQ5 version:</p>
 * <ul>
 *   <li>Fixes validation problem with allowBlank=true</li>
 *   <li>Support initForPagePath method to initialize for current page's path</li>
 *   <li>Add loadChildPageOptions method to fill selection with a list of child pages</li>
 *   <li>Supports configurable Drag&Drop from contentfinder</li>
 *   <li>Fix problem with default value in CQ54 </li>
 * </ul>
 */
io.wcm.wcm.ui.form.Selection = CQ.Ext.extend(CQ.form.Selection, {

  /**
   * Array of Drag&Drop groups (if null, Drag&Drop support is disabled)
   */
  ddGroups: null,

  /**
   * Array of Drag&Drop accept patterns (if null, Drag&Drop support is disabled)
   */
  ddAccept: null,

  /**
   * Text for empty option that is inserted automatically for the loadChildPageOptions if blank is allowed.
   */
  emptyOptionText: "(none)",

  // private
  initialLoadedValue: null,

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

    io.wcm.wcm.ui.form.Selection.superclass.constructor.call(this, config);
  },

  /*
   * The original CQ.form.Selection control does not override "getRawValue" method
   * which leads to validation error with "allowBlank=true", because blank validation
   * uses getRawValue method
   */
  getRawValue: function() {
    return this.getValue();
  },

  /*
   * Store value initialy loaded from page in a separate variable "initialLoadedValue" to set
   * it later on after loading selection again.
   */
  setValue : function(pValue) {
    this.initialLoadedValue = pValue;
    io.wcm.wcm.ui.form.Selection.superclass.setValue.call(this, pValue);
  },

  /**
   * Initializes the field.
   * If <code>ignoreData</code> is true processRecord will not be called afterwards.
   */
  processPath: function(pPath, pIgnoreData) {

    // set current path
    this.initForPagePath(pPath);

  },

  // private
  onRender : function(ct, position) {
    io.wcm.wcm.ui.form.Selection.superclass.onRender.call(this, ct, position);

    // initialize drag&drop support
    this.initDragAndDrop();

    // add additional hidden form field for JCR attribute type information
    if (!this.typeHintElement) {
      this.typeHintElement = new CQ.Ext.form.Hidden({
        name: this.name + "@TypeHint",
        value: "String",
        renderTo: ct
      });
    }
  },

  /**
   * Set options
   */
  setOptions : function(options) {
    // ignore empty/invalid options (avoid script error in CQ5 selection control)
    if (!options) {
      return;
    }
    io.wcm.wcm.ui.form.Selection.superclass.setOptions.call(this, options);
  },

  /**
   * Initialize drag&drop for field
   */
  initDragAndDrop : function() {
    if ((typeof(this.ddGroups)==="undefined" || this.ddGroups===null) && (typeof(this.ddAccept)==="undefined" || this.ddAccept===null)) {
      return;
    }

    // convert to arrays if needed
    if (typeof(this.ddGroups) === "string") {
      this.ddGroups = [ this.ddGroups ];
    }
    if (typeof(this.ddAccept) === "string") {
      this.ddAccept = [ this.ddAccept ];
    }

    // handle drop support
    var field = this;
    var target = new CQ.wcm.EditBase.DropTarget(this.el, {
      "notifyDrop": function(dragObject, evt, data) {
        if (dragObject && dragObject.clearAnimations) {
          dragObject.clearAnimations(this);
        }
        if (dragObject.isDropAllowed(this)) {
          if (data.records && data.single) {
            var record = data.records[0];
            var path = record.get("path");
            field.setValue(CQ.Util.escapePath(path));
            evt.stopEvent();
            return true;
          }
          return false;
        }
      }
    });
    target.ddAccept = this.ddAccept;
    for (var i = 0; i < this.ddGroups.length; i++) {
      target.addToGroup(this.ddGroups[i]);
    }
    target.removeFromGroup(CQ.wcm.EditBase.DD_GROUP_DEFAULT);
    this.dropTargets = [ target ];

  },

  /**
   * Initialize for path of current page
   */
  initForPagePath : function(pPath) {

    // can be overridden by subclasses

  },

  /**
   * Loads list for selection from JSON URL returning array of JSON objects with value/text pairs.
   * @param pUrl JSON url
   */
  loadOptionsFromUrl : function(pUrl) {
    var options = CQ.Util.eval(CQ.HTTP.get(pUrl));

    // add empty option at first position if blank is allowed
    if (this.allowBlank && this.type==="select") {
      options.unshift({
        "value": "",
        "text": this.emptyOptionText
      });
    }

    this.setOptions(options);

    // set loaded value again to display matching text for internal value
    if (this.initialLoadedValue) {
      this.setValue(this.initialLoadedValue);
    }
    else if (this.allowBlank) {
      this.setValue("");
    }
  },

  /**
   * Loads child pages of root path as list for selection (value=page path, text=page title)
   * @param pRootPath Path of parent page of whom the child pages should be listed
   * @param pParams: Optional set of additional parametres:
   *   predicate: Name of predicate (OSGI component implementing org.apache.commons.collections.Predicate)
   */
  loadChildPageOptions : function(pRootPath, pParams) {
    // load options from pagelist json
    var url = pRootPath + ".wcm-io-wcm-ui-extjs-pagetree" + CQ.HTTP.EXTENSION_JSON;
    if (pParams && pParams.predicate) {
      url = CQ.HTTP.addParameter(url, "predicate", pParams.predicate);
    }
    url = CQ.HTTP.noCaching(CQ.Util.externalize(url));

    // load options from url
    return this.loadOptionsFromUrl(url);
  }

});

CQ.Ext.reg("io.wcm.wcm.ui.selection", io.wcm.wcm.ui.form.Selection);
