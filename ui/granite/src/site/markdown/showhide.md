## Granite UI Show/Hide for dialog fields

Allows to automatically show/hide dialog fields based on other fields values.

Supported Coral UI 3 components:

* coral-select
* coral-checkbox

### Usage

* Add the class `wcmio-dialog-showhide` to a select/dropdown or checkbox Granite UI component
* Add a data attribute `wcmio-dialog-showhide-target` to the same element. The value should be a selector, usually a specific class name (the target class), to find all possible target elements that can be shown/hidden.
* Add the target class to each target component that should be shown/hidden
* Add the data attribute `showhidetargetvalue` to each target component, the value should equal the value of the select option that will unhide this element.
    * For select components use the select option value
    * For checkbox components use "true" or "false" for the checkbox state

To ensure the show/hide features is applied only to a certain group of elements in the edit dialog, when it cannot be ensured that the CSS class is unique across the whole dialog (e.g. in multi fields):

* Add the data attribute `wcmio-dialog-showhide-parent` to the dropdown/select element, value should be a selector that identifies a common parent element. Only dialog fields that are children of that element (e.g. a container) will be processed.

This works the same as the AEM built-in `cq-dialog-dropdown-showhide` feature, but with additional features.


### Example

```json
{
  "sling:resourceType": "granite/ui/components/coral/foundation/container",
  "items": {
    "mycheckbox": {
      "sling:resourceType": "wcm-io/wcm/ui/granite/components/form/checkbox",
      "name": "./mycheckbox",
      "text": "Example Checkbox",
      "granite:class": "wcmio-dialog-showhide",
      "granite:data": {
        "wcmio-dialog-showhide-target": ".mycheckbox-target"
      }
    },
    "mytextfield": {
      "sling:resourceType": "granite/ui/components/coral/foundation/form/textfield",
      "granite:class": "mycheckbox-target",
      "granite:data": {
        "showhidetargetvalue": "true"
      },
      "name": "./mytextfield",
      "required": true,
      "fieldLabel": "Example textfield",
      "fieldDescription": "This is only visible when the checkbox is checked."
    }
  }
}
```
