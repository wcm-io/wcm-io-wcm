## ExtJS Widgets

### BrowseField

The BrowseField class represents an input field with a button to open a `CQ.BrowseDialog` for browsing links.

```json
"field": {
  "jcr:primaryType": "cq:Widget",
  "xtype": "io.wcm.wcm.ui.browsefield",
  "name": "./field",
  "fieldLabel": "Browse Field"
}
```

Enhancements over AEM version:

* Displaying only a subtree with a specific root and root label
* Support initForPagePath method to initialize for current page's path
* Supports validateSelectedNode method to validate node on selection in tree
* Supports configurable Drag&Drop from content finder

Configuration properties:

* **treeRootPath**: If a path is defined, only the subtree starting at this path is displayed in the browse field.
* **treeRootText**: If a treeRootPath is specified, this attribute defines the label displays the label for the root node.
* **treeDataPath**: The path that serves the JSON tree data (if empty, the treeRootPath will be used)
* **dataSelectorString**: Selector strings to be used for generating tree JSON request. Default: `.wcm-io-wcm-ui-extjs-pagetree`
* **dataUrlParameters**: Additional URL parameters to be appended to JSON requests for fetching tree.
* **ddGroups**: Array of Drag&Drop groups (if null, Drag&Drop support is disabled)
* **ddAccept**: Array of Drag&Drop accept patterns (if null, Drag&Drop support is disabled)


### Checkbox

Form checkbox for boolean data.

```json
"field": {
  "jcr:primaryType": "cq:Widget",
  "xtype": "io.wcm.wcm.ui.checkbox",
  "name": "./field",
  "fieldLabel": "Checkbox"
}
```

Enhancements over AEM version:

* Fix problem with standalone ExtJS checkbox storing always a correct value
* Store with correct Boolean datatype via explicit @TypeHint
* Always store value to repository (true or false), even if checkbox is not checked
* Fix problem with default value in AEM


### DateField

Form field for date value.

```json
"field": {
  "jcr:primaryType": "cq:Widget",
  "xtype": "io.wcm.wcm.ui.datefield",
  "name": "./field",
  "fieldLabel": "Date field"
}
```

Enhancements over AEM version:

* Ensure time is always set to 00:00:00


### DateTimeField

Form field for date/time value.

```json
"field": {
  "jcr:primaryType": "cq:Widget",
  "xtype": "",
  "name": "./field",
  "fieldLabel": "Date/time field"
}
```

Enhancements over AEM version:

* Set default date format
* Fix problems with setting date field to empty value


### DoubleField

Form field for double number data.

```json
"field": {
  "jcr:primaryType": "cq:Widget",
  "xtype": "io.wcm.wcm.ui.doublefield",
  "name": "./field",
  "fieldLabel": "Double field"
}
```

Enhancements over AEM version:

* Store with correct Double datatype via explicit @TypeHint
* Fix problem with default value in AEM


### Hidden

Hidden form field.

```json
"field": {
  "jcr:primaryType": "cq:Widget",
  "xtype": "io.wcm.wcm.ui.hidden",
  "name": "./field",
  "defaultValue": "myvalue"
}
```

Enhancements over AEM version:

* Fix problem with default value in AEM


### LongField

Form field for long number data.

```json
"field": {
  "jcr:primaryType": "cq:Widget",
  "xtype": "io.wcm.wcm.ui.longfield",
  "name": "./field",
  "fieldLabel": "Long field"
}
```

Enhancements over AEM version:

* Store with correct Long datatype via explicit @TypeHint
* Do not allow decimals<
* Fix problem with default value in AEM


### MultiField

The MultiField is an editable list of form fields for editing multi-value properties.

```json
"field": {
  "jcr:primaryType": "cq:Widget",
  "xtype": "io.wcm.wcm.ui.multifield",
  "name": "./field",
  "fieldLabel": "Multi field"
}
```

Enhancements over AEM version:

* Allow drag and drop into subfield


### RichText

Form field for editing styled text information (rich text).

```json
"field": {
  "jcr:primaryType": "cq:Widget",
  "xtype": "io.wcm.wcm.ui.richtext",
  "name": "./field"
}
```

Enhancements over AEM version:

* Set link browse protocols to http|https|mailto
* Use strong and em instead of b and i for bold/italic
* Ensure that all &lt;br&gt; tags are replaced with XHTML-conformant &lt;br/&gt; tags


### Selection

Enhanced version of selection.

```json
"field": {
  "jcr:primaryType": "cq:Widget",
  "xtype": "io.wcm.wcm.ui.selection",
  "name": "./field",
  "fieldLabel": "Selection"
}
```

Enhancements over AEM version:

* Fixes validation problem with allowBlank=true
* Support initForPagePath method to initialize for current page's path
* Add loadChildPageOptions method to fill selection with a list of child pages
* Supports configurable Drag&Drop from content finder
* Fix problem with default value in AEM

Configuration properties:

* **ddGroups**: Array of Drag&Drop groups (if null, Drag&Drop support is disabled)
* **ddAccept**: Array of Drag&Drop accept patterns (if null, Drag&Drop support is disabled)
* **emptyOptionText**: Text for empty option that is inserted automatically for the loadChildPageOptions if blank is allowed.


### TextArea

Form field for multiple line String data.

```json
"field": {
  "jcr:primaryType": "cq:Widget",
  "xtype": "io.wcm.wcm.ui.textarea",
  "name": "./field",
  "fieldLabel": "Text area"
}
```

Enhancements over AEM version:

* Store with correct String datatype via explicit @TypeHint
* Support "rows" attribute defining number of rows to display

Configuration properties:

* **rows**: Define height of text area via rows parameter (each row is 15px in height). Parameter is ignored if explicit "style" attribute is provided.


### TextField

Form field for String data.

```json
"field": {
  "jcr:primaryType": "cq:Widget",
  "xtype": "io.wcm.wcm.ui.textfield",
  "name": "./field",
  "fieldLabel": "Text field"
}
```

Enhancements over AEM version:

* Store with correct String datatype via explicit @TypeHint
* Set maxlength attribute for text field to dis-allow typing more text than allowed
* Fix problem with default value in AEM


### TimeField

Form field for time value.

```json
"field": {
  "jcr:primaryType": "cq:Widget",
  "xtype": "io.wcm.wcm.ui.timefield",
  "name": "./field",
  "fieldLabel": "Time field"
}
```


### TypeSelectionFieldSet

Field set whose fields are shown/hidden based on a selected value from a type selection box.

```json
"field": {
  "jcr:primaryType": "cq:Widget",
  "xtype": "io.wcm.wcm.ui.typeselectionfieldset",
  "typeSelectionFieldName": "./type",
  "items": [
    {
      "xtype": "io.wcm.wcm.ui.selection",
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
      "xtype": "io.wcm.wcm.ui.textfield",
      "name": "./text1",
      "fieldLabel": "Text 1",
      "typeSelectionValues": ["option1"]
    },
    {
      "xtype": "io.wcm.wcm.ui.textfield",
      "name": "./text2",
      "fieldLabel": "Text 2",
      "typeSelectionValues": ["option2"]
    }
  ]
}
```

This is an example how to populate this field set - replace it with project-specific items.

The first items item usually is the "type select" selection field. The name of this field to be specified in the 'typeSelectionFieldName' property.

All other fields can be shown or hidden depending on the selection of the filter box - for each of these fields the visibility is controlled by setting a 'typeSelectionValues' properties with an array defining for which type selection options the field should be displayed.
