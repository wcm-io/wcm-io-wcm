/**
 * Field set whose fields are shown/hidden based on a selected value from a type selection box.
 * This field set can e.g. be used to implement link or media reference field sets with link type/media source selection.
 */
io.wcm.wcm.ui.form.TypeSelectionFieldSet = CQ.Ext.extend(CQ.form.DialogFieldSet, {
  /**
   * Defines the property name of the select field that is used for type selection.
   */
  typeSelectionField: null,
  
  /**
   * This method can be set to pass through a method for initializing drag&drop capabilities of a field.
   * This method has to be called on "render" events of fields that require such support.
   * In context of RTE the link dialog implementation used this property to path a customized method.
   */
  initFieldDragAndDrop: null,
  
  /**
   * Creates a new TypeSelectionFieldSet component.
   * @param config configuration
   */
  constructor: function (config) {
    config = config || {};
    var defaults = {
      "collapsible": false,
      "collapsed": false,
      "initFieldDragAndDrop": function () {
      }
      /*
       * This is an example how to populate this field set - replace it with project-specific items.
       * The first items item usually is the "type select" selection field. The name of this field
       * has to be specified in the 'typeSelectionFieldName' property.
       * All other fields can be shown or hidden depending on the selection of the filter box - for each of these
       * fields the visibility is controlled by setting a 'typeSelectionValues' properties with an array
       * defining for which type selection options the field should be displayed.
       */
      /*
       "typeSelectionFieldName": "./type",
       "items": [
       {
       "xtype": "wcm.ui.extjs.selection",
       "name" : "./type",
       "fieldLabel" : "Type",
       "allowBlank" : false,
       "type" : "select",
       "defaultValue" : "option1",
       "options" : [
       {
       "value" : "option1",
       "text" : "Option 1"
       },
       {
       "value" : "option2",
       "text" : "Option 2"
       }
       ]
       },
       {
       "xtype": "wcm.ui.extjs.textfield",
       "name": "./text1",
       "fieldLabel": "Text 1",
       "typeSelectionValues": ["option1"]
       },
       {
       "xtype": "wcm.ui.extjs.textfield",
       "name": "./text2",
       "fieldLabel": "Text 2",
       "typeSelectionValues": ["option2"]
       }
       ]
       */
    };
    CQ.Util.applyDefaults(config, defaults);

    this.typeSelectionFieldName = config.typeSelectionFieldName;

    io.wcm.wcm.ui.form.TypeSelectionFieldSet.superclass.constructor.call(this, config);
  },
  
  /**
   * Initializes the component.
   */
  initComponent: function () {
    io.wcm.wcm.ui.form.TypeSelectionFieldSet.superclass.initComponent.call(this);

    var typeSelectionFieldSet = this;

    // detect current path and initialize controls after content loaded
    typeSelectionFieldSet.registerOnLoadContent(this, function (e) {

      // call processPath for all fields in this container
      typeSelectionFieldSet.processPath(this.path);

      // init for page path
      typeSelectionFieldSet.initForPagePath(this.path);

      // get type selection field
      typeSelectionFieldSet.typeSelectionField = null;
      if (typeSelectionFieldSet.typeSelectionFieldName) {
        var fields = typeSelectionFieldSet.find("name", typeSelectionFieldSet.typeSelectionFieldName);
        if (fields) {
          typeSelectionFieldSet.typeSelectionField = fields[0];
        }
      }

      if (typeSelectionFieldSet.typeSelectionField) {

        // register event handler for type selection field
        typeSelectionFieldSet.typeSelectionField.addListener(CQ.form.Selection.EVENT_SELECTION_CHANGED, function (pComponent, pValue) {
          typeSelectionFieldSet.showHideFieldsForSelection(pValue);
        });

        // show fields depending on type selection
        typeSelectionFieldSet.showHideFieldsForSelection();

      }

      // re-layout to fix sizing issues of select boxes
      this.doLayout();

    });

  },
  
  /**
   * Initialize controls for path of current page.
   */
  initForPagePath: function (pPath) {

    // can be overridden by subclasses

  },
  /**
   * Show/hide fields depending on type selection
   */
  showHideFieldsForSelection: function (pValue) {
    var value = pValue;
    if (!value) {
      value = this.typeSelectionField.getValue();
    }
    this.cascade(function () {
      var field = this;
      if (field.typeSelectionValues) {
        var showField = field.typeSelectionValues.indexOf(value) >= 0;
        showFormField(field, showField);
      }
    });
  },
  
  /**
   * Call "process path" methods on all form widgets
   */
  processPath: function (pPath) {
    var self = this;
    this.cascade(function () {
      var field = this;
      if (field !== self) {
        if (typeof field.processPath === "function") {
          CQ.Log.debug("TypeSelectionFieldSet#processPath: field '{0}'", [field.name]);
          field.processPath(pPath);
        }
      }
    });
  },
  
  /**
   * Call "process record" methods on all form widgets
   */
  processRecord: function (pRecord, pPath) {
    var self = this;
    this.cascade(function () {
      var field = this;
      if (field !== self) {
        if (typeof field.processRecord === "function") {
          CQ.Log.debug("TypeSelectionFieldSet#processRecord: field '{0}'", [field.name]);
          field.processRecord(pRecord, pPath);
        }
      }
    });
  },
  
  /**
   * Show/hide form field incl. associated label and description DIV containers.
   */
  showFormField: function (pField, pVisible) {
    if (typeof pField.getEl !== 'function'
            || typeof pField.addClass !== 'function'
            || typeof pField.removeClass !== 'function') {
      return;
    }
    var hiddenCssClass = "x-hidden";
    var element = pField.getEl();
    if (element) {
      var parent = element.findParent(".x-form-item");
      if (parent) {
        var parentElement = new CQ.Ext.Element(parent);
        if (pVisible) {
          parentElement.removeClass(hiddenCssClass);
        }
        else {
          parentElement.addClass(hiddenCssClass);
        }
      }
    }
    if (pVisible) {
      pField.removeClass(hiddenCssClass);
    }
    else {
      pField.addClass(hiddenCssClass);
    }
  },
  
  /**
   * Detect parent dialog of component.
   * This methods implements two ways of parent dialog detection:
   * 1. detect via xtype "dialog"
   * 2. if no match found, find a component which has set a flag "wcmUiExtJsParentDialog" to true
   */
  getParentDialog: function (pComponent) {
    var parentDialog = pComponent.findParentByType("dialog");
    if (parentDialog) {
      return parentDialog;
    }
    else {
      return pComponent.findParentBy(function (p) {
        return p.wcmUiExtJsParentDialog === true;
      });
    }
  },
  
  /**
   * Register function on "loadContent" event of parent dialog of component.
   */
  registerOnLoadContent: function (pComponent, pFunction) {
    pComponent.on("render", function () {
      var parentDialog = getParentDialog(pComponent);
      if (parentDialog) {
        parentDialog.on("loadContent", pFunction, this.parentDialog);
      }
    }, pComponent);
  }

});

// register xtype
CQ.Ext.reg("io.wcm.wcm.ui.typeselectionfieldset", io.wcm.wcm.ui.form.TypeSelectionFieldSet);
