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
 * The BrowseField class represents an input field
 * with a button to open a <code>CQ.BrowseDialog</code> for browsing links.
 * <p>Enhancements over CQ5 version:</p>
 * <ul>
 *   <li>Displaying only a subtree with a specific root and root label</li>
 *   <li>Support initForPagePath method to initialize for current page's path</li>
 *   <li>Supports validateSelectedNode method to validate node on selection in tree</li>
 *   <li>Supports configurable Drag&Drop from contentfinder</li>
 * </ul>
 */
io.wcm.wcm.ui.form.BrowseField = CQ.Ext.extend(CQ.form.BrowseField, {

  /**
   * If a path is defined, only the subtree starting at this path is displayed in the browse field.
   */
  treeRootPath: null,

  /**
   * If a treeRootPath is specified, this attribute defines the label displays the label for the root node.
   */
  treeRootText: null,

  /**
   * the path that serves the JSON tree data (if empty, the treeRootPath will be used)
   */
  treeDataPath: null,

  /**
   * Selector strings to be used for generating tree JSON request.
   * Default: .wcm_ui_extjs_widget_tree
   */
  dataSelectorString: null,

  /**
   * Additional URL parameters to be appended to JSON requests for fetching tree.
   * Default: empty
   */
  dataUrlParameters: null,

  /**
   * Array of Drag&Drop groups (if null, Drag&Drop support is disabled)
   */
  ddGroups: null,

  /**
   * Array of Drag&Drop accept patterns (if null, Drag&Drop support is disabled)
   */
  ddAccept: null,

  /**
   * Creates a new component.
   * @param config configuration
   */
  constructor : function(config) {
    config = config || {};
    var defaults = {
      "treeRootPath": null,
      "treeRootText": null,
      "treeDataPath": null,
      "dataSelectorString": ".io-wcm-wcm-ui-tree",
      "ddGroups": null,
      "ddAccept": null,

      // hide trigger initially and show when content of dialog is loaded and trigger properly initialized
      "hideTrigger": true
    };
    CQ.Util.applyDefaults(config, defaults);

    io.wcm.wcm.ui.form.BrowseField.superclass.constructor.call(this, config);
  },

  /**
   * The trigger action of the TriggerField.
   * Prepare special tree root/tree loader config if needed.
   **/
  onTriggerClick : function() {
    var browseField = this;

    // preselect current selected path
    this.content = this.getValue();

    if (this.treeRootPath) {

      this.treeRoot = {
        "name": browseField.treeRootPath.substring(1),
        "text": browseField.treeRootText ? browseField.treeRootText : CQ.I18n.getMessage("Site"),
        "draggable": false,
        "singleClickExpand": true,
        "expanded":true
      };

      this.treeLoader = {
        "dataUrl": function() {
          // for dataUrl: use treeDataPath if specified in config, otherwise use treeRootPath
          var dataPath = browseField.treeDataPath ? browseField.treeDataPath : browseField.treeRootPath;
          var url = dataPath + browseField.dataSelectorString + CQ.HTTP.EXTENSION_JSON;
          if (browseField.dataUrlParameters) {
            url += "?" + browseField.dataUrlParameters;
          }
          return CQ.Util.externalize(url);
        },
        "requestMethod":"GET",
        "baseAttrs": {
          "singleClickExpand":true
        }
      };

    }

    // --- original code from CQ.form.BrowseField extended for selectedNode hook-in ---
    if (this.disabled) {
      return;
    }
    if (this.browseDialog === null) {
      /* Create the BrowseDialog if it has not been created before */
      var browseDialogConfig = {
        "jcr:primaryType" : "cq:BrowseDialog",
        "id" : this.id + "-dialog",
        "ok" : function() {
          /* The ok handler of the BrowseDialog. */
          if (this.browseField) {

            // --- custom extension start ---
            // validate selected node
            var selectedNode = this.treePanel.getSelectionModel().getSelectedNode();
            if (selectedNode) {
              var errorMsg = this.browseField.validateSelectedNode(selectedNode);
              if (errorMsg) {
                CQ.Ext.Msg.show(errorMsg);
                return;
              }
            }
            // --- custom extension end ---

            if (this.browseField.formatHtmlLink) {
              var anchor = this.browseField.getParagraphAnchor();
              anchor = anchor === "" ? anchor : "#" + anchor;
              this.browseField.setValue(this.getSelectedPath() + ".html" + anchor);
            }
            else {
              this.browseField.setValue(this.getSelectedPath());
            }
            this.browseField.fireEvent("dialogselect", this);
          }
          this.hide();
        },
        /* pass this to the BrowseDialog to make in configurable from 'outside' */
        "parBrowse" : this.parBrowse,
        "treeRoot" : this.treeRoot,
        "treeLoader" : this.treeLoader,
        "listeners" : {
          "hide" : function() {
            if (this.browseField) {
              this.browseField.fireEvent("dialogclose");
            }
          }
        }
      };

      /* build the dialog and load its contents */
      this.browseDialog = new CQ.Util.build(browseDialogConfig);
      this.browseDialog.browseField = this;
    }

    /*
     * open the tree at the currently stored link location. if the field is empty, open the tree at the current location
     */
    if (this.getValue()) {
      this.browseDialog.loadContent(CQ.HTTP.getPath(this.getValue()));
    }
    else {
      this.browseDialog.loadContent(CQ.WCM.getPagePath());
    }

    /* Show the Dialog */
    this.browseDialog.show();
    this.fireEvent("dialogopen");
    this.fireEvent("browsedialog.opened"); // deprecated since 5.3
  },

  /**
   * Initializes the field.
   * If <code>ignoreData</code> is true processRecord will not be called afterwards.
   */
  processPath: function(pPath, pIgnoreData) {

    // set current path
    this.initForPagePath(pPath);

    // show trigger which was hidden initially until properly initialize
    if (this.hideTrigger) {
      this.trigger.setDisplayed(true);
    }

  },

  // private
  onRender : function(ct, position) {
    io.wcm.wcm.ui.form.BrowseField.superclass.onRender.call(this, ct, position);

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
            var path = record.data.path;
            // fires Change-Event
            var old = field.getValue();
            field.setValue(CQ.Util.escapePath(path));
            var v = field.getValue();
            if(String(v) !== String(old)){
              field.fireEvent("change", this, v, old);
            }
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
   * Validates note before accepting when clicking OK button.
   * @param pNode Node object
   * @return null if node is ok, otherwise a config block for a message to be displayed with CQ.Ext.Msg.show
   */
  validateSelectedNode : function(pNode) {
    return null;
  },

  /**
   * Re-initialize tree root for a browse field already in use.
   * Deletes previously configured dialog browser to ensure new settings take effect.
   */
  reinitTreeRoot : function(pTreeRootPath, pTreeRootText) {
    if (this.browseDialog) {
      this.browseDialog.destroy();
      delete this.browseDialog;
    }
    if (pTreeRootPath) {
      this.treeRootPath = pTreeRootPath;
    }
    if (pTreeRootText) {
      this.treeRootText = pTreeRootText;
    }
  }

});

CQ.Ext.reg("io.wcm.wcm.ui.browsefield", io.wcm.wcm.ui.form.BrowseField);
