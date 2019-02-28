## Granite UI components


### Config-Scope Path Browser

This is a customized Path Field/Browser that always sets the root path to the inner-most configuration scope root. If you want to show only pages of the current site, consider using the Site Root Path Picker from [wcm.io URL Handler Granite UI components][url-handler-graniteui-components].

Requires proper configuration of Apache Sling Context-Aware configuration, see also [wcm.io Context-Aware Configuration][wcmio-caconfig].


```json
"field": {
  "sling:resourceType": "wcm-io/wcm/ui/granite/components/form/configScopePathBrowser",
  "name": "./field",
  "fieldLabel": "Internal Page"
}
```

Enhancements over AEM version:

* Dynamically sets the `rootPath` to the configuration scope root.
* Optional config parameter `appendPath`: provides a relative path (starting with "/") which is appended to the detected root path


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


### Number Field

Number field for long value.

```json
"field": {
  "sling:resourceType": "wcm-io/wcm/ui/granite/components/form/numberfield",
  "name": "./field",
  "fieldLabel": "Number"
}
```

Enhancements over AEM version:

* Store with correct Long datatype via explicit @TypeHint


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


### Pathfield

A field that allows the user to enter path. This path field can be used for both picking page paths or asset paths.

```json
"field": {
  "sling:resourceType": "wcm-io/wcm/ui/granite/components/form/pathfield",
  "name": "./field",
  "rootPath": "/content"
}
```

Enhancements over AEM version:

* Keep repository order for orderable parent nodes (e.g. pages)
* Always display root path in an extra column, so it can be selected as well
* Path field always displays only the subtree of the configure root path, regardless if the given path value has a path outside the root path


[wcmio-caconfig]: http://wcm.io/caconfig/
[url-handler-graniteui-components]: http://wcm.io/handler/url/graniteui-components.html
