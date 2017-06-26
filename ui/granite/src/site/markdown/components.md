## Granite UI components


### Config-Scope Path Browser

This is a customized Miller Column Path Browser that always sets the root path to the inner-most configuration scope root. That is usually the root page of the current site, allowing selection only pages from the current site.

Requires usage and proper configuration of [wcm.io Configuration](http://wcm.io/config/).


```json
"field": {
  "sling:resourceType": "wcm-io/wcm/ui/granite/components/form/configScopePathBrowser",
  "name": "./field",
  "fieldLabel": "Internal Page"
}
```

Enhancements over AEM version:

* Dynamically sets the `rootPath` to the configuration scope root.


### Checkbox

Form checkbox for boolean data.

```json
"field": {
  "sling:resourceType": "wcm-io/wcm/ui/granite/components/form/checkbox",
  "name": "./field",
  "fieldDescription": "Description for checkbox",
  "text": "Check this"
}
```

Enhancements over AEM version:

* Store with correct Boolean datatype via explicit @TypeHint
* Always store value to repository (true or false), even if checkbox is not checked
* Defaults to value 'true'


### Date Picker

Form field for date/time value.

```json
"field": {
  "sling:resourceType": "wcm-io/wcm/ui/granite/components/form/datepicker",
  "displayedFormat": "DD.MM.YYYY HH:mm",
  "name": "./field",
  "type": "datetime",
  "fieldLabel": "Date"
}
```

### Selection

Form field for selection with an array as type hint, when multiple is set to true

```json
"field": {
  "sling:resourceType": "/apps/wcm-io/wcm/ui/granite/components/form/selection",
  "name": "./field",
  "multiple": true,
  "fieldLabel": "Selection",
  "items": {
    "item1": {
      "value": "1",
      "text": "Item 1"
    },
    "item2": {
      "value": "2",
      "text": "Item 2"
    },
    ...
  }
}
```

Enhancements over AEM version:

* Store with correct Date datatype via explicit @TypeHint


### Select

Select form field.

```json
"field": {
  "sling:resourceType": "wcm-io/wcm/ui/granite/components/form/select",
  "name": "./field",
  "multiple": true,
  "fieldLabel": "Select"
}
```

Enhancements over AEM version:

* Always stores values as array if "multiple" mode is activated, regardless how many entries are selected.
